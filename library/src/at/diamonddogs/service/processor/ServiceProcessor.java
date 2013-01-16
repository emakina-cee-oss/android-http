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
import at.diamonddogs.data.adapter.parcelable.ParcelableAdapterWebRequest;
import at.diamonddogs.data.dataobjects.Reply;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager.CachedObject;

// @formatter:off
/**
 * Abstract class required to process data from services. For performance and
 * complexity reasons it is deprecated to save the state of a processing
 * operation, since the processor can be called from multiple threads at once.
 * 
 * Message Format (Error and Success): 
 * 1) m.what - _MUST_ be the processorID 
 * 2) m.arg1 - _MUST_ be either ServiceProcessor.RETURN_MESSAGE_FAIL or ServiceProcessor.RETURN_MESSAGE_OK
 * 3) the request must be provided using ServiceProcessor.BUNDLE_EXTRA_MESSAGE_REQUEST as bundle key
 * 4) a throwable should be provided using ServiceProcessor.BUNDLE_EXTRA_MESSAGE_THROWABLE as bundle key, IF m.arg1 == ServiceProcessor.RETURN_MESSAGE_FAIL
 * 
 * TODO: Implement a way to pass http status codes down to the handler, using the message created by processors
 */
// @formatter:on
public abstract class ServiceProcessor {

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
	public static final String BUNDLE_EXTRA_MESSAGE_THROWABLE = "RETURN_MESSAGE_THROWABLE";

	/**
	 * {@link Bundle} key for the {@link Request} that was processed
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_REQUEST = "RETURN_MESSAGE_REQUEST";

	/**
	 * {@link Bundle} key for the HTTP status code returned by the operation
	 */
	public static final String BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE = "BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE";

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
	 * Creates a default error message for a {@link WebRequest}
	 * 
	 * @param processorId
	 *            the ID of the processor
	 * @param tr
	 *            the {@link Throwable} related to the error
	 * @param wr
	 *            the {@link WebRequest} that caused the error
	 * @return an error {@link Message} object
	 */
	public Message createErrorMessage(int processorId, Throwable tr, WebRequest wr) {
		Message msg = new Message();
		msg.what = processorId;
		msg.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, tr);
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(wr));
		msg.setData(b);
		return msg;
	}

	/**
	 * Creates a default error message for a {@link WebRequest}. Creates and
	 * provides a new {@link Throwable}
	 * 
	 * @param processorId
	 *            the ID of the processor
	 * @param wr
	 *            the {@link WebRequest} that caused the error
	 * @return an error {@link Message} object
	 */
	public Message createErrorMessage(int processorId, WebRequest wr) {
		Message msg = new Message();
		msg.what = processorId;
		msg.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, new Throwable());
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(wr));
		msg.setData(b);
		return msg;
	}
}
