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

import java.io.Serializable;

import android.os.Message;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * A small util class that can be used to conveniently obtain various
 * information from a
 * reply {@link Message}.
 */
public class ServiceProcessorMessageUtil {

	/**
	 * Check if msg was created by the {@link ServiceProcessor} identified by
	 * the given processorId
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @param processorId
	 *            the processor id to be checked
	 * @return <code>true</code> if the message was send from the processor
	 *         known by processorId, <code>false</code> otherwise
	 */
	public static boolean isFromProcessor(Message msg, int processorId) {
		return msg.what == processorId;
	}

	/**
	 * Check if the {@link WebRequest} was successful
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return <code>true</code> if the {@link WebRequest} was successful,
	 *         <code>false</code> otherwise
	 */
	public static boolean isSuccessful(Message msg) {
		return msg.arg1 == ServiceProcessor.RETURN_MESSAGE_OK;
	}

	/**
	 * Obtains the {@link WebRequest} from msg
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return the {@link WebRequest} that caused {@link Message} msg or null if
	 *         the {@link WebRequest} hasn't been provided properly
	 */
	public static WebRequest getWebRequest(Message msg) {
		Serializable request = msg.getData().getSerializable(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_REQUEST);
		if (request == null) {
			return null;
		}
		if (request instanceof WebRequest) {
			return (WebRequest) request;
		} else {
			return null;
		}
	}

	/**
	 * Obtains the {@link WebReply} from msg
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return the {@link WebReply} that caused {@link Message} msg or null if
	 *         the {@link WebReply} hasn't been provided properly
	 */
	public static WebReply getWebReply(Message msg) {
		Serializable reply = msg.getData().getSerializable(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_REPLY);
		if (reply == null) {
			return null;
		}
		if (reply instanceof WebReply) {
			return (WebReply) reply;
		} else {
			return null;
		}
	}

	/**
	 * Returns the http status code of the reply
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return the http status code or <code>-1</code> if the result was
	 *         obtained from cache or if the http status code is not accessible
	 */
	public static int getHttpStatusCode(Message msg) {
		if (isFromCache(msg)) {
			return -1;
		} else {
			return msg.getData().getInt(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE, -1);
		}
	}

	/**
	 * Checks if the result of a {@link WebRequest} was obtained from the cache
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return <code>true</code> if the payload was obtained from cache,
	 *         <code>false</code> otherwise
	 */
	public static boolean isFromCache(Message msg) {
		return msg.getData().getBoolean(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_FROMCACHE, false);
	}

	/**
	 * Returns the payload
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return the payload
	 */
	public static Object getPayLoad(Message msg) {
		return msg.obj;
	}

	/**
	 * Returns the {@link Throwable} describing the problem in case the
	 * {@link WebRequest} was unsuccessful
	 * 
	 * @param msg
	 *            the input {@link Message}
	 * @return a {@link Throwable} or <code>null</code> if none was provided
	 */
	public static Throwable getThrowable(Message msg) {
		Serializable throwable = msg.getData().getSerializable(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_THROWABLE);
		if (throwable == null) {
			return null;
		}
		if (throwable instanceof Throwable) {
			return (Throwable) throwable;
		} else {
			return null;
		}
	}
}
