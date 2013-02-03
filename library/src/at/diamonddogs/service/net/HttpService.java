/*
 * Copyright (C) 2012 the diamond:dogs|group
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RecoverySystem.ProgressListener;
import android.util.SparseArray;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.exception.ProcessorExeception;
import at.diamonddogs.exception.ServiceException;
import at.diamonddogs.net.WebClient;
import at.diamonddogs.net.WebClient.DownloadProgressListener;
import at.diamonddogs.net.WebClient.WebClientReplyListener;
import at.diamonddogs.net.WebClientFactory;
import at.diamonddogs.service.processor.DataProcessor;
import at.diamonddogs.service.processor.ServiceProcessor;
import at.diamonddogs.service.processor.SynchronousProcessor;
import at.diamonddogs.util.CacheManager;
import at.diamonddogs.util.CacheManager.CachedObject;
import at.diamonddogs.util.WorkerQueue;

/**
 * The central {@link Service} used to process {@link WebRequest}s
 */
public class HttpService extends Service implements WebClientReplyListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpService.class);

	/**
	 * The core thread pool size, refer to {@link ThreadPoolExecutor} for more
	 * information
	 */
	private static final int POOL_SIZE_CORE = 10;

	/**
	 * The maximum thread pool size, refer to {@link ThreadPoolExecutor} for
	 * more information
	 */
	private static final int POOL_SIZE_MAX = 20;

	/**
	 * The thread pool keep alive time, refer to {@link ThreadPoolExecutor} for
	 * more information
	 */
	private static final int POOL_KEEPALIVE = 3000;

	/**
	 * Contains all registered processors
	 */
	private SparseArray<ServiceProcessor<?>> registeredProcessors;

	/**
	 * Maps handler to {@link WebRequest}s
	 */
	private Map<Handler, List<WebRequest>> webRequestHandlerMap;

	/**
	 * {@link WorkerQueue} for threaded (async) {@link WebRequest} processing
	 */
	private WorkerQueue workerQueue;

	/**
	 * {@link Binder} instance for {@link HttpService} the default
	 * {@link Binder} will return the {@link HttpService}
	 */
	private IBinder binder;

	/**
	 * A map storing {@link WebRequest}s
	 */
	private Map<String, WebRequestFutureContainer> webRequests;

	@Override
	public void onCreate() {
		super.onCreate();
		workerQueue = new WorkerQueue(POOL_SIZE_CORE, POOL_SIZE_MAX, POOL_KEEPALIVE);
		webRequestHandlerMap = Collections.synchronizedMap(new HashMap<Handler, List<WebRequest>>());
		registeredProcessors = new SparseArray<ServiceProcessor<?>>();
		webRequests = Collections.synchronizedMap(new HashMap<String, WebRequestFutureContainer>());
	}

	@Override
	public IBinder onBind(Intent intent) {
		LOGGER.debug("onBind, Intent: " + intent == null ? "null" : intent.toString());
		return binder == null ? binder = new HttpServiceBinder() : binder;
	}

	@Override
	public void onDestroy() {
		LOGGER.debug("onDestroy");
		workerQueue.shutDown();
		webRequestHandlerMap.clear();
		registeredProcessors.clear();
		super.onDestroy();
	}

	/**
	 * Convenience method that calls
	 * {@link HttpService#runWebRequest(Handler, WebRequest, DownloadProgressListener)}
	 * with a <code>null</code> {@link ProgressListener}
	 * 
	 * @param handler
	 *            the handler that will be informed once the {@link WebRequest}
	 *            has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @return
	 */
	public String runWebRequest(Handler handler, WebRequest webRequest) {
		return runWebRequest(handler, webRequest, null);
	}

	/**
	 * Runs a {@link WebRequest} asynchronously
	 * 
	 * @param handler
	 *            the handler that will be informed once the {@link WebRequest}
	 *            has been completed
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param progressListener
	 *            a {@link ProgressListener} that will be informed of download
	 *            progress
	 * @return the {@link WebRequest} id as {@link String}
	 */
	public String runWebRequest(final Handler handler, final WebRequest webRequest, final DownloadProgressListener progressListener) {
		if (handler == null) {
			throw new IllegalArgumentException("handler may not be null");
		}

		if (workerQueue.isShutDown()) {
			LOGGER.info("service already shutdown, couldn't run: " + webRequest);
			return null;
		}

		if (webRequest == null) {
			return null;
		}

		addRequestToHandlerMap(handler, webRequest);
		ServiceProcessor<?> processor = registeredProcessors.get(webRequest.getProcessorId());
		if (processor == null) {
			int id = webRequest.getProcessorId();
			if (id == -1) {
				throw new ServiceException("processor id == -1 looks like you forgot to set a Processor ID for the WebRequest");
			}
			throw new ServiceException("No processor with id '" + id + "' has been registered!");
		}
		if (webRequest.getUrl() == null) {
			ReplyAdapter ra = new ReplyAdapter();
			ra.setRequest(webRequest);
			ra.setStatus(ReplyAdapter.Status.FAILED);
			ra.setThrowable(new IllegalArgumentException("WebRequest URL was null"));
			dispatchWebReplyProcessor(ra, getHandler(ra.getRequest()));
		}

		Runnable r = new Runnable() {
			@Override
			public void run() {
				Future<?> future = getWebRequestTask(webRequest, progressListener, true);
				if (future != null) {
					webRequests.put(webRequest.getId(), new WebRequestFutureContainer(webRequest, future));
				}
			}
		};
		Thread t = new Thread(r);
		t.start();

		return webRequest.getId();
	}

	/**
	 * Executes an array of {@link WebRequest} synchronously using
	 * {@link HttpService#runSynchronousWebRequest(WebRequest)}.
	 * 
	 * @param webRequests
	 *            the {@link WebRequest}s to run
	 * @return an array of length webRequest.length containing return objects
	 */
	public Object[] runSynchronousWebRequests(WebRequest[] webRequests) {
		return runSynchronousWebRequests(webRequests, new DownloadProgressListener[0]);
	}

	/**
	 * Executes an array of {@link WebRequest} synchronously using
	 * {@link HttpService#runSynchronousWebRequest(WebRequest)}.
	 * 
	 * @param webRequests
	 *            the {@link WebRequest}s to run
	 * @param progressListener
	 *            a single {@link DownloadProgressListener} that will receive
	 *            callbacks from each {@link WebRequest}. Every time a new
	 *            {@link WebRequest} is started,
	 *            {@link DownloadProgressListener#downloadSize(long)} is called
	 * @return an array of length webRequest.length containing return objects
	 */
	public Object[] runSynchronousWebRequests(WebRequest[] webRequests, DownloadProgressListener progressListener) {
		return runSynchronousWebRequests(webRequests, new DownloadProgressListener[] { progressListener });
	}

	/**
	 * Executes an array of {@link WebRequest} synchronously using
	 * {@link HttpService#runSynchronousWebRequest(WebRequest)}.
	 * 
	 * @param webRequests
	 *            the {@link WebRequest}s to run
	 * @param progressListeners
	 *            an array of {@link DownloadProgressListener} that will receive
	 *            callbacks from their corresponding {@link WebRequest}
	 * @return an array of results
	 */
	public Object[] runSynchronousWebRequests(WebRequest[] webRequests, DownloadProgressListener[] progressListeners) {
		Object[] ret = new Object[webRequests.length];
		CacheManager cm = CacheManager.getInstance();
		for (int i = 0; i < webRequests.length; i++) {
			CachedObject cachedObject = cm.getFromCache(this, webRequests[i]);
			if (cachedObject != null) {
				ret[i] = cachedObject.getCachedObject();
			} else if (progressListeners.length == 0) {
				ret[i] = runSynchronousWebRequest(webRequests[i], null);
			} else if (progressListeners.length == 1) {
				ret[i] = runSynchronousWebRequest(webRequests[i], progressListeners[0]);
			} else if (progressListeners.length != webRequests.length) {
				throw new ServiceException("progressListeners.length must be 0, 1 or equal to webRequest.length");
			} else {
				ret[i] = runSynchronousWebRequest(webRequests[i], progressListeners[i]);
			}

		}
		return ret;
	}

	/**
	 * Executes a {@link WebRequest} on the same {@link Thread} this method is
	 * called. Beware that calling
	 * {@link HttpService#runSynchronousWebRequest(WebRequest,DownloadProgressListener)}
	 * on the main thread may cause ANR issues. The processor handling the
	 * {@link WebRequest} must be a {@link SynchronousProcessor}, otherwise, an
	 * exception will be thrown.
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @return The object created by the
	 *         {@link DataProcessor#obtainDataObjectFromWebReply(ReplyAdapter)}
	 *         method of the {@link DataProcessor} registered for this request,
	 *         or <code>null</code> if the request failed.
	 */
	public Object runSynchronousWebRequest(final WebRequest webRequest) {
		return runSynchronousWebRequest(webRequest, null);
	}

	/**
	 * Executes a {@link WebRequest} on the same {@link Thread} this method is
	 * called. Beware that calling
	 * {@link HttpService#runSynchronousWebRequest(WebRequest,DownloadProgressListener)}
	 * on the main thread may cause ANR issues. The processor handling the
	 * {@link WebRequest} must be a {@link SynchronousProcessor}, otherwise, an
	 * exception will be thrown.
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} to run
	 * @param progressListener
	 *            an optional {@link ProgressListener}
	 * @return The object created by the
	 *         {@link DataProcessor#obtainDataObjectFromWebReply(ReplyAdapter)}
	 *         method of the {@link DataProcessor} registered for this request,
	 *         or <code>null</code> if the request failed.
	 */
	public Object runSynchronousWebRequest(final WebRequest webRequest, final DownloadProgressListener progressListener) {
		SynchronousProcessor<?> synchronousProcessor = (SynchronousProcessor<?>) registeredProcessors.get(webRequest.getProcessorId());
		try {
			CacheManager cm = CacheManager.getInstance();
			CachedObject cachedObject = cm.getFromCache(HttpService.this, webRequest);
			if (cachedObject != null) {
				return cachedObject.getCachedObject();
			} else {
				return synchronousProcessor
						.obtainDataObjectFromWebReply(runSynchronousWebRequestFuture(webRequest, progressListener).get());
			}
		} catch (Throwable tr) {
			LOGGER.error("Error getting result", tr);
		}
		return null;
	}

	private Future<ReplyAdapter>[] runSynchronousWebRequestsFuture(WebRequest[] webRequests) {
		return runSynchronousWebRequestsFuture(webRequests, new DownloadProgressListener[0]);
	}

	private Future<ReplyAdapter>[] runSynchronousWebRequestsFuture(WebRequest[] webRequests, DownloadProgressListener progressListener) {
		return runSynchronousWebRequestsFuture(webRequests, new DownloadProgressListener[] { progressListener });
	}

	private Future<ReplyAdapter>[] runSynchronousWebRequestsFuture(WebRequest[] webRequests, DownloadProgressListener[] progressListeners) {
		@SuppressWarnings("unchecked")
		Future<ReplyAdapter>[] ret = new Future[webRequests.length];
		for (int i = 0; i < webRequests.length; i++) {
			if (progressListeners.length == 0) {
				ret[i] = runSynchronousWebRequestFuture(webRequests[i], null);
			} else if (progressListeners.length == 1) {
				ret[i] = runSynchronousWebRequestFuture(webRequests[i], progressListeners[0]);
			} else if (progressListeners.length != webRequests.length) {
				throw new ServiceException("progressListeners.length must be 0, 1 or equal to webRequest.length");
			} else {
				ret[i] = runSynchronousWebRequestFuture(webRequests[i], progressListeners[i]);
			}

		}
		return ret;
	}

	protected Future<ReplyAdapter> runSynchronousWebRequestFuture(final WebRequest webRequest) {
		return runSynchronousWebRequestFuture(webRequest, null);
	}

	protected Future<ReplyAdapter> runSynchronousWebRequestFuture(final WebRequest webRequest,
			final DownloadProgressListener progressListener) {

		if (workerQueue.isShutDown()) {
			LOGGER.info("service already shutdown, couldn't run: " + webRequest);
			return null;
		}

		if (webRequest == null) {
			return null;
		}

		ServiceProcessor<?> processor = registeredProcessors.get(webRequest.getProcessorId());
		if (processor == null) {
			int id = webRequest.getProcessorId();
			if (id == -1) {
				throw new ServiceException("processor id == -1 looks like you forgot to set a Processor ID for the WebRequest");
			}
			throw new ServiceException("No processor with id '" + id + "' has been registered!");
		}
		if (webRequest.getUrl() == null) {
			return null;
		}

		if (processor instanceof SynchronousProcessor<?>) {
			try {
				Future<ReplyAdapter> future = getWebRequestTask(webRequest, progressListener, false);
				if (future != null) {
					webRequests.put(webRequest.getId(), new WebRequestFutureContainer(webRequest, future));
					return future;
				}
			} catch (Throwable tr) {
				LOGGER.error("Error getting result!", tr);
			}
		} else {
			throw new ServiceException("Invalid ServiceProcessor for sync WebRequest, only instances of SynchronousProcessor are allowed");
		}
		return null;
	}

	private void addRequestToHandlerMap(Handler handler, WebRequest webRequest) {
		synchronized (webRequestHandlerMap) {
			List<WebRequest> requestList = webRequestHandlerMap.get(handler);
			if (requestList == null) {
				requestList = new LinkedList<WebRequest>();

				webRequestHandlerMap.put(handler, requestList);
			}
			requestList.add(webRequest);
		}
	}

	private Handler getHandler(Request webRequest) {
		synchronized (webRequestHandlerMap) {
			Iterator<Handler> iterator = webRequestHandlerMap.keySet().iterator();
			while (iterator.hasNext()) {
				Handler h = iterator.next();
				List<WebRequest> requestList = webRequestHandlerMap.get(h);
				if (requestList.contains(webRequest)) {
					requestList.remove(webRequest);
					if (requestList.isEmpty()) {
						iterator.remove();
					}
					return h;
				}
			}
		}
		return null;
	}

	private WebClient getNewWebClient(WebRequest webRequest, DownloadProgressListener downloadProgressListener) {
		WebClient client = null;

		WebClientFactory f = WebClientFactory.getInstance();
		client = f.getNetworkClient(webRequest, this);

		if (downloadProgressListener != null) {
			client.setDownloadProgressListener(downloadProgressListener);
		}
		client.setListener(this);
		client.setWebRequest(webRequest);
		return client;
	}

	/**
	 * Register a {@link ServiceProcessor} with the the {@link HttpService}.
	 * Make sure that the {@link ServiceProcessor} that should be registered is
	 * not registered with {@link HttpService} already by using
	 * {@link HttpService#isProcessorRegistered(int)}
	 * 
	 * @param processor
	 *            an instance of the {@link ServiceProcessor} to register
	 */
	public void registerProcessor(ServiceProcessor<?> processor) {
		int processorId = processor.getProcessorID();
		if (isProcessorRegistered(processorId)) {
			if (registeredProcessors.get(processorId).getClass() != processor.getClass()) {
				throw new ProcessorExeception("Processor id collision, processorids for " + processor + " and "
						+ registeredProcessors.get(processorId) + " are identical!");
			} else {
				throw new ProcessorExeception("A processor known by id " + processorId + " has already been registered.");
			}
		}
		registeredProcessors.put(processorId, processor);
	}

	/**
	 * Unregisters a {@link ServiceProcessor} known by the given id
	 * 
	 * @param id
	 *            the id of the {@link ServiceProcessor} to unregister
	 */
	public void unregisterProcessor(int id) {
		registeredProcessors.remove(id);
	}

	/**
	 * Checks if a {@link ServiceProcessor} has been registered with
	 * {@link HttpService}
	 * 
	 * @param id
	 *            the id of the {@link ServiceProcessor}
	 * @return <code>true</code> if the {@link ServiceProcessor} known by id has
	 *         been registered <code>false</code> otherwise
	 */
	public boolean isProcessorRegistered(int id) {
		return (registeredProcessors.indexOfKey(id) >= 0);
	}

	/**
	 * Returns the {@link ServiceProcessor} known by id
	 * 
	 * @param id
	 *            the id of the {@link ServiceProcessor} to obtain
	 * @return a {@link ServiceProcessor}
	 */
	public ServiceProcessor<?> getRegisteredProcessorById(int id) {
		return registeredProcessors.get(id);
	}

	@Override
	public void onWebReply(WebClient webClient, ReplyAdapter reply) {
		LOGGER.debug("onWebReply: " + reply.getStatus() + " from: " + reply.getRequest().getUrl());
		webRequests.remove(webClient.getWebRequest().getId());
		dispatchWebReplyProcessor(reply, getHandler(reply.getRequest()));
	}

	private void dispatchCachedObjectToProcessor(CachedObject cachedObject, Request webRequest) {
		if (!workerQueue.isShutDown()) {
			getProcessor(webRequest).processCachedObject(cachedObject, getHandler(webRequest), webRequest);
		} else {
			LOGGER.debug("service already shutdown, ignoring response from cache");
		}
	}

	private void dispatchWebReplyProcessor(ReplyAdapter replyAdaper, Handler handler) {
		try {
			if (!workerQueue.isShutDown()) {
				getProcessor(replyAdaper.getRequest()).processWebReply(this.getApplicationContext(), replyAdaper, handler);
			} else {
				LOGGER.debug("service already shutdown, ignoring web response");
			}
		} catch (Exception e) {
			LOGGER.error("Error while processing web reply", e);
		}
	}

	private ServiceProcessor<?> getProcessor(Request webRequest) {
		int processorId = ((WebRequest) webRequest).getProcessorId();
		ServiceProcessor<?> processor = registeredProcessors.get(processorId);
		if (processor == null) {
			throw new ProcessorExeception("No processor with id " + processorId);
		}
		LOGGER.debug("found processor with id: " + processorId);
		return processor;
	}

	/**
	 * Convenience binding method
	 * 
	 * @param context
	 *            a {@link Context}
	 * @param serviceConnection
	 *            a {@link ServiceConnection}
	 */
	public static void bindService(Context context, ServiceConnection serviceConnection) {
		Intent intent = new Intent(context, HttpService.class);
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Cancels a {@link Future} {@link WebRequest} by id
	 * 
	 * @param id
	 *            the id of the {@link WebRequest} to cancel
	 */
	public void cancelRequest(String id) {
		LOGGER.debug("cancelRequest " + id);
		if (webRequests.containsKey(id)) {
			LOGGER.debug("found cancelRequest " + id);
			WebRequestFutureContainer container = webRequests.get(id);
			boolean hasBeenCanceled = container.future.cancel(true);
			LOGGER.info("WebRequest with id " + id + " has been canceled " + hasBeenCanceled);
			container.webRequest.setCancelled(true);
			webRequests.remove(id);
		}
	}

	private Future<ReplyAdapter> getWebRequestTask(WebRequest webRequest, DownloadProgressListener downloadProgressListener, boolean async) {
		CacheManager cm = CacheManager.getInstance();
		Future<ReplyAdapter> ret = null;
		WebClient client = null;
		try {
			CachedObject cachedObject = cm.getFromCache(HttpService.this, webRequest);
			if (cachedObject == null) {
				LOGGER.debug("No cached objects available for: " + webRequest.getUrl());
				client = getNewWebClient(webRequest, downloadProgressListener);
				if (!async) {
					client.setListener(null);
				}
				ret = workerQueue.<ReplyAdapter> runCancelableTask(client);
			} else {
				LOGGER.debug("File found in file cache: " + webRequest.getUrl());
				if (!webRequest.isCancelled()) {
					dispatchCachedObjectToProcessor(cachedObject, webRequest);
				}
			}
		} catch (Throwable tr) {
			LOGGER.debug("No cached objects available for: " + webRequest.getUrl());
			client = getNewWebClient(webRequest, downloadProgressListener);
			if (!async) {
				client.setListener(null);
			}
			ret = workerQueue.<ReplyAdapter> runCancelableTask(client);
		}
		return ret;
	}

	/**
	 * Default {@link Binder} implementation
	 */
	public final class HttpServiceBinder extends Binder {
		/**
		 * Gets the {@link HttpService}
		 * 
		 * @return the {@link HttpService}
		 */
		public HttpService getHttpService() {
			return HttpService.this;
		}
	}

	private static final class WebRequestFutureContainer {
		private WebRequest webRequest;
		private Future<?> future;

		private WebRequestFutureContainer(WebRequest webRequest, Future<?> future) {
			this.webRequest = webRequest;
			this.future = future;
		}
	}
}
