/*
 * Copyright (C) 2012, 2013 the diamond:dogs|group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.diamonddogs.service.net;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.RecoverySystem.ProgressListener;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.NonTimeCriticalTask;
import at.diamonddogs.data.dataobjects.NonTimeCriticalTaskQueueDefaultConfiguration;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.exception.ServiceException;
import at.diamonddogs.net.WebClient.DownloadProgressListener;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskManager;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskQueue.NonTimeCriticalTaskProcessingListener;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskQueue.NonTimeCriticalTaskQueueConfiguration;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskQueue.NonTimeCriticalTaskQueueConfigurationFactory;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskQueueConfigurationDefaultFactory;
import at.diamonddogs.service.net.HttpService.HttpServiceBinder;
import at.diamonddogs.service.net.HttpService.WebRequestReturnContainer;
import at.diamonddogs.service.processor.DataProcessor;
import at.diamonddogs.service.processor.ServiceProcessor;
import at.diamonddogs.service.processor.SynchronousProcessor;
import at.diamonddogs.util.AndroidUtils;

/**
 * The {@link HttpServiceAssister} can be used to issue {@link WebRequest}s at
 * any given time, without having to worry if a service binding to
 * {@link HttpService} exists. {@link HttpServiceAssister} supports both,
 * asynchronous and synchronous {@link WebRequest}s. If the {@link HttpService}
 * has not been bound yet, asynchronous {@link WebRequest}s will be appended to
 * a queue, which will be processed once a connection to {@link HttpService} has
 * been established. Synchronous {@link WebRequest}s will cause
 * {@link HttpServiceAssister} to wait for a maximum of
 * {@link HttpServiceAssister#SYNC_REQUEST_BINDING_TIMEOUT} milliseconds until
 * {@link Service} binding.
 */
