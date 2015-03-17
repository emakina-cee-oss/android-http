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

import android.content.Context;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * The {@link WebClientFactory} returns the appropriate {@link WebClient} for a
 * {@link WebRequest}, judging by parameters and Android Version.
 * http://android-developers .blogspot.co.at/2011/09/androids-http-clients.html
 */
public class WebClientFactory {

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
		return new WebClientOkHttpClient(context);
	}
}
