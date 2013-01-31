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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.processor.ServiceProcessor;

// @formatter:off
/**
 * This class may be used to chain multiple asynchronous {@link WebRequest}s
 * together. It provides facilities to implement conditional {@link WebRequest}
 * chains that allow running {@link WebRequest} based on the result of previous
 * {@link WebRequest}s. To use this assister, one must wrap the
 * {@link WebRequest} in an instance of {@link HttpOrderedAsyncRequest}. The
 * {@link WebRequest} is executed normally, using {@link HttpServiceAssister}.
 * The following steps will be executed in order once the {@link WebRequest} has
 * been executed:
 * 
 * 1) {@link ReplyAdapter} is passed to the {@link ServiceProcessor} 
 * 2) The {@link ServiceProcessor} creates a {@link Message} {@link Object} and passes
 * it to an instance of {@link HttpOrderedAsyncHandler} 
 * 3) {@link HttpOrderedAsyncHandler} uses the instance of {@link NextWebRequestDelegate}
 * provided by the {@link HttpOrderedAsyncRequest} to determine the next {@link WebRequest}
 * to run and executes it.
 */
//@formatter:on
public class HttpOrderedAsyncAssisiter {
	/**
	 * An instance of {@link HttpServiceAssister} to run the {@link WebRequest}s
	 * in a safe manner.
	 */
	private HttpServiceAssister assister;

	/**
	 * Default constructor
	 * 
	 * @param context
	 *            a {@link Context}
	 */
	public HttpOrderedAsyncAssisiter(Context context) {
		this.assister = new HttpServiceAssister(context);
	}

	/**
	 * Starts running a chain of {@link WebRequest}
	 * 
	 * @param initialRequest
	 *            the initial {@link WebRequest} (the first in line)
	 */
	public void runRequests(HttpOrderedAsyncRequest initialRequest) {
		assister.runWebRequest(initialRequest.handler, initialRequest.webRequest, initialRequest.serviceProcessor);
	}

	/**
	 * Base {@link Handler} for ordered asynchronous {@link WebRequest}s. Uses
	 * {@link NextWebRequestDelegate} to run the next {@link WebRequest} in
	 * line.
	 */
	public class HttpOrderedAsyncHandler extends Handler {
		private HttpOrderedAsyncRequest request;

		/**
		 * Default construcor
		 * 
		 * @param webRequest
		 *            the {@link HttpOrderedAsyncRequest} that uses this handler
		 */
		public HttpOrderedAsyncHandler(HttpOrderedAsyncRequest request) {
			this.request = request;
		}

		/**
		 * Make sure to call super.handleMessage(Message) when
		 * overriding this method
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			HttpOrderedAsyncRequest nextWebRequest = request.nextWebRequestDelegate.getNextWebRequest(msg);
			assister.runWebRequest(nextWebRequest.handler, nextWebRequest.webRequest, nextWebRequest.serviceProcessor);
		}
	}

	/**
	 * Delegates to the next {@link WebRequest} that should be executed. The
	 * {@link Message}, which is the result of the prior {@link WebRequest} will
	 * be passed in order to enable {@link NextWebRequestDelegate} to make an
	 * informed decision.
	 */
	public abstract class NextWebRequestDelegate {
		/**
		 * Gets the next {@link HttpOrderedAsyncRequest} in line. The request
		 * may be determined using the provided {@link Message}. Therefore, it
		 * is possible to issue conditional {@link WebRequest}.
		 * 
		 * @param message
		 *            the {@link Message} that was created by the previous
		 *            {@link WebRequest}'s {@link ServiceProcessor} and then
		 *            passed to {@link HttpOrderedAsyncHandler}.
		 * @return returns the next {@link HttpOrderedAsyncRequest} in line.
		 *         Returning <code>null</code> indicates that the current
		 *         {@link WebRequest} is the last request in line.
		 */
		public abstract HttpOrderedAsyncRequest getNextWebRequest(Message message);
	}

	/**
	 * A wrapper for multiple objects that are relevant if {@link WebRequest}
	 * should be chained.
	 */
	public class HttpOrderedAsyncRequest {
		/**
		 * The actual {@link WebRequest}, usable by {@link HttpService}.
		 */
		private WebRequest webRequest;
		/**
		 * The {@link HttpOrderedAsyncHandler}, that will take care of handling
		 * the message and running the next {@link HttpOrderedAsyncRequest}.
		 */
		private HttpOrderedAsyncHandler handler;
		/**
		 * Handles picking the next {@link HttpOrderedAsyncRequest} in line.
		 */
		private NextWebRequestDelegate nextWebRequestDelegate;
		/**
		 * The {@link ServiceProcessor} for
		 * {@link HttpOrderedAsyncRequest#webRequest}
		 */
		private ServiceProcessor<?> serviceProcessor;

		/**
		 * Default constructor
		 * 
		 * @param webRequest
		 *            the {@link WebRequest} to run.
		 * @param handler
		 *            the {@link Handler} for the {@link WebRequest}
		 * @param nextWebRequestDelegate
		 *            the delegate that determines the next {@link WebRequest}
		 * @param serviceProcessor
		 *            the {@link ServiceProcessor} for the {@link WebRequest}
		 */
		public HttpOrderedAsyncRequest(WebRequest webRequest, HttpOrderedAsyncHandler handler,
				NextWebRequestDelegate nextWebRequestDelegate, ServiceProcessor<?> serviceProcessor) {
			this.webRequest = webRequest;
			this.handler = handler;
			this.nextWebRequestDelegate = nextWebRequestDelegate;
			this.serviceProcessor = serviceProcessor;
		}

		@SuppressWarnings("javadoc")
		public WebRequest getWebRequest() {
			return webRequest;
		}

		@SuppressWarnings("javadoc")
		public void setWebRequest(WebRequest webRequest) {
			this.webRequest = webRequest;
		}

		@SuppressWarnings("javadoc")
		public HttpOrderedAsyncHandler getHandler() {
			return handler;
		}

		@SuppressWarnings("javadoc")
		public void setHandler(HttpOrderedAsyncHandler handler) {
			this.handler = handler;
		}

		@SuppressWarnings("javadoc")
		public NextWebRequestDelegate getNextWebRequestDelegate() {
			return nextWebRequestDelegate;
		}

		@SuppressWarnings("javadoc")
		public void setNextWebRequestDelegate(NextWebRequestDelegate nextWebRequestDelegate) {
			this.nextWebRequestDelegate = nextWebRequestDelegate;
		}

		@SuppressWarnings("javadoc")
		public ServiceProcessor<?> getServiceProcessor() {
			return serviceProcessor;
		}

		@SuppressWarnings("javadoc")
		public void setServiceProcessor(ServiceProcessor<?> serviceProcessor) {
			this.serviceProcessor = serviceProcessor;
		}

	}
}
