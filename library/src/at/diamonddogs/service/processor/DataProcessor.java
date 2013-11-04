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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager.CachedObject;

/**
 * Base class for data processing
 * 
 * @param <INPUT>
 *            the input object type that will be created from the byte array
 *            returned by a {@link WebRequest}. Examples: JSONArray, JSONObject,
 *            etc
 * @param <OUTPUT>
 *            The output object that will be created from the input object,
 *            usually a POJO
 */
public abstract class DataProcessor<INPUT, OUTPUT> extends ServiceProcessor<OUTPUT> implements SynchronousProcessor<OUTPUT> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessor.class.getSimpleName());

	/**
	 * Parses the content of data into an INPUT object
	 * 
	 * @param data
	 *            the raw data
	 * @return a parsed object
	 */
	protected abstract INPUT createParsedObjectFromByteArray(byte[] data);

	/**
	 * The actual parsing of the data
	 * 
	 * @param inputObject
	 *            the object returned by createParsedObjectFromByteArray
	 * @return return data that can be handed to createReturnMessage
	 */
	protected abstract OUTPUT parse(INPUT inputObject);

	/**
	 * Handles processing using the provided callback methods of the respective
	 * child classes
	 * 
	 * @param data
	 *            the raw input data
	 * @return a processing object containing the data extracted from the raw
	 *         input byte[] data and a Message object. Message.what will be set
	 *         to the current processors id.
	 */
	protected ProcessingData<OUTPUT> processData(ReplyAdapter replyAdapter) {
		INPUT input = createParsedObjectFromByteArray(((WebReply) replyAdapter.getReply()).getData());
		OUTPUT output = parse(input);
		Message message = createReturnMessage(replyAdapter, output);
		return new ProcessingData<OUTPUT>(message, output);
	}

	/**
	 * Handles processing using the provided callback methods of the respective
	 * child classes. This method should be used if the data was obtained from
	 * the cache.
	 * 
	 * @param wr
	 *            the {@link WebRequest}
	 * @param data
	 *            the data obtained from cache
	 * @return {@link ProcessingData} containing all relevant information
	 */
	protected ProcessingData<OUTPUT> processData(WebRequest wr, byte[] data) {
		INPUT input = createParsedObjectFromByteArray(data);
		OUTPUT output = parse(input);
		Message message = createReturnMessage(wr, output);
		return new ProcessingData<OUTPUT>(message, output);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		try {
			if (r.getStatus() == Status.OK) {
				handler.sendMessage(processData(r).returnMessage);
				if (((WebRequest) r.getRequest()).getCacheTime() != CacheInformation.CACHE_NO) {
					cacheObjectToFile(c, (WebRequest) r.getRequest(), ((WebReply) r.getReply()).getData(),
							((WebRequest) r.getRequest()).isUseOfflineCache());
				}
			} else {
				handler.sendMessage(createErrorMessage(r));
			}
		} catch (Throwable tr) {
			handler.sendMessage(createErrorMessage(tr, r));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request) {
		try {
			handler.sendMessage(processData((WebRequest) request, (byte[]) cachedObject.getCachedObject()).returnMessage);
		} catch (Throwable tr) {
			handler.sendMessage(createErrorMessage(tr, (WebRequest) request));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OUTPUT obtainDataObjectFromWebReply(Context c, ReplyAdapter reply) {
		LOGGER.debug("status: " + ((WebReply) reply.getReply()).getHttpStatusCode());
		cacheObjectToFile(c, reply);
		return parse(createParsedObjectFromByteArray(((WebReply) reply.getReply()).getData()));
	}

	/**
	 * Default implementation - no caching support
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param object
	 *            the {@link CachedObject}
	 * @return always returns <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public OUTPUT obtainDataObjectFromCachedObject(Context c, WebRequest wr, CachedObject object) {
		switch (object.getFrom()) {
		case MEMORY:
			return (OUTPUT) object.getCachedObject();
		case FILE:
			return processData(wr, (byte[]) object.getCachedObject()).output;
		default:
			throw new RuntimeException("Invalid cache source");
		}
	}

	@Override
	protected Message createReturnMessage(ReplyAdapter replyAdapter, OUTPUT payload) {
		return super.createReturnMessage(replyAdapter, payload);
	}

	protected final static class ProcessingData<OUTPUT> {
		public Message returnMessage;
		public OUTPUT output;

		public ProcessingData(Message returnMessage, OUTPUT output) {
			this.returnMessage = returnMessage;
			this.output = output;
		}
	}

}
