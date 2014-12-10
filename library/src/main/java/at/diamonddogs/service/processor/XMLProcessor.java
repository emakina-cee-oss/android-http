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
import java.text.DateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
	 * Creates an instance of XMLProcessor using a custom
	 * {@link DocumentBuilderFactory}
	 * 
	 * @param factory
	 */
	public XMLProcessor(DocumentBuilderFactory factory) {
		this.factory = factory;
	}

	/**
	 * Creates an instance of XMLProcessor using a predefined
	 * {@link DocumentBuilderFactory}
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

	/**
	 * Gets the {@link String} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link String}
	 */
	protected String getStringFromNode(Node node) {
		return node.getTextContent();
	}

	/**
	 * Gets the {@link boolean} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link boolean}
	 */
	protected boolean getBooleanFromNode(Node node) {
		return getBoolean(node.getTextContent());
	}

	/**
	 * Gets the {@link byte} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link byte}
	 */
	protected byte getByteFromNode(Node node) {
		return getByte(node.getTextContent());
	}

	/**
	 * Gets the {@link short} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link short}
	 */
	protected short getShortFromNode(Node node) {
		return getShort(node.getTextContent());
	}

	/**
	 * Gets the {@link char} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link char}
	 */
	protected char getCharFromNode(Node node) {
		return getChar(node.getTextContent());
	}

	/**
	 * Gets the {@link int} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link int}
	 */
	protected int getIntFromNode(Node node) {
		return getInt(node.getTextContent());
	}

	/**
	 * Gets the {@link long} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link long}
	 */
	protected long getLongFromNode(Node node) {
		return getLong(node.getTextContent());
	}

	/**
	 * Gets the {@link float} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link float}
	 */
	protected float getFloatFromNode(Node node) {
		return getFloat(node.getTextContent());
	}

	/**
	 * Gets the {@link double} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @return a {@link double}
	 */
	protected double getDouble(Node node) {
		return getDouble(node.getTextContent());
	}

	/**
	 * Gets the {@link Enum} value of a {@link Node}
	 * 
	 * @param <T>
	 *            the generic type of the {@link Enum} to obtain from the
	 *            {@link Node}
	 * @param node
	 *            the {@link Node} whose value to get
	 * @param cls
	 *            the {@link Class} instance of the {@link Enum} to obtain
	 * @return an {@link Enum} of the provided type <T>
	 */
	protected <T extends Enum<T>> T getEnumFromNode(Node node, Class<T> cls) {
		return getEnum(node.getTextContent(), cls);
	}

	/**
	 * Gets the {@link Date} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @param formatter
	 *            the {@link DateFormat} instance to parse the {@link Date}
	 * @return a {@link Date} or <code>null</code> if parsing the date failed
	 */
	protected Date getDateFromNode(Node node, DateFormat formatter) {
		return getDate(node.getTextContent(), formatter);
	}

	/**
	 * Gets the {@link Date} value of a {@link Node}
	 * 
	 * @param node
	 *            the {@link Node} whose value to get
	 * @param format
	 *            the {@link String} containing a valid date format
	 * @return a {@link Date} or <code>null</code> if parsing the date failed
	 */
	protected Date getDateFromNode(Node node, String format) {
		return getDate(node.getTextContent(), format);
	}

	/**
	 * Checks if the node name equals the provided value using
	 * {@link String#equals(String)}
	 * 
	 * @param node
	 *            the node to check
	 * @param nodeName
	 *            the {@link String} that will be compared
	 *            {@link Node#getNodeName()}
	 * @return <code>true</code> if the {@link Node}'s name equals nodeName,
	 *         <code>false</code> otherwise
	 */
	protected boolean nodeNameEquals(Node node, String nodeName) {
		return node.getNodeName().equals(nodeName);
	}

	/**
	 * Checks if the node name equals the provided value using
	 * {@link String#equalsIgnoreCase(String)}
	 * 
	 * @param node
	 *            the node to check
	 * @param nodeName
	 *            the {@link String} that will be compared
	 *            {@link Node#getNodeName()}
	 * @return <code>true</code> if the {@link Node}'s name equals nodeName,
	 *         <code>false</code> otherwise
	 */
	protected boolean nodeNameEqualsIgnoreCase(Node node, String nodeName) {
		return node.getNodeName().equalsIgnoreCase(nodeName);
	}

	/**
	 * Casts the {@link Element} instance of a given {@link Node} if and only if
	 * {@link Node} is in fact an {@link Element}
	 * 
	 * @param node
	 *            the {@link Node} to cast
	 * @return the {@link Element} casted node
	 */
	protected Element getNodeElement(Node node) {
		if (node instanceof Element) {
			return (Element) node;
		} else {
			return null;
		}
	}
}
