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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.SparseArray;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.net.WebClient;
import at.diamonddogs.net.WebClient.DownloadProgressListener;
import at.diamonddogs.net.WebClient.WebClientReplyListener;
import at.diamonddogs.net.WebClientFactory;
import at.diamonddogs.service.processor.ProcessorExeception;
import at.diamonddogs.service.processor.ServiceProcessor;
import at.diamonddogs.util.CacheManager;
import at.diamonddogs.util.CacheManager.CachedObject;
import at.diamonddogs.util.WorkerQueue;

public class HttpService extends Service implements WebClientReplyListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpService.class);

	private static final int POOL_SIZE_CORE = 10;

	private static final int POOL_SIZE_MAX = 20;

	private static final int POOL_KEEPALIVE = 3000;

	private SparseArray<ServiceProcessor> registeredProcessors;

	private Map<Handler, List<WebRequest>> webRequestHandlerMap;

	private WorkerQueue workerQueue;

	private IBinder binder;

	public static final String INTENT_EXTRA_WEBREQUEST = "webrequest";

	public static final String BUNDLE_WEBREPLY = "webreply";

	public static final String BUNDLE_THROWABLE = "throwable";

	private Map<String, Future<?>> webRequestMap;
	private Map<String, WebRequest> webReqeusts;

	@Override
	public void onCreate() {
		super.onCreate();
		workerQueue = new WorkerQueue(POOL_SIZE_CORE, POOL_SIZE_MAX, POOL_KEEPALIVE);
		webRequestHandlerMap = new HashMap<Handler, List<WebRequest>>();
		registeredProcessors = new SparseArray<ServiceProcessor>();
		webRequestMap = new HashMap<String, Future<?>>();
		webReqeusts = new HashMap<String, WebRequest>();
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

	public final class HttpServiceBinder extends Binder {
		public HttpService getHttpService() {
			return HttpService.this;
		}
	}

	public String runWebRequest(Handler handler, final WebRequest webRequest, final DownloadProgressListener progressListener) {
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
		ServiceProcessor processor = registeredProcessors.get(webRequest.getProcessorId());
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
		RequestRunnable requestRunnable = new RequestRunnable(webRequest, progressListener);

		Future<?> future = workerQueue.runCancelableTask(requestRunnable);

		webReqeusts.put(webRequest.getId(), webRequest);
		webRequestMap.put(webRequest.getId(), future);

		return webRequest.getId();
	}

	public String runWebRequest(Handler handler, WebRequest webRequest) {
		return runWebRequest(handler, webRequest, null);
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

	public void registerProcessor(ServiceProcessor processor) {
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

	public void unregisterProcessor(int id) {
		registeredProcessors.remove(id);
	}

	public boolean isProcessorRegistered(int id) {
		return (registeredProcessors.indexOfKey(id) >= 0);
	}

	public ServiceProcessor getRegisteredProcessorById(int id) {
		return registeredProcessors.get(id);
	}

	@Override
	public void onWebReply(WebClient webClient, ReplyAdapter reply) {
		LOGGER.debug("onWebReply: " + reply.getStatus());
		webRequestMap.remove(webClient.getWebRequest().getId());
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

	private ServiceProcessor getProcessor(Request webRequest) {
		int processorId = ((WebRequest) webRequest).getProcessorId();
		ServiceProcessor processor = registeredProcessors.get(processorId);
		if (processor == null) {
			throw new ProcessorExeception("No processor with id " + processorId);
		}
		LOGGER.debug("found processor with id: " + processorId);
		return processor;
	}

	public static void bindService(Context context, ServiceConnection serviceConnection) {
		Intent intent = new Intent(context, HttpService.class);
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private class RequestRunnable implements Runnable {

		WebRequest webRequest;
		DownloadProgressListener downloadProgressListener;

		public RequestRunnable(WebRequest webRequest, DownloadProgressListener downloadProgressListener) {
			this.webRequest = webRequest;
			this.downloadProgressListener = downloadProgressListener;
		}

		@Override
		public void run() {
			CacheManager cm = CacheManager.getInstance();
			try {
				CachedObject cachedObject = cm.getFromCache(HttpService.this, webRequest);
				if (cachedObject == null) {
					LOGGER.debug("No cached objects available for: " + webRequest.getUrl());
					workerQueue.runTask(getNewWebClient(webRequest, downloadProgressListener));
				} else {
					LOGGER.debug("File found in file cache: " + webRequest.getUrl());
					dispatchCachedObjectToProcessor(cachedObject, webRequest);
				}
			} catch (Throwable tr) {
				LOGGER.debug("No cached objects available for: " + webRequest.getUrl());
				workerQueue.runTask(getNewWebClient(webRequest, downloadProgressListener));
			}
		}
	}

	public void cancelRequest(String id) {
		LOGGER.debug("cancelRequest " + id);
		if (webRequestMap.containsKey(id)) {
			LOGGER.debug("found cancelRequest " + id);
			Future<?> request = webRequestMap.get(id);
			request.cancel(true);
			WebRequest cancelRequest = webReqeusts.get(id);
			cancelRequest.setCancelled(true);
			webRequestMap.remove(id);
		}
	}
}
