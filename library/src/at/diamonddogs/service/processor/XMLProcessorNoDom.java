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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * Xml processor for replies that cannot be handled using a DOM (mostly due to
 * memory issues)
 */
public abstract class XMLProcessorNoDom<T> extends ServiceProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLProcessorNoDom.class);

	/**
	 * The reader to be used
	 */
	protected XMLReader reader;
	/**
	 * Instance of the handler for a particular XML document.
	 */
	protected XmlProcessorNoDomHandler xmlHandler;

	public void setXmlHandler(XmlProcessorNoDomHandler handler) {
		try {
			xmlHandler = handler;
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			reader.setContentHandler(handler);
		} catch (Throwable tr) {
			LOGGER.warn("Error initializing parser.", tr);
		}
	}

	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		if (r.getStatus() == Status.OK) {
			try {
				byte[] data = ((WebReply) r.getReply()).getData();
				InputSource inSource = new InputSource(new InputStreamReader(new ByteArrayInputStream(data)));
				reader.parse(inSource);
				handler.sendMessage(createReturnMessage(xmlHandler.getData()));
			} catch (Throwable tr) {
				LOGGER.warn("Failed to parse document", tr);
				handler.sendMessage(createErrorMessage(getProcessorID(), tr, (WebRequest) r.getRequest()));
			}
		}
	}

	protected abstract Message createReturnMessage(T data);

	public abstract class XmlProcessorNoDomHandler extends DefaultHandler {
		public abstract T getData();
	}
}
