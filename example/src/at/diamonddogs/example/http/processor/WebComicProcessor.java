/*
 * Copyright (C) 2013 the diamond:dogs|group
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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import at.diamonddogs.example.http.dataobject.WebComic;
import at.diamonddogs.service.processor.XMLProcessor;

/**
 * Parses the RSS feed of a webcomic and returns an instance of {@link WebComic}
 * containing all image urls
 */
public class WebComicProcessor extends XMLProcessor<WebComic> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebComicProcessor.class.getSimpleName());

	public static final int ID = 12581751;

	private static final String IMAGE_URL_REGEX = ".*<img src=\"(.*?)\".*";
	private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(IMAGE_URL_REGEX);

	/**
	 * Parses news post titles only
	 */
	@Override
	protected WebComic parse(Document inputObject) {
		NodeList nodeList = inputObject.getElementsByTagName("content:encoded");
		WebComic ret = new WebComic();
		ArrayList<String> urls = new ArrayList<String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			String content = nodeList.item(i).getTextContent();
			Matcher m = IMAGE_URL_PATTERN.matcher(content);
			if (m.find()) {
				String url = m.group(1);
				LOGGER.error("Adding url " + url);
				urls.add(url);
			}
		}
		ret.setImagePaths(urls);
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
