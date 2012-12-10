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
package at.diamonddogs.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.util.Pair;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest.Type;

public class WebClientHttpURLConnection extends WebClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebClientHttpURLConnection.class.getSimpleName());

	private HttpURLConnection connection;

	private int retryCount = 0;

	public WebClientHttpURLConnection(Context context) {
		super(context);
		SSLSocketFactory sslSocketFactory = SSLHelper.getInstance().SSL_FACTORY_JAVA;
		if (sslSocketFactory == null) {
			LOGGER.warn("No SSL Connection possible");
		} else {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
		}
	}

	@Override
	public void run() {

		ReplyAdapter listenerReply = null;
		if (webRequest == null) {
			throw new WebClientException("WebRequest must not be null!");
		}
		retryCount = webRequest.getNumberOfRetries();
		do {
			try {
				retryCount--;
				WebReply reply;

				connection = (HttpURLConnection) webRequest.getUrl().openConnection();
				configureConnection();
				reply = runRequest();

				if (needsFollowRedirect(reply)) {
					String url = getRedirectUrl(reply);
					LOGGER.error("following redirect manually to new url: " + url);
					connection = (HttpURLConnection) new URL(url).openConnection();
					configureConnection();
					reply = runRequest();
				}

				listenerReply = createListenerReply(webRequest, reply, null, Status.OK);

				int status = ((WebReply) listenerReply.getReply()).getHttpStatusCode();
				if (!(status == -1)) {
					retryCount = -1;
				}
			} catch (Throwable tr) {

				if (retryCount != 0) {
					try {
						Thread.sleep(webRequest.getRetryInterval());
					} catch (InterruptedException e) {
						LOGGER.error("Error in WebRequest", e);
					}
				}
				listenerReply = createListenerReply(webRequest, null, tr, Status.FAILED);
				LOGGER.info("Error running webrequest " + tr.getMessage(), tr);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		} while (retryCount >= 0);

		if (webClientReplyListener != null) {
			webClientReplyListener.onWebReply(this, listenerReply);
		}
	}

	private String getRedirectUrl(WebReply wr) {
		return wr.getReplyHeader().get("location").get(0);
	}

	private boolean needsFollowRedirect(WebReply wr) {
		if (!followProtocolRedirect) {
			return false;
		}
		if (wr.getHttpStatusCode() == HTTPStatus.HTTP_MOVED_TEMP || wr.getHttpStatusCode() == HTTPStatus.HTTP_MOVED_PERM) {
			return true;
		}
		return false;
	}

	private void configureConnection() throws ProtocolException {
		connection.setReadTimeout(webRequest.getReadTimeout());
		connection.setConnectTimeout(webRequest.getConnectionTimeout());
		connection.setInstanceFollowRedirects(webRequest.isFollowRedirects());

		setRequestType();
		buildHeader();
	}

	private void setRequestType() throws ProtocolException {
		switch (webRequest.getRequestType()) {
		case POST:
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			break;
		case GET:
			connection.setRequestMethod("GET");
			break;
		}
	}

	protected void buildHeader() {
		Map<String, String> header = webRequest.getHeader();
		if (header != null) {
			for (String field : header.keySet()) {
				connection.setRequestProperty(field, header.get(field));
			}
		}
	}

	private WebReply runRequest() throws IOException {

		configureConnection();

		writePostDataIfPresent();

		int statusCode = connection.getResponseCode();

		WebReply reply = null;

		switch (statusCode) {
		case HttpURLConnection.HTTP_PARTIAL:
		case HttpURLConnection.HTTP_OK:
			LOGGER.debug("WebRequest OK: " + webRequest);
			publishFileSize(connection.getContentLength());
			reply = handleResponseOk(connection.getInputStream(), statusCode, connection.getHeaderFields());
			break;
		case HttpURLConnection.HTTP_NOT_MODIFIED:
			reply = handleResponseNotModified(statusCode, connection.getHeaderFields());
			break;
		default:
			LOGGER.debug("WebRequest DEFAULT: " + webRequest + " status code: " + statusCode);
			if (connection != null) {
				reply = handleResponseNotOk(connection.getInputStream(), statusCode, connection.getHeaderFields());
			} else {
				reply = handleResponseNotOk(null, statusCode, connection.getHeaderFields());
			}

			break;
		}

		return reply;
	}

	/**
	 * Appends post data to the request. WARNING: HttpUrlConnection IS BUGGY! IT
	 * WILL CUT OF POST DATA!
	 */
	private void writePostDataIfPresent() {
		try {
			byte[] postData = webRequest.getPostData();
			List<Pair<String, String>> postValues = webRequest.getPostValues();
			if (postData != null && postValues != null) {
				throw new IllegalArgumentException("WebRequest specifies post data and post values, which are mutually exclusive");
			}

			if (webRequest.getRequestType() == Type.POST) {
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);
				connection.setRequestMethod("POST");
				byte[] data = null;
				if (postData != null) {
					data = postData;
				} else if (postValues != null) {
					data = getPostParameterStringFromWebRequest().getBytes();
				}

				if (data != null) {
					connection.setRequestProperty("Content-Length", String.valueOf(data.length));
					OutputStream os = connection.getOutputStream();
					os.write(data, 0, data.length);
					os.flush();
					os.close();
				}
			}
		} catch (Throwable tr) {
			LOGGER.warn("Could not write POST data", tr);
		}
	}

	private String getPostParameterStringFromWebRequest() {
		StringBuilder ret = new StringBuilder();
		Iterator<Pair<String, String>> iterator = webRequest.getPostValues().iterator();
		do {
			Pair<String, String> p = iterator.next();
			ret.append(p.first + "=" + p.second);
			if (iterator.hasNext()) {
				ret.append("&");
			}
		} while (iterator.hasNext());
		String retString = ret.toString();
		LOGGER.info("Post parameters from value pairs: " + retString);
		return retString;
	}
}
