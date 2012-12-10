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
 * 
 */
public abstract class SoapProcessor<T> extends ServiceProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SoapProcessor.class);

	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		if (r.getStatus() == Status.OK) {
			LOGGER.debug("processing SoapReply");
			SoapReply soapReply = new SoapReplyAdapter((WebReply) r.getReply()).getReply();
			SoapSerializationEnvelope e = soapReply.getEnvelope();
			SoapUtil.printSoapEnvelopeToStdout(e);
			Object result = null;
			try {
				result = (SoapObject) e.getResponse();
			} catch (Throwable tr) {
				LOGGER.warn("Problem while getting soap result.");
				return;
			}

			if (result == null) {
				LOGGER.debug("processSoapNull");
				handler.sendMessage(processSoapNull(r));
			} else if (result instanceof SoapFault) {
				LOGGER.debug("processSoapFault");
				handler.sendMessage(processSoapFault(r, (SoapFault) result));
			} else if (result instanceof SoapObject) {
				try {
					LOGGER.debug("processSoapObject");
					handler.sendMessage(createReturnMessage(processSoapReply(c, r, (SoapObject) result)));
				} catch (Exception e2) {
					LOGGER.debug("processSoapObject - failed", e2);
					handler.sendMessage(createErrorMessage(getProcessorID(), (WebRequest) r.getRequest()));
				}

			}
		} else {
			handler.sendMessage(createErrorMessage(getProcessorID(), (WebRequest) r.getRequest()));
		}
	}

	protected abstract Message processSoapNull(ReplyAdapter wr);

	protected abstract T processSoapReply(Context c, ReplyAdapter wr, SoapObject o);

	protected abstract Message processSoapFault(ReplyAdapter wr, SoapFault fault);

	protected abstract Message createReturnMessage(T returnType);
}
