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
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * Xml processor for replies that cannot be handled using a DOM (mostly due to
 * memory issues), supports synchronous {@link WebRequest}s
 * 
 * @param <T>
 *            the type of object created by this processor
 */
public abstract class SynchronousXmlProcessorNoDom<T> extends DataProcessor<InputSource, T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronousXmlProcessorNoDom.class);

	/**
	 * The reader to be used
	 */
	protected XMLReader reader;
	/**
	 * Instance of the handler for a particular XML document.
	 */
	protected XmlProcessorNoDomHandler xmlHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected InputSource createParsedObjectFromByteArray(byte[] data) {
		return new InputSource(new InputStreamReader(new ByteArrayInputStream(data)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected T parse(InputSource inputObject) {
		try {
			LOGGER.info("Starting NoDom parsing");
			reader.parse(inputObject);
			LOGGER.info("NoDom parsing complete");
		} catch (Throwable tr) {
			LOGGER.error("Failed to parse!", tr);
			return null;
		}
		return xmlHandler.getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		try {
			if (r.getStatus() == Status.OK) {
				ProcessingData<T> pData = processData(r);
				handler.sendMessage(pData.returnMessage);
			} else {
				handler.sendMessage(createErrorMessage(r));
			}
		} catch (Throwable tr) {
			handler.sendMessage(createErrorMessage(tr, r));
		}
	}

	/**
	 * Sets the {@link XmlProcessorNoDomHandler} that will be used to parse the
	 * XML
	 * 
	 * @param handler
	 *            the {@link XmlProcessorNoDomHandler}
	 */
	public void setXmlHandler(XmlProcessorNoDomHandler handler) {
		try {
			xmlHandler = handler;
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			reader.setContentHandler(handler);
		} catch (Throwable tr) {
			LOGGER.warn("Error initializing parser.", tr);
		}
	}

	/**
	 * Wrapper for {@link DefaultHandler} that allows obtaining the data object
	 */
	public abstract class XmlProcessorNoDomHandler extends DefaultHandler {
		/**
		 * Gets the data object created by the handler
		 * 
		 * @return the data object
		 */
		public abstract T getData();
	}

}
