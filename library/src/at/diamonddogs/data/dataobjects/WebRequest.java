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

public class WebRequest implements Request {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebRequest.class.getSimpleName());

	protected String id = UUID.randomUUID().toString();

	public enum Type {
		POST, GET
	}

	public static final int NO_RETRY = -1;

	public static final int NO_TIMEOUT = 0;

	/**
	 * The id of the processor that should handle the request
	 */
	protected int processorId = -1;

	private Type requestType = Type.GET;

	protected URL url;

	protected int readTimeout = 2000;

	protected int connectionTimeout = 1000;

	protected boolean followRedirects = true;

	protected Map<String, String> header;

	protected long cacheTime = CacheInformation.CACHE_NO;

	protected int numberOfRetries = 3;

	protected int retryInterval = 500;

	protected Pair<Boolean, TempFile> tmpFile = new Pair<Boolean, TempFile>(false, null);

	protected byte[] postData;

	protected boolean isCancelled = false;

	protected UploadFile uploadFile;

	protected List<Pair<String, String>> postValues;

	public UploadFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(UploadFile uploadFile) {
		this.uploadFile = uploadFile;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

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

	public void removeHeaderField(String field) {
		header.remove(field);
	}

	public Type getRequestType() {
		return requestType;
	}

	public void setRequestType(Type requestType) {
		this.requestType = requestType;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	public void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (Throwable t) {
			LOGGER.error("Invalid url:" + url);
			this.url = null;
		}
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	public int getProcessorId() {
		return processorId;
	}

	public void setProcessorId(int processorId) {
		this.processorId = processorId;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public Pair<Boolean, TempFile> getTmpFile() {
		return tmpFile;
	}

	public void setTmpFile(Pair<Boolean, TempFile> tmpFile) {
		this.tmpFile = tmpFile;
	}

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

	public void addPostValue(String key, String value) {
		addPostValue(new Pair<String, String>(key, value));
	}

	public void addPostValue(Pair<String, String> value) {
		if (postValues == null) {
			postValues = new ArrayList<Pair<String, String>>();
		}
		postValues.add(value);
	}

	public List<Pair<String, String>> getPostValues() {
		return postValues;
	}

	/**
	 * Will only be used if uploadfile is set.
	 */
	public void setPostValues(List<Pair<String, String>> postValues) {
		this.postValues = postValues;
	}

}
