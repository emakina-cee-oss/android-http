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
package at.diamonddogs.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Build;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;

/**
 * The {@link WebClientFactory} returns the appropriate {@link WebClient} for a
 * {@link WebRequest}, judging by parameters and Android Version.
 * http://android-developers .blogspot.co.at/2011/09/androids-http-clients.html
 */
public class WebClientFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebClientFactory.class);

	private static WebClientFactory INSTANCE = null;

	/**
	 * Singleton getInstance() method
	 * 
	 * @return a singleton instance of {@link WebClientFactory}
	 */
	public synchronized static WebClientFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WebClientFactory();
		}
		return INSTANCE;
	}

	/**
	 * 
	 * Returns the most stable network client most suitable for the current
	 * android platform
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} to obtain a {@link WebClient} for
	 * @param context
	 *            a {@link Context}
	 * @return a {@link WebClient}
	 * 
	 */
	public WebClient getNetworkClient(WebRequest webRequest, Context context) {
		WebClient client = null;
		// HttpUrlConnection, that should be used starting from FROYO, cuts off
		// POST data ... we need to force the obsolete client implementation!!!
		if (isPostWithData(webRequest)) {
			LOGGER.info("!!!WARNING!!! FORCE USING WebClientDefaultHttpClient DUE TO BUGGY IMPLEMENTATION OF HttpUrlConnection (POST DATA WOULD OTHERWISE BE CUT OFF)!!!");
			client = new WebClientDefaultHttpClient(context);
		} else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			LOGGER.debug("Using WebClientHttpURLConnection, since SDK bigger than Froyo: " + Build.VERSION.SDK_INT);
			client = new WebClientHttpURLConnection(context);
		} else {
			LOGGER.debug("Using WebClientDefaultHttpClient, since SDK smaller or equal Froyo: " + Build.VERSION.SDK_INT);
			client = new WebClientDefaultHttpClient(context);
		}
		return client;
	}

	/**
	 * Checks if the {@link WebRequest} has post data
	 * 
	 * @param wr
	 *            the {@link WebRequest} to check
	 * @return true or false, depending on the presence of post data
	 */
	public boolean isPostWithData(WebRequest wr) {
		return wr.getRequestType() == Type.POST && wr.getHttpEntity() != null;
	}
}
