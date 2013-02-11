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

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.adapter.soap.SoapReplyAdapter;
import at.diamonddogs.data.dataobjects.SoapReply;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.SoapUtil;

/**
 * Abstract base class for SOAP requests
 * 
 * @param <T>
 *            the output object
 */
public abstract class SoapProcessor<T> extends ServiceProcessor<T> implements SynchronousProcessor<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SoapProcessor.class);

	/**
	 * This method should not be overridden, will pre-process a SOAP reply and
	 * coordinate callbacks and direct processing output to the appropriate
	 * {@link Handler}
	 */
	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		if (r.getStatus() == Status.OK) {
			LOGGER.debug("processing SoapReply");
			SoapReply soapReply = new SoapReplyAdapter((WebReply) r.getReply()).getReply();
			SoapSerializationEnvelope e = soapReply.getEnvelope();
			SoapUtil.printSoapEnvelopeToStdout(e);
			Object result = null;
			try {
				result = e.getResponse();
			} catch (Throwable tr) {
				LOGGER.warn("Problem while getting soap result.", tr);
				return;
			}

			try {
				if (result == null) {
					LOGGER.debug("processSoapNull");
					handler.sendMessage(processSoapNull(r));
				} else if (result instanceof SoapFault) {
					LOGGER.debug("processSoapFault");
					handler.sendMessage(processSoapFault(r, (SoapFault) result));
				} else if (result instanceof SoapObject) {
					LOGGER.debug("processSoapObject");
					handler.sendMessage(createReturnMessage(r, processSoapReply(c, r, (SoapObject) result)));
				} else if (result instanceof SoapPrimitive) {
					LOGGER.debug("processSoapPrimitive");
					handler.sendMessage(createReturnMessage(r, processSoapReply(c, r, (SoapPrimitive) result)));
				} else {
					handler.sendMessage(createErrorMessage(r));
				}
			} catch (Exception e2) {
				LOGGER.debug("processSoap - failed", e2);
				handler.sendMessage(createErrorMessage(e2, r));
			}

		} else {
			handler.sendMessage(createErrorMessage(r));
		}
	}

	/**
	 * Will throw an {@link UnsupportedOperationException} if not overridden by
	 * the subclass.
	 */
	@Override
	public T obtainDataObjectFromWebReply(Context c, ReplyAdapter replyAdapter) {
		throw new UnsupportedOperationException("Not Implemented");
	}

	/**
	 * Called when the SOAP result is null
	 * 
	 * @param replyAdapter
	 *            the {@link ReplyAdapter}
	 * @return a message Object
	 */
	protected Message processSoapNull(ReplyAdapter replyAdapter) {
		return createErrorMessage(replyAdapter);
	}

	/**
	 * Called when a SOAP result should be processed
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param replyAdapter
	 *            a {@link WebRequest}
	 * @param o
	 *            the {@link SoapObject} created by
	 *            {@link SoapProcessor#processWebReply(Context, ReplyAdapter, Handler)}
	 * @return an output object of type T
	 */
	protected abstract T processSoapReply(Context c, ReplyAdapter replyAdapter, SoapObject o);

	/**
	 * Called when a SOAP result should be processed
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param replyAdapter
	 *            a {@link WebRequest}
	 * @param o
	 *            the {@link SoapPrimitive} created by
	 *            {@link SoapProcessor#processWebReply(Context, ReplyAdapter, Handler)}
	 * @return an output object of type T
	 */
	protected abstract T processSoapReply(Context c, ReplyAdapter replyAdapter, SoapPrimitive o);

	/**
	 * Called if the result is a {@link SoapFault}
	 * 
	 * @param replyAdapter
	 *            the {@link WebRequest}
	 * @param fault
	 *            the {@link SoapFault}
	 * @return a {@link Message}
	 */
	protected Message processSoapFault(ReplyAdapter replyAdapter, SoapFault fault) {
		return createErrorMessage(fault, replyAdapter);
	}
}
