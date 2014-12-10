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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.parcelable.ParcelableAdapterWebReply;
import at.diamonddogs.data.adapter.parcelable.ParcelableAdapterWebRequest;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.Reply;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager;
import at.diamonddogs.util.CacheManager.CachedObject;
import at.diamonddogs.util.Utils;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProcessor.class.getSimpleName());

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
	 * Called if there is a {@link CachedObject} available for the
	 * {@link Request} that is being processed, defaults to the old
	 * implementation.
	 * 
	 * @param cachedObject
	 *            the {@link CachedObject} related to the {@link Request}
	 * @param handler
	 *            the {@link Handler} instance that posts the result of the
	 *            {@link Request} to the UI thread
	 * @param request
	 *            the {@link Request} that is being processed
	 * @param context
	 *            the {@link Context} that is used
	 */
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request, Context context) {
		processCachedObject(cachedObject, handler, request);
	}

	/**
	 * Returns the ID of the processor
	 * 
	 * @return the id of the processor
	 */
	public abstract int getProcessorID();

	/**
	 * @deprecated don't use, only for backwards compatibility
	 */
	@Deprecated
	protected void cacheObjectToFile(Context context, WebRequest request, byte[] data) {
		cacheObjectToFile(context, request, data, false);
	}

	/**
	 * Writes {@link WebRequest} specific data to the cache. Ignores
	 * {@link WebRequest} whose {@link WebRequest#getCacheTime()} is
	 * {@link CacheInformation#CACHE_NO}. This method disables
	 * {@link CacheInformation}s offline caching feature.
	 * 
	 * @param context
	 *            a {@link Context}
	 * @param r
	 *            a {@link ReplyAdapter}
	 * @see CacheInformation#useOfflineCache
	 */
	protected void cacheObjectToFile(Context context, ReplyAdapter r) {
		cacheObjectToFile(context, (WebRequest) r.getRequest(), ((WebReply) r.getReply()).getData(), false);
	}

	/**
	 * Writes {@link WebRequest} specific data to the cache. Ignores
	 * {@link WebRequest} whose {@link WebRequest#getCacheTime()} is
	 * {@link CacheInformation#CACHE_NO}
	 * 
	 * @param context
	 *            a {@link Context}
	 * @param r
	 *            a {@link ReplyAdapter}
	 * @param useOfflineCache
	 *            controls {@link CacheInformation}s useOfflineCache parameter
	 * 
	 * @see CacheInformation#useOfflineCache
	 */
	protected void cacheObjectToFile(Context context, ReplyAdapter r, boolean useOfflineCache) {
		cacheObjectToFile(context, (WebRequest) r.getRequest(), ((WebReply) r.getReply()).getData(), useOfflineCache);
	}

	/**
	 * Writes {@link WebRequest} specific data to the cache. Ignores
	 * {@link WebRequest} whose {@link WebRequest#getCacheTime()} is
	 * {@link CacheInformation#CACHE_NO}
	 * 
	 * @param context
	 *            a {@link Context}
	 * @param request
	 *            the {@link WebRequest} whose data will be saved to the cache
	 * @param data
	 *            the actual data
	 * @param useOfflineCache
	 *            controls {@link CacheInformation}s useOfflineCache parameter
	 * 
	 * @see CacheInformation#useOfflineCache
	 */
	protected void cacheObjectToFile(Context context, WebRequest request, byte[] data, boolean useOfflineCache) {
		String filename = Utils.getMD5Hash(request.getUrl().toString());
		BufferedOutputStream bos = null;
		try {
			if (filename != null && data != null) {
				if (request.getCacheTime() != CacheInformation.CACHE_NO) {
					File path = Utils.getCacheDir(context);
					FileOutputStream fos = new FileOutputStream(new File(path, filename));
					bos = new BufferedOutputStream(fos);
					bos.write(data);

					CacheInformation ci = createCachingInformation(request.getCacheTime(), path.toString(), filename, useOfflineCache);

					CacheManager cm = CacheManager.getInstance();
					cm.addToCache(context, ci);
				}
			}
		} catch (Throwable th) {
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					bos = null;
				}
			}
		}
	}

	private CacheInformation createCachingInformation(long chacheTime, String filePath, String fileName, boolean useOfflineCache) {
		CacheInformation c = new CacheInformation();
		c.setCacheTime(chacheTime);
		c.setCreationTimeStamp(System.currentTimeMillis());
		c.setFileName(fileName);
		c.setFilePath(filePath);
		c.setUseOfflineCache(useOfflineCache);
		return c;
	}

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
		Message m = Message.obtain();
		m.what = getProcessorID();
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
		Message m = Message.obtain();
		m.what = getProcessorID();
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
		Message m = Message.obtain();
		m.what = getProcessorID();
		m.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, tr);
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest((WebRequest) replyAdapter.getRequest()));
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REPLY, new ParcelableAdapterWebReply((WebReply) replyAdapter.getReply()));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, false);
		m.setData(b);
		return m;
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
		Message m = Message.obtain();
		m.what = getProcessorID();
		m.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, new Throwable());
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest((WebRequest) replyAdapter.getRequest()));
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REPLY, new ParcelableAdapterWebReply((WebReply) replyAdapter.getReply()));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, false);
		m.setData(b);
		return m;
	}

	/**
	 * Creates a default error message for a {@link WebRequest}. Use this method
	 * when dealing with errors caused by cache retrieval.
	 * 
	 * @param tr
	 *            a {@link Throwable} explaining the error
	 * @param webRequest
	 *            the {@link WebRequest} that caused the error to appear
	 * @return a {@link Message} {@link Object}
	 */
	public Message createErrorMessage(Throwable tr, WebRequest webRequest) {
		Message m = Message.obtain();
		m.what = getProcessorID();
		m.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, tr);
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(webRequest));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, true);
		m.setData(b);
		return m;
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
		Message m = Message.obtain();
		m.what = getProcessorID();
		m.arg1 = RETURN_MESSAGE_FAIL;
		Bundle b = new Bundle(1);
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_THROWABLE, new Throwable());
		b.putParcelable(BUNDLE_EXTRA_MESSAGE_REQUEST, new ParcelableAdapterWebRequest(webRequest));
		b.putSerializable(BUNDLE_EXTRA_MESSAGE_FROMCACHE, true);
		m.setData(b);
		return m;
	}

	protected boolean getBoolean(String s) {
		if (isStringEmpty(s)) {
			return false;
		}
		try {
			return Boolean.parseBoolean(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return false;
		}
	}

	protected byte getByte(String s) {
		if (isStringEmpty(s)) {
			return 0;
		}
		try {
			return Byte.parseByte(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return 0;
		}
	}

	protected short getShort(String s) {
		if (isStringEmpty(s)) {
			return 0;
		}
		try {
			return Short.parseShort(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return 0;
		}
	}

	protected char getChar(String s) {
		if (isStringEmpty(s) || s.length() != 1) {
			return '\0';
		}
		try {
			return s.charAt(0);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return '\0';
		}
	}

	protected int getInt(String s) {
		if (isStringEmpty(s)) {
			return 0;
		}
		try {
			return Integer.parseInt(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return 0;
		}
	}

	protected long getLong(String s) {
		if (isStringEmpty(s)) {
			return 0;
		}
		try {
			return Long.parseLong(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return 0;
		}
	}

	protected float getFloat(String s) {
		if (isStringEmpty(s)) {
			return 0.0f;
		}
		try {
			return Float.parseFloat(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return 0.0f;
		}
	}

	protected double getDouble(String s) {
		if (isStringEmpty(s)) {
			return 0.0d;
		}
		try {
			return Double.parseDouble(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return 0.0d;
		}
	}

	protected <T extends Enum<T>> T getEnum(String s, Class<T> cls) {
		try {
			if (s.equals("")) {
				return null;
			}
			return Enum.valueOf(cls, s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return null;
		}
	}

	protected Date getDate(String s, String format) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			return formatter.parse(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return null;
		}
	}

	protected Date getDate(String s, DateFormat formatter) {
		try {
			return formatter.parse(s);
		} catch (Throwable tr) {
			LOGGER.warn("Could not parse: ", tr);
			return null;
		}
	}

	protected boolean isStringEmpty(String string) {
		return string == null || string.length() == 0;
	}
}
