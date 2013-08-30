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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import at.diamonddogs.exception.ProcessorExeception;

/**
 * Abstract processor for DOM based XML parsing
 * 
 * @param <OUTPUT>
 *            the type of the output object
 *            TODO: create parser methods for primitive types (attributes /
 *            elements alike)
 */
public abstract class XMLProcessor<OUTPUT> extends DataProcessor<Document, OUTPUT> {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLProcessor.class);

	private final DocumentBuilderFactory factory;
	protected DocumentBuilder builder;

	/**
	 * Creates an instance of XMLProcessor using a custom DocumentBuilderFactory
	 * 
	 * @param factory
	 */
	public XMLProcessor(DocumentBuilderFactory factory) {
		this.factory = factory;
	}

	/**
	 * Creates an instance of XMLProcessor using a predefined
	 * DocumentBuilderFactory
	 */
	public XMLProcessor() {
		factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setCoalescing(false);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setExpandEntityReferences(false);
		try {
			builder = factory.newDocumentBuilder();
		} catch (Throwable tr) {
			builder = null;
			LOGGER.error("Could not create default DocumentBuilderFactory.", tr);
		}
	}

	@Override
	protected Document createParsedObjectFromByteArray(byte[] data) {
		LOGGER.debug("Creating DOM from " + data.length + " bytes of data");
		InputSource inSource = new InputSource(new InputStreamReader(new ByteArrayInputStream(data)));
		Document dom = null;
		if (builder == null) {
			throw new ProcessorExeception("builder was null, parsing XML not possible!");
		}
		try {
			dom = builder.parse(inSource);
		} catch (Throwable tr) {
			LOGGER.error("Could not create DOM.", tr);
		}
		return dom;
	}

}
