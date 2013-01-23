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
package at.diamonddogs.service.processor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.parcelable.ParcelableAdapterWebReply;
import at.diamonddogs.data.adapter.parcelable.ParcelableAdapterWebRequest;
import at.diamonddogs.data.dataobjects.Reply;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager.CachedObject;

// @formatter:off
/**
 * Abstract class required to process data from services. For performance and
 * complexity reasons it is deprecated to save the state of a processing
 * operation, since the processor can be called from multiple threads at once.
 * 
 * Message Format (Error and Success):
 * 
 * android-http 1.0
 * 1) m.what - _MUST_ be the processorID 
 * 2) m.arg1 - _MUST_ be either {@link ServiceProcessor#RETURN_MESSAGE_FAIL} or {@link ServiceProcessor#RETURN_MESSAGE_OK}
 * 3) The {@link Request} must be provided using {@link ServiceProcessor#BUNDLE_EXTRA_MESSAGE_REQUEST} as bundle key
 * 4) A {@link Throwable} should be provided using {@link ServiceProcessor#BUNDLE_EXTRA_MESSAGE_THROWABLE} as {@link Bundle} key, IF {@link Message#arg1} == {@link ServiceProcessor#RETURN_MESSAGE_FAIL}
 * 
 * android-http 1.0+
 * 5) The {@link Reply} must be provided using {@link ServiceProcessor#BUNDLE_EXTRA_MESSAGE_REPLY} as bundle key UNLESS the object was obtained from cache
 * 6) The http status code must be provided using {@link ServiceProcessor#BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE} as {@link Bundle} key UNLESS the object was obtained from cache OR if an error prevents access to the http status code
 * 7) Payload, which is defined as the result of the {@link WebRequest}, processed by a {@link ServiceProcessor} must be saved in m.obj
 * 8) The {@link ServiceProcessor#BUNDLE_EXTRA_MESSAGE_FROMCACHE} must be used to indicate if the Object was obtained from the cache or the {@link WebRequest}, {@link Boolean} flag!
 *
 * @param <OUTPUT>
 *            the type out output {@link Object} the subclass will produce.
 */
// @formatter:on
public abstract class ServiceProcessor<OUTPUT> {

	/**
	 * Constant that indicates failure
	 */
	public static final int RETURN_MESSAGE_FAIL = 0;

	/**
	 * Constant that indicated success
	 */
	public static final int RETURN_MESSAGE_OK = 1;

	/**
	 * {@link Bundle} key for {@link Throwable}s caused during processing
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_THROWABLE = "BUNDLE_EXTRA_MESSAGE_THROWABLE";

	/**
	 * {@link Bundle} key for the {@link Request} that was processed
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_REQUEST = "BUNDLE_EXTRA_MESSAGE_REQUEST";

	/**
	 * {@link Bundle} key for the {@link Reply} that was processed
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_REPLY = "BUNDLE_EXTRA_MESSAGE_REPLY";

	/**
	 * {@link Bundle} key for the HTTP status code returned by the operation
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE = "BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE";

	/**
	 * {@link Bundle} key that indicates if the result of a {@link WebRequest}
	 * was taken from the cache
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_FROMCACHE = "BUNDLE_EXTRA_MESSAGE_FROMCACHE";

	/**
	 * Called when a {@link Reply} is ready for processing
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param r
	 *            a {@link ReplyAdapter} containing {@link Request} and
	 *            {@link Reply}
	 * @param handler
	 *            the {@link Handler} instance that posts the result of the
	 *            {@link Request} to the UI thread
	 */
	public abstract void processWebReply(Context c, ReplyAdapter r, Handler handler);

	/**
	 * Called if there is a {@link CachedObject} available for the
	 * {@link Request} that is being processed
	 * 
	 * @param cachedObject
	 *            the {@link CachedObject} related to the {@link Request}
	 * @param handler
	 *            the {@link Handler} instance that posts the result of the
	 *            {@link Request} to the UI thread
	 * @param request
	 *            the {@link Request} that is being processed
	 */
	public abstract void processCachedObject(CachedObject cachedObject, Handler handler, Request request);

	/**
	 * Returns the ID of the processor
	 * 
	 * @return the id of the processor
	 */
	public abstract int getProcessorID();

