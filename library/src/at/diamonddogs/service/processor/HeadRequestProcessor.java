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
package at.diamonddogs.service.processor;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;
import at.diamonddogs.util.CacheManager.CachedObject;

/**
 * Use or subclass this {@link ServiceProcessor} when processing
 * {@link Type#HEAD} {@link WebRequest}s. {@link HeadRequestProcessor} supports
 * asynchronous and synchronous {@link WebRequest}s.
 */
public class HeadRequestProcessor extends ServiceProcessor<Map<String, List<String>>> implements
        SynchronousProcessor<Map<String, List<String>>> {

	/**
	 * The processor's ID
	 */
	public static final int ID = 18926342;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		Message m;
		if (r.getStatus() == Status.OK) {
			m = createReturnMessage(r, ((WebReply) r.getReply()).getReplyHeader());
		} else {
			m = createErrorMessage(r);
		}
		handler.sendMessage(m);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request) {
		// no cache support
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProcessorID() {
		return ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<String>> obtainDataObjectFromWebReply(Context c, ReplyAdapter reply) {
		if (reply.getReply() == null) {
			return null;
		}
		return ((WebReply) reply.getReply()).getReplyHeader();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<String>> obtainDataObjectFromCachedObject(Context c, WebRequest wr, CachedObject object) {
		// no cache support
		return null;
	}
}
