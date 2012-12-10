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
package at.diamonddogs.data.dataobjects;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Pair;

/**
 * Web request representation
 */
public class WebRequest implements Request {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebRequest.class.getSimpleName());

	protected String id = UUID.randomUUID().toString();

	/**
	 * The type of web request
	 */
	public enum Type {
		/** Post Request */
		POST,
		/** Get Request */
		GET
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
	 * The targe url of the request
	 */
	protected URL url;

	/**
	 * The read timeout in ms
	 */
	protected int readTimeout = 2000;

	/**
	 * The connection timeout in ms
	 */
	protected int connectionTimeout = 1000;

	/**
	 * Indicates that redirects should be followed (or not)
	 */
	protected boolean followRedirects = true;

	/**
	 * Request header
	 */
	protected Map<String, String> header;

	/**
	 * 
	 */
	protected long cacheTime = CacheInformation.CACHE_NO;

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
	 * Data to be posted (only valid if {@link WebRequest#requestType} is
	 * Type.POST
	 */
	protected byte[] postData;

	/**
	 * A flag to indicate if this {@link WebRequest} has been cancelled
	 */
	protected boolean isCancelled = false;

	/**
	 * Upload file information
	 */
	protected UploadFile uploadFile;

	/**
	 * Post value list
	 */
	protected List<Pair<String, String>> postValues;

	@SuppressWarnings("javadoc")
	public UploadFile getUploadFile() {
		return uploadFile;
	}

	@SuppressWarnings("javadoc")
	public void setUploadFile(UploadFile uploadFile) {
		this.uploadFile = uploadFile;
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
			LOGGER.error("Invalid url:" + url);
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
	public long getCacheTime() {
		return cacheTime;
	}

	@SuppressWarnings("javadoc")
	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
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
	public byte[] getPostData() {
		return postData;
	}

	/**
	 * Do not use postValues and postData at the same time. These values will
	 * override each other.
	 * 
	 * @param postData
	 */
	public void setPostData(byte[] postData) {
		this.postData = postData;
	}

	@SuppressWarnings("javadoc")
	public void addPostValue(String key, String value) {
		addPostValue(new Pair<String, String>(key, value));
	}

	@SuppressWarnings("javadoc")
	public void addPostValue(Pair<String, String> value) {
		if (postValues == null) {
			postValues = new ArrayList<Pair<String, String>>();
		}
		postValues.add(value);
	}

	@SuppressWarnings("javadoc")
	public List<Pair<String, String>> getPostValues() {
		return postValues;
	}

	/**
	 * 
	 * Will only be used if uploadfile is set.
	 * 
	 * @param postValues
	 *            post values
	 */
	public void setPostValues(List<Pair<String, String>> postValues) {
		this.postValues = postValues;
	}

}