public class HttpServiceAssister {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceAssister.class.getSimpleName());

	/**
	 * Service binding timeout for synchronous {@link WebRequest}s.
	 */
	private static final int SYNC_REQUEST_BINDING_TIMEOUT = 2000;

	private static final int SYNC_REQUEST_BINDING_TIMEOUT_DEBUG = 200000;

	private int syncRequestBindingTimeout;

	/**
	 * A {@link Context} object that will be used for all Android specific
	 * calls, such as {@link Service} binding.
	 */
	private Context context;

	/**
	 * The instance of {@link HttpService} that is used for issuing
	 * {@link WebRequest}s.
	 */
	private HttpService httpService;

	/**
	 * Contains the "active" {@link ServiceConnection} to {@link HttpService}.
	 * Can be null if {@link HttpService} was unbound.
	 */
	private ServiceConnection activeServiceConnection;

	/**
	 * The template {@link ServiceConnection} used as
	 * {@link HttpServiceAssister#activeServiceConnection}, can be exchanged
	 * with a custom implementation.
	 */
	private ServiceConnection standardServiceConnection;

	/**
	 * This {@link Queue} holds all asynchronous {@link WebRequest}s that need
	 * to be processed once a connection to {@link HttpService} has been
	 * established.
	 */
	private final Queue<WebRequestInformation> pendingWebRequests;

	/**
	 * This flag is set by {@link HttpServiceAssister#savlyUnbindService()}. It
	 * ensures that pending {@link WebRequest} will be executed prior to
	 * unbinding the {@link HttpService}
	 */
	private final AtomicBoolean unbindServiceAfterWebRequestExecution = new AtomicBoolean(false);

	/**
	 * This flag indicates if synchronous {@link WebRequest}s can currently be
	 * processed. Is false when {@link HttpServiceAssister#bindService()} has
	 * not been called or if {@link HttpServiceAssister#unbindService()} has
	 * been called.
	 */
	private final AtomicBoolean synchronousWebRequestPossible = new AtomicBoolean(false);

	/**
	 * This monitor {@link Object} is used to wait for a connection to
	 * {@link HttpService} when running a synchronous {@link WebRequest}.
	 */
	private final Object monitor = new Object();

	private final NonTimeCriticalTaskManager nonTimeCriticalTaskManager;

	/**
	 * Default constructor. Will use
	 * {@link NonTimeCriticalTaskQueueDefaultConfiguration} to configure the
	 * behaviour of {@link NonTimeCriticalTask} processing.
	 * 
	 * @param context
	 *            a {@link Context} object.
	 */
	public HttpServiceAssister(Context context) {
		this.context = context;
		this.pendingWebRequests = new LinkedList<WebRequestInformation>();
		this.nonTimeCriticalTaskManager = new NonTimeCriticalTaskManager(
				new NonTimeCriticalTaskQueueConfigurationDefaultFactory().newInstance(), this);
		initBindingTimeout();
	}

	/**
	 * Alternative constructor
	 * 
	 * @param context
	 *            a {@link Context} object.
	 * @param factory
	 *            the factory used to construct a configuration for
	 *            {@link NonTimeCriticalTask} processing.
	 */
	public HttpServiceAssister(Context context, NonTimeCriticalTaskQueueConfigurationFactory factory) {
		this.context = context;
		this.pendingWebRequests = new LinkedList<WebRequestInformation>();
		this.nonTimeCriticalTaskManager = new NonTimeCriticalTaskManager(factory.newInstance(), this);
		initBindingTimeout();
	}

	private void initBindingTimeout() {
		syncRequestBindingTimeout = Debug.isDebuggerConnected() ? SYNC_REQUEST_BINDING_TIMEOUT_DEBUG : SYNC_REQUEST_BINDING_TIMEOUT;
	}

	/**
	 * Starts {@link HttpService} binding process. Will use
	 * {@link HttpServiceAssister#standardServiceConnection} if no custom
	 * {@link ServiceConnection} has been supplied.
	 * 
	 * @return <code>true</code> if the connection to {@link HttpService} could
	 *         be established, <code>false</code> if the service has already
	 *         been bound or if binding is impossible.
	 */
	public boolean bindService() {
		if (!AndroidUtils.getInstance().isServiceAvailable(context, HttpService.class)) {
			throw new RuntimeException("You forgot to register the HttpService in your manifest!");
		}
		synchronousWebRequestPossible.set(true);
		if (standardServiceConnection == null) {
			standardServiceConnection = new HttpServiceAssisterConnection();
		}
		if (activeServiceConnection == null) {
			return context.bindService(new Intent(context, HttpService.class), activeServiceConnection = standardServiceConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			return false;
		}
	}

	/**
	 * Unbinds the {@link HttpService}. All pending {@link WebRequest} will be
	 * discarded!
	 * 
	 * @return <code>true</code> if {@link HttpService} was bound and therefore
	 *         successfully unbound, <code>false</code> otherwise.
	 */
	public boolean unbindService() {
		synchronousWebRequestPossible.set(false);
		if (activeServiceConnection != null) {
			unbindServiceAfterWebRequestExecution.set(false);
			context.unbindService(activeServiceConnection);
			activeServiceConnection = null;
			httpService = null;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Unbinds the {@link HttpService}. Pending {@link WebRequest}s will be
	 * executed before unbinding.
	 * 
	 * @return
	 */
	public boolean safelyUnbindService() {
		synchronousWebRequestPossible.set(false);
		if (activeServiceConnection != null) {
			if (hasPendingAsyncWebRequests()) {
				unbindServiceAfterWebRequestExecution.set(true);
			} else {
				context.unbindService(activeServiceConnection);
				activeServiceConnection = null;
				httpService = null;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Runs a {@link WebRequest} asynchronously
	 * 
	 * @param callback
	 *            the {@link Handler.Callback} that will be informed once the
	 *            {@link WebRequest} has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should be used to process
	 *            the {@link WebRequest}
	 */
	public void runWebRequest(Handler.Callback callback, WebRequest webRequest, ServiceProcessor<?> serviceProcessor) {
		runWebRequest(new Handler(callback), webRequest, serviceProcessor, null);
	}

	/**
	 * Runs a {@link WebRequest} asynchronously
	 * 
	 * @param callback
	 *            the {@link Handler.Callback} that will be informed once the
	 *            {@link WebRequest} has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should be used to process
	 *            the {@link WebRequest}
	 * @param progressListener
	 *            a {@link ProgressListener} that will be informed of download
	 *            progress
	 */
	public void runWebRequest(Handler.Callback callback, WebRequest webRequest, ServiceProcessor<?> serviceProcessor,
			DownloadProgressListener progressListener) {
		runWebRequest(new Handler(callback), webRequest, serviceProcessor, progressListener);
	}

	/**
	 * Runs a {@link WebRequest} asynchronously
	 * 
	 * @param handler
	 *            the handler that will be informed once the {@link WebRequest}
	 *            has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should be used to process
	 *            the {@link WebRequest}
	 */
	public void runWebRequest(Handler handler, WebRequest webRequest, ServiceProcessor<?> serviceProcessor) {
		runWebRequest(handler, webRequest, serviceProcessor, null);
	}

	/**
	 * Runs a {@link WebRequest} asynchronously
	 * 
	 * @param handler
	 *            the handler that will be informed once the {@link WebRequest}
	 *            has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should be used to process
	 *            the {@link WebRequest}
	 * @param progressListener
	 *            a {@link ProgressListener} that will be informed of download
	 *            progress
	 */
	public void runWebRequest(Handler handler, WebRequest webRequest, ServiceProcessor<?> serviceProcessor,
			DownloadProgressListener progressListener) {
		if (httpService == null) {
			LOGGER.info("httpService is null, appending WebRequest to queue for later processing: " + webRequest);
			addWebRequestToQueue(handler, webRequest, progressListener, serviceProcessor);
		} else {
			runTimeCriticalAsynchronousWebRequest(handler, webRequest, serviceProcessor, progressListener);
			runNonTimeCriticalTasksIfRequired();
		}
	}

	/**
	 * Runs a non time critical {@link WebRequest}. Make sure that the
	 * {@link WebRequest} you are trying to run using this method implements
	 * {@link NonTimeCriticalTask} and that {@link WebRequest#isTimeCritical()}
	 * returns <code>false</code>
	 * 
	 * @param wr
	 *            the {@link WebRequest} to queue
	 */
	public void runNonTimeCriticalWebRequest(WebRequest wr) {
		if (wr.isTimeCritical() || !(wr instanceof NonTimeCriticalTask)) {
			throw new IllegalArgumentException("WebRequest is time critical or not an instance of NonTimeCriticalTask");
		}
		nonTimeCriticalTaskManager.put((NonTimeCriticalTask) wr);
	}

	/**
	 * Execute all {@link NonTimeCriticalTask}s even if the configuration
	 * treshold has not been hit
	 */
	public void forceProcessAllNonTimeCriticalWebRequests() {
		nonTimeCriticalTaskManager.runTasks(context);
	}

	/**
	 * Runs a time critical {@link WebRequest}
	 * 
	 * @param handler
	 *            the handler that will be informed once the {@link WebRequest}
	 *            has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should be used to process
	 *            the {@link WebRequest}
	 * @param progressListener
	 *            a {@link ProgressListener} that will be informed of download
	 *            progress
	 */
	private void runTimeCriticalAsynchronousWebRequest(Handler handler, WebRequest webRequest, ServiceProcessor<?> serviceProcessor,
			DownloadProgressListener progressListener) {
		LOGGER.info("httpService is ready, running WebRequest directly");
		if (!httpService.isProcessorRegistered(serviceProcessor.getProcessorID())) {
			httpService.registerProcessor(serviceProcessor);
		}
		httpService.runWebRequest(handler, webRequest, progressListener);
	}

	/**
	 * Runs all {@link NonTimeCriticalTask}s, if the provided
	 * {@link NonTimeCriticalTaskQueueConfiguration} matches the state of the
	 * queue
	 */
	private void runNonTimeCriticalTasksIfRequired() {
		nonTimeCriticalTaskManager.runTasksIfNecessary(context);
	}

	/**
	 * Executes a {@link WebRequest} on the same {@link Thread} this method is
	 * called. Beware that calling
	 * {@link HttpService#runSynchronousWebRequest(WebRequest,DownloadProgressListener)}
	 * on the main thread may cause ANR issues. The processor handling the
	 * {@link WebRequest} must be a {@link DataProcessor}, otherwise, an
	 * exception will be thrown.
	 * 
	 * WARNING: Unlike {@link HttpService#runSynchronousWebRequest(WebRequest)},
	 * this method must be executed from a thread that is not the Main (UI)
	 * thread. Calling this method from the Main (UI) thread will always result
	 * in a timeout! This behaviour is due to technical constraints related to
	 * service binding.
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param serviceProcessor
	 *            the processor that handles the {@link WebRequest}
	 * @return The object created by the
	 *         {@link DataProcessor#obtainDataObjectFromWebReply(ReplyAdapter)}
	 *         method of the {@link DataProcessor} registered for this request,
	 *         or <code>null</code> if the request failed.
	 */
	public WebRequestReturnContainer runSynchronousWebRequest(WebRequest webRequest, ServiceProcessor<?> serviceProcessor) {
		prepareForSyncRequest(serviceProcessor);
		return httpService.runSynchronousWebRequest(webRequest);
	}

	/**
	 * Executes a {@link WebRequest} on the same {@link Thread} this method is
	 * called. Beware that calling
	 * {@link HttpService#runSynchronousWebRequest(WebRequest,DownloadProgressListener)}
	 * on the main thread may cause ANR issues. The processor handling the
	 * {@link WebRequest} must be a {@link DataProcessor}, otherwise, an
	 * exception will be thrown.
	 * 
	 * WARNING: Unlike {@link HttpService#runSynchronousWebRequest(WebRequest)},
	 * this method must be executed from a thread that is not the Main (UI)
	 * thread. Calling this method from the Main (UI) thread will always result
	 * in a timeout! This behaviour is due to technical constraints related to
	 * service binding.
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param progressListener
	 *            an optional {@link ProgressListener}
	 * @param serviceProcessor
	 *            the processor that handles the {@link WebRequest}
	 * @return The object created by the
	 *         {@link DataProcessor#obtainDataObjectFromWebReply(ReplyAdapter)}
	 *         method of the {@link DataProcessor} registered for this request,
	 *         or <code>null</code> if the request failed.
	 */
	public WebRequestReturnContainer runSynchronousWebRequest(WebRequest webRequest, ServiceProcessor<?> serviceProcessor,
			DownloadProgressListener progressListener) {
		prepareForSyncRequest(serviceProcessor);
		return httpService.runSynchronousWebRequest(webRequest, progressListener);
	}

	/**
	 * This method should be used to check if synchronous {@link WebRequest} can
	 * be executed without causing a service binding timeout. Calls to
	 * {@link HttpServiceAssister#runSynchronousWebRequest(WebRequest, ServiceProcessor)}
	 * and
	 * {@link HttpServiceAssister#runSynchronousWebRequest(WebRequest, ServiceProcessor, DownloadProgressListener)}
	 * can result in service binding timeouts when
	 * {@link HttpServiceAssister#bindService()} has not been called yet or if
	 * {@link HttpServiceAssister#unbindService()} was called before the
	 * synchronous {@link WebRequest} is issued. While this method will protect
	 * against service binding timeout exceptions, it does not indicate whether
	 * a service binding to {@link HttpService} is active.
	 * 
	 * @return <code>true</code> if
	 *         {@link HttpServiceAssister#runSynchronousWebRequest(WebRequest, ServiceProcessor)}
	 *         or
	 *         {@link HttpServiceAssister#runSynchronousWebRequest(WebRequest, ServiceProcessor, DownloadProgressListener)}
	 *         can be called without causing service binding timeouts
	 */
	public boolean synchronousWebRequestPossible() {
		return synchronousWebRequestPossible.get();
	}

	/**
	 * Checks if there are still {@link WebRequest}s to be processed
	 * 
	 * @return <code>true</code> if there are still {@link WebRequest}s in the
	 *         {@link Queue}, <code>false</code> otherwise.
	 */
	public boolean hasPendingAsyncWebRequests() {
		return pendingWebRequests.size() != 0;
	}

	/**
	 * Waits for {@link HttpService} to be bound. This method will NOT
	 * initialize binding, use {@link HttpServiceAssister#bindService()} before
	 * calling this method.
	 * 
	 * @return <code>true</code> if {@link HttpService} was bound,
	 *         <code>false</code> if the timeout was reached
	 */
	private boolean waitForHttpService() {
		LOGGER.info("Waiting on thread " + Thread.currentThread().getId());
		if (httpService != null) {
			return true;
		}
		synchronized (monitor) {
			try {
				monitor.wait(SYNC_REQUEST_BINDING_TIMEOUT);
				return httpService != null;
			} catch (Throwable tr) {
				LOGGER.debug("Thread Interruption", tr);
				return httpService != null;
			}
		}
	}

	/**
	 * Checks precondions for synchronous {@link WebRequest}s
	 * 
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} used for the {@link WebRequest}
	 */
	private void prepareForSyncRequest(ServiceProcessor<?> serviceProcessor) {
		if (!(serviceProcessor instanceof SynchronousProcessor<?>)) {
			throw new ServiceException("Supplied processor was no SynchronousProcessor");
		}
		if (!waitForHttpService()) {
			throw new ServiceException("Timeout reached while waiting for service binding");
		}
		if (httpService == null) {
			throw new ServiceException("httpService was null!");
		}
		if (!httpService.isProcessorRegistered(serviceProcessor.getProcessorID())) {
			httpService.registerProcessor(serviceProcessor);
		}
	}

	/**
	 * Adds an asynchronous {@link WebRequest} to the
	 * {@link HttpServiceAssister#pendingWebRequests} {@link Queue}.
	 * 
	 * @param handler
	 * @param webRequest
	 * @param progressListener
	 * @param serviceProcessor
	 */
	private void addWebRequestToQueue(Handler handler, WebRequest webRequest, DownloadProgressListener progressListener,
			ServiceProcessor<?> serviceProcessor) {
		synchronized (pendingWebRequests) {
			pendingWebRequests.add(new WebRequestInformation(handler, webRequest, progressListener, serviceProcessor));
		}
	}

	/**
	 * Sets the standard {@link ServiceConnection} that is used to bind to
	 * {@link HttpService}. This method has to be called before
	 * {@link HttpServiceAssister#bindService()} in order to have any effect.
	 * 
	 * @param standardServiceConnection
	 *            the connection to use
	 */
	public void setStandardServiceConnection(ServiceConnection standardServiceConnection) {
		this.standardServiceConnection = standardServiceConnection;
	}

	/**
	 * Default {@link ServiceConnection} implementation for
	 * {@link HttpServiceAssister}. Override this class if you wish to provide a
	 * custom implementation. Make sure to call
	 * super.onServiceConnected(ComponentName, IBinder) in
	 * {@link HttpServiceAssisterConnection#onServiceConnected(ComponentName, IBinder)}
	 * or implement the logic manually.
	 */
	public class HttpServiceAssisterConnection implements ServiceConnection {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (!(service instanceof HttpServiceBinder)) {
				throw new IllegalArgumentException("Binder must be of type HttpServiceBinder");
			}
			httpService = ((HttpServiceBinder) service).getHttpService();
			synchronized (monitor) {
				monitor.notify();
			}
			WebRequestInformation webRequestInformation;
			synchronized (pendingWebRequests) {
				while ((webRequestInformation = pendingWebRequests.poll()) != null && httpService != null) {
					if (!httpService.isProcessorRegistered(webRequestInformation.serviceProcessor.getProcessorID())) {
						httpService.registerProcessor(webRequestInformation.serviceProcessor);
					}
					LOGGER.debug("Running " + webRequestInformation.webRequest + " after service binding!");
					httpService.runWebRequest(webRequestInformation.handler, webRequestInformation.webRequest,
							webRequestInformation.progressListener);
				}
				if (unbindServiceAfterWebRequestExecution.get()) {
					unbindService();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	}

	/**
	 * Adds a listener that will be notified once a {@link NonTimeCriticalTask}
	 * is being processed. ATTENTION: Make sure to call
	 * {@link HttpServiceAssister#removeNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener)}
	 * when the listener is no longer needed!
	 * 
	 * @param nonTimeCriticalTaskProcessingListener
	 *            a listener that receives updates when a task has been started.
	 *            This listener is not intrinsic to this instance of
	 *            {@link HttpServiceAssister} or
	 *            {@link NonTimeCriticalTaskManager} for that matter. It will
	 *            receive notifications whenever a task is started, even if the
	 *            task was not started using this instance of
	 *            {@link HttpServiceAssister}. In addition, make sure that the
	 *            listener does not hold a reference to a {@link Context}, as
	 *            this can cause memory leaks (you can have references to
	 *            {@link Context} {@link Object}s if you remove the listener as
	 *            soon as the {@link Context} is invalidated!).
	 */
	public void addNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener nonTimeCriticalTaskProcessingListener) {
		nonTimeCriticalTaskManager.addNonTimeCriticalTaskProcessingListener(nonTimeCriticalTaskProcessingListener);
	}

	/**
	 * Removes the provided listener. ATTENTION: Make sure to call
	 * {@link HttpServiceAssister#removeNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener)}
	 * when the listener is no longer needed!
	 * 
	 * 
	 * @param nonTimeCriticalTaskProcessingListener
	 *            the listener
	 */
	public void removeNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener nonTimeCriticalTaskProcessingListener) {
		nonTimeCriticalTaskManager.removeNonTimeCriticalTaskProcessingListener(nonTimeCriticalTaskProcessingListener);
	}

	/**
	 * Data {@link Object} for information regarding {@link WebRequest}s.
	 */
	private static final class WebRequestInformation {

		private Handler handler;
		private WebRequest webRequest;
		private ServiceProcessor<?> serviceProcessor;
		private DownloadProgressListener progressListener;

		public WebRequestInformation(Handler handler, WebRequest webRequest, DownloadProgressListener progressListener,
				ServiceProcessor<?> serviceProcessor) {
			this.handler = handler;
			this.webRequest = webRequest;
			this.progressListener = progressListener;
			this.serviceProcessor = serviceProcessor;
		}
	}

}
