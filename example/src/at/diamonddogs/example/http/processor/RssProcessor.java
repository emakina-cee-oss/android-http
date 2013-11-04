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
package at.diamonddogs.example.http.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import at.diamonddogs.service.processor.XMLProcessor;

/**
 * 
 */
public class RssProcessor extends XMLProcessor<String[]> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RssProcessor.class.getSimpleName());
	public static final int ID = 93025;

	/**
	 * Parses news post titles only
	 */
	@Override
	protected String[] parse(Document inputObject) {
		NodeList nodeList = inputObject.getElementsByTagName("title");
		String[] ret = new String[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++) {
			ret[i] = nodeList.item(i).getTextContent();
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProcessorID() {
		return ID;
	}
}
