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
package at.diamonddogs.data.dataobjects;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Pair;
import at.diamonddogs.service.net.HttpService;

/**
 * Web request representation
 * 
 * TODO: set processor instance instead of id, add a flag -> usePresentProcessor
 * (uses an already registered processor, if set to false, the given processor
 * instance will be used and delete from HttpService once the request has
 * finished)
 */
public class WebRequest implements Request {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebRequest.class.getSimpleName());

	/**
	 * Default constructor
	 */
	public WebRequest() {
		origin = new Throwable();
		timeCritical = true;
	}

	/**
	 * An instance of {@link Throwable} that records where the
	 * {@link WebRequest} was created from.
	 */
	protected final Throwable origin;

	protected boolean timeCritical;

	protected String id = UUID.randomUUID().toString();

	/**
	 * The type of web request
	 */
	public enum Type {
		/** Post Request */
		POST,
		/** Get Request */
		GET,
		/** Head Request */
		HEAD
	}

	/**
	 * No retries
	 */
	public static final int NO_RETRY = -1;

	/**
	 * No timeout
	 */
	public static final int NO_TIMEOUT = 0;

	/**
	 * The id of the processor that should handle the request
	 */
	protected int processorId = -1;

	/**
	 * The type of the request, currently only GET and POST are supported
	 */
	private Type requestType = Type.GET;

	/**
	 * An arbitrary entity (e.g. for post)
	 */
	protected HttpEntity httpEntity;

	/**
	 * The targe url of the request
	 */
	protected URL url;

	/**
	 * The read timeout in ms
	 */
	protected int readTimeout = 50000;

	/**
	 * The connection timeout in ms
	 */
	protected int connectionTimeout = 50000;

	/**
	 * Indicates that redirects should be followed (or not)
	 */
	protected boolean followRedirects = true;

	/**
	 * Request header
	 */
	protected Map<String, String> header;

	/**
	 * If set to <code>true</code>, header fields will be appended instead of
	 * overwritten
	 */
	protected boolean appendHeader = false;

	/**
	 * The time the result of this {@link WebRequest} should be cached, default
	 * is {@link CacheInformation#CACHE_NO} which turns of caching altogether
	 */
	protected long cacheTime = CacheInformation.CACHE_NO;

	/**
	 * Constrols {@link CacheInformation#useOfflineCache}
	 */
	protected boolean useOfflineCache = false;

	/**
	 * Retry attempts of this request
	 */
	protected int numberOfRetries = 3;

	/**
	 * The interval in which retries will take place
	 */
	protected int retryInterval = 500;

	/**
	 * The tempfile
	 */
	protected Pair<Boolean, TempFile> tmpFile = new Pair<Boolean, TempFile>(false, null);

	/**
	 * A flag to indicate if this {@link WebRequest} has been cancelled
	 */
	protected boolean isCancelled = false;

	/**
	 * A flag to indicate if connectivity should be checked before running the
	 * {@link WebRequest}. This flag will cause {@link HttpService} to use the
	 * {@link ConnectivityManager} to check for connectivity using
	 * {@link ConnectivityManager#getActiveNetworkInfo()}.
	 */
	protected boolean checkConnectivity = true;

	/**
	 * If set to <code>true</code>, {@link HttpService} will issue a ping to see
	 * if the target host is reachable. This feature is turned off by default.
	 */
	protected boolean checkConnectivityPing = false;

	private boolean getStream = false;

	public void setGetStream(boolean getStream) {
		this.getStream = getStream;
	}

	public boolean isGetStream() {
		return getStream;
	}

	@SuppressWarnings("javadoc")
	public boolean isCancelled() {
		return isCancelled;
	}

	@SuppressWarnings("javadoc")
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	@SuppressWarnings("javadoc")
	public String getId() {
		return id;
	}

	/**
	 * Adds a header field to the http header. WARNING this method is _NOT_
	 * synchronized
	 * 
	 * @param field
	 * @param value
	 */
	public void addHeaderField(String field, String value) {
		if (header == null) {
			header = new HashMap<String, String>();
		}
		header.put(field, value);
	}

	@SuppressWarnings("javadoc")
	public void removeHeaderField(String field) {
		header.remove(field);
	}

	@SuppressWarnings("javadoc")
	public Type getRequestType() {
		return requestType;
	}

	@SuppressWarnings("javadoc")
	public void setRequestType(Type requestType) {
		this.requestType = requestType;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@SuppressWarnings("javadoc")
	public void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (Throwable t) {
			LOGGER.warn("Invalid url:" + url, t);
			this.url = null;
		}
	}

	@SuppressWarnings("javadoc")
	public void setUrl(URI uri) {
		try {
			this.url = uri.toURL();
		} catch (Throwable t) {
			LOGGER.warn("Invalid url:" + url, t);
			this.url = null;
		}
	}

	@SuppressWarnings("javadoc")
	public void setUrl(Uri uri) {
		try {
			setUrl(uri.toString());
		} catch (Throwable t) {
			LOGGER.warn("Invalid url:" + url, t);
			this.url = null;
		}
	}

	@SuppressWarnings("javadoc")
	public void setUrl(URL url) {
		this.url = url;
	}

	@SuppressWarnings("javadoc")
	public int getReadTimeout() {
		return readTimeout;
	}

	@SuppressWarnings("javadoc")
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	@SuppressWarnings("javadoc")
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	@SuppressWarnings("javadoc")
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	@SuppressWarnings("javadoc")
	public boolean isFollowRedirects() {
		return followRedirects;
	}

	@SuppressWarnings("javadoc")
	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	@SuppressWarnings("javadoc")
	public Map<String, String> getHeader() {
		return header;
	}

	@SuppressWarnings("javadoc")
	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	@SuppressWarnings("javadoc")
	public boolean isAppendHeader() {
		return appendHeader;
	}

	@SuppressWarnings("javadoc")
	public void setAppendHeader(boolean appendHeader) {
		this.appendHeader = appendHeader;
	}

	@SuppressWarnings("javadoc")
	public int getProcessorId() {
		return processorId;
	}

	@SuppressWarnings("javadoc")
	public void setProcessorId(int processorId) {
		this.processorId = processorId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCacheTime() {
		return cacheTime;
	}

	@SuppressWarnings("javadoc")
	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	@SuppressWarnings("javadoc")
	public boolean isUseOfflineCache() {
		return useOfflineCache;
	}

	@SuppressWarnings("javadoc")
	public void setUseOfflineCache(boolean useOfflineCache) {
		this.useOfflineCache = useOfflineCache;
	}

	@SuppressWarnings("javadoc")
	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	@SuppressWarnings("javadoc")
	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	@SuppressWarnings("javadoc")
	public int getRetryInterval() {
		return retryInterval;
	}

	@SuppressWarnings("javadoc")
	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	@SuppressWarnings("javadoc")
	public Pair<Boolean, TempFile> getTmpFile() {
		return tmpFile;
	}

	@SuppressWarnings("javadoc")
	public void setTmpFile(Pair<Boolean, TempFile> tmpFile) {
		this.tmpFile = tmpFile;
	}

	@SuppressWarnings("javadoc")
	public HttpEntity getHttpEntity() {
		return httpEntity;
	}

	@SuppressWarnings("javadoc")
	public void setHttpEntity(HttpEntity httpEntity) {
		this.httpEntity = httpEntity;
	}

	@SuppressWarnings("javadoc")
	public boolean isCheckConnectivity() {
		return checkConnectivity;
	}

	@SuppressWarnings("javadoc")
	public void setCheckConnectivity(boolean checkConnectivity) {
		this.checkConnectivity = checkConnectivity;
	}

	@SuppressWarnings("javadoc")
	public boolean isCheckConnectivityPing() {
		return checkConnectivityPing;
	}

	@SuppressWarnings("javadoc")
	public void setCheckConnectivityPing(boolean checkConnectivityPing) {
		this.checkConnectivityPing = checkConnectivityPing;
	}

	@SuppressWarnings("javadoc")
	public Throwable getOrigin() {
		return origin;
	}

	@SuppressWarnings("javadoc")
	public boolean isTimeCritical() {
		return timeCritical;
	}

	@Override
	public String toString() {
		return url == null ? "null" : url.toString();
	}

}
