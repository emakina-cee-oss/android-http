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
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager.CachedObject;

/**
 * Abstract class required to process data from services. For performance and
 * complexity reasons it is deprecated to save the state of a processing
 * operation, since the processor can be called from multiple threads at once.
 * 
 * Message Format (Error and Success): 1) m.what - _MUST_ be the processorID 2)
 * m.arg1 - _MUST_ be either ServiceProcessor.RETURN_MESSAGE_FAIL or
 * ServiceProcessor.RETURN_MESSAGE_OK 3) the request must be provided using
 * ServiceProcessor.BUNDLE_EXTRA_MESSAGE_REQUEST as bundle key 4) a throwable
 * should be provided using ServiceProcessor.BUNDLE_EXTRA_MESSAGE_THROWABLE as
 * bundle key, IF m.arg1 == ServiceProcessor.RETURN_MESSAGE_FAIL
 */
public abstract class ServiceProcessor {

	public static final int RETURN_MESSAGE_FAIL = 0;

	public static final int RETURN_MESSAGE_OK = 1;

	public static final String BUNDLE_EXTRA_MESSAGE_THROWABLE = "RETURN_MESSAGE_THROWABLE";

	public static final String BUNDLE_EXTRA_MESSAGE_REQUEST = "RETURN_MESSAGE_REQUEST";

	public abstract void processWebReply(Context c, ReplyAdapter r, Handler handler);

	public abstract void processCachedObject(CachedObject cachedObject, Handler handler, Request request);

	public abstract int getProcessorID();

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