	/**
	 * Use this method to create a return {@link Message} if the result of the
	 * {@link WebRequest} was obtained from the web rather than the cache
	 * 
	 * @param replyAdapter
	 *            the {@link ReplyAdapter} that was passed to
	 *            {@link ServiceProcessor#processWebReply(Context, ReplyAdapter, Handler)}
	 * @param payload
	 *            the processed data
	 * @return a {@link Message} object containing all required return data
	 */
	protected Message createReturnMessage(ReplyAdapter replyAdapter, OUTPUT payload) {
		Message m = new Message();
		m.what = ((WebRequest) replyAdapter.getRequest()).getProcessorId();
		m.arg1 = ServiceProcessor.RETURN_MESSAGE_OK;
		m.obj = payload;
		Bundle dataBundle = new Bundle();
		dataBundle.putParcelable(BUNDLE_EXTRA_MESSAGE_REPLY, new ParcelableAdapterWebReply((WebReply) replyAdapter.getReply()));
		dataBundle.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest((WebRequest) replyAdapter.getRequest()));
		dataBundle.putInt(BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE, ((WebReply) replyAdapter.getReply()).getHttpStatusCode());
		dataBundle.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, false);
		m.setData(dataBundle);
		return m;
	}

	/**
	 * Use this method to create a return {@link Message} if the result of the
	 * {@link WebRequest} was obtained from the cache rather than the web
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} that is the root of this reply
	 * @param payload
	 *            the processed data
	 * @return a {@link Message} object containing all required return data
	 */
	protected Message createReturnMessage(WebRequest webRequest, OUTPUT payload) {
		Message m = new Message();
		m.what = webRequest.getProcessorId();
		m.arg1 = ServiceProcessor.RETURN_MESSAGE_OK;
		m.obj = payload;
		Bundle dataBundle = new Bundle();
		dataBundle.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(webRequest));
		dataBundle.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, true);
		m.setData(dataBundle);
		return m;
	}

	/**
	 * Creates a default error message for a {@link WebRequest}
	 * 
	 * @param tr
	 *            the {@link Throwable} related to the error
	 * @param replyAdapter
	 *            the instance of {@link ReplyAdapter} that was the result of
	 *            the {@link WebRequest}
	 * @return an error {@link Message} object
	 */
	public Message createErrorMessage(Throwable tr, ReplyAdapter replyAdapter) {
		Message msg = new Message();
		msg.what = getProcessorID();
		msg.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, tr);
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest((WebRequest) replyAdapter.getRequest()));
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REPLY, new ParcelableAdapterWebReply((WebReply) replyAdapter.getReply()));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, false);
		msg.setData(b);
		return msg;
	}

	/**
	 * Creates a default error message for a {@link WebRequest}. Creates and
	 * provides a new {@link Throwable}.
	 * 
	 * @param replyAdapter
	 *            the instance of {@link ReplyAdapter} that was the result of
	 *            the {@link WebRequest}
	 * @return an error {@link Message} object
	 */
	public Message createErrorMessage(ReplyAdapter replyAdapter) {
		Message msg = new Message();
		msg.what = getProcessorID();
		msg.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, new Throwable());
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest((WebRequest) replyAdapter.getRequest()));
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REPLY, new ParcelableAdapterWebReply((WebReply) replyAdapter.getReply()));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, false);
		msg.setData(b);
		return msg;
	}

	/**
	 * Creates a default error message for a {@link WebRequest}. Use this method
	 * when dealing with
	 * errors caused by cache retrieval.
	 * 
	 * @param tr
	 *            a {@link Throwable} explaining the error
	 * @param webRequest
	 *            the {@link WebRequest} that caused the error to appear
	 * @return a {@link Message} {@link Object}
	 */
	public Message createErrorMessage(Throwable tr, WebRequest webRequest) {
		Message msg = new Message();
		msg.what = getProcessorID();
		msg.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, tr);
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(webRequest));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, true);
		msg.setData(b);
		return msg;
	}

	/**
	 * Creates a default error message for a {@link WebRequest}. Creates and
	 * provides a new {@link Throwable}. Use this method when dealing with
	 * errors caused by cache retrieval.
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} that caused the error to appear
	 * @return a {@link Message} {@link Object}
	 */
	public Message createErrorMessage(WebRequest webRequest) {
		Message msg = new Message();
		msg.what = getProcessorID();
		msg.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, new Throwable());
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(webRequest));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, true);
		msg.setData(b);
		return msg;
	}
}
