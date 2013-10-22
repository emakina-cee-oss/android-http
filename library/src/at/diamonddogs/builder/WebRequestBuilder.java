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
package at.diamonddogs.builder;

import java.net.URI;
import java.net.URL;

import android.net.Uri;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * This builder enables programmers to create uniform {@link WebRequest}s based
 * on the provided configuration. The builder's purpose is not to micro-manage
 * {@link WebRequest} creation but to provide a configured {@link WebRequest}
 * template.
 */
public class WebRequestBuilder {

	/**
	 * The configuration used for {@link WebRequest} construction
	 */
	private WebRequestBuilderConfiguration configuration;

	/**
	 * The {@link WebRequest} that is currently build
	 */
	private WebRequest webRequest;

	/**
	 * This enum is an abstraction for read timeout durations. The exact
	 * duration is controlled by the provided
	 * {@link WebRequestBuilderConfiguration}
	 */
	// @formatter:off
	@SuppressWarnings("javadoc")
	public enum ReadTimeout {
		SHORT, 
		MEDIUM, 
		LONG, 
		VERYLONG
	}
	// @formatter:on

	/**
	 * This enum is an abstraction for connection timeout durations. The exact
	 * duration is controlled by the provided
	 * {@link WebRequestBuilderConfiguration}
	 */
	// @formatter:off
	@SuppressWarnings("javadoc")
	public enum ConnectionTimeout {
		SHORT, 
		MEDIUM, 
		LONG, 
		VERYLONG
	}
	// @formatter:on

	/**
	 * Constructor with configuration option
	 * 
	 * @param configuration
	 *            the {@link WebRequestBuilderConfiguration} to use when
	 *            building {@link WebRequest}s
	 */
	public WebRequestBuilder(WebRequestBuilderConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * This constructor utilzes {@link WebRequestBuilderDefaultConfig} to
	 * configure {@link WebRequest}s
	 */
	public WebRequestBuilder() {
		this.configuration = new WebRequestBuilderDefaultConfig();
	}

	/**
	 * Creates a new {@link WebRequest}, must be called before attempting to do
	 * anything else. Resets any existing {@link WebRequest} instances.
	 * 
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder newWebRequest() {
		webRequest = new WebRequest();
		webRequest.setFollowRedirects(configuration.isFollowRedirectEnabled());
		webRequest.setUseOfflineCache(configuration.isOfflineCachingEnabled());
		webRequest.setCheckConnectivity(configuration.isConnectivityCheckEnabled());
		webRequest.setCheckConnectivityPing(configuration.isConnectivityPingEnabled());
		webRequest.setRetryInterval(configuration.getRetryInterval());
		webRequest.setNumberOfRetries(configuration.getRetryCount());
		webRequest.setCacheTime(configuration.getDefaultCacheTime());
		return this;
	}

	/**
	 * Sets the target url of the {@link WebRequest}
	 * 
	 * @param url
	 *            the url
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setUrl(String url) {
		throwOnError();
		webRequest.setUrl(url);
		return this;
	}

	/**
	 * Sets the target url of the {@link WebRequest}
	 * 
	 * @param url
	 *            the url
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setUrl(URL url) {
		throwOnError();
		webRequest.setUrl(url);
		return this;
	}

	/**
	 * Sets the target url of the {@link WebRequest}
	 * 
	 * @param url
	 *            the url
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setUrl(URI url) {
		throwOnError();
		webRequest.setUrl(url);
		return this;
	}

	/**
	 * Sets the target url of the {@link WebRequest}
	 * 
	 * @param url
	 *            the url
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setUrl(Uri url) {
		throwOnError();
		webRequest.setUrl(url);
		return this;
	}

	/**
	 * Set the type of {@link WebRequest}
	 * 
	 * @param type
	 *            the type
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setType(Type type) {
		throwOnError();
		webRequest.setRequestType(type);
		return this;
	}

	/**
	 * Sets the abstracted read timeout
	 * 
	 * @param timeout
	 *            the read timeout to be used in this {@link WebRequest}
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setReadTimeout(ReadTimeout timeout) {
		throwOnError();
		webRequest.setReadTimeout(getReadTimeout(timeout));
		return this;
	}

	private int getReadTimeout(ReadTimeout timeout) {
		switch (timeout) {
		case SHORT:
			return configuration.getReadTimeoutShort();
		case MEDIUM:
			return configuration.getReadTimeoutMedium();
		case LONG:
			return configuration.getReadTimeoutLong();
		case VERYLONG:
			return configuration.getReadTimeoutVeryLong();
		default:
			return 1000;
		}
	}

	/**
	 * Sets the abstracted connection timeout
	 * 
	 * @param timeout
	 *            the connection timeout to be used in this {@link WebRequest}
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setConnectionTimeout(ConnectionTimeout timeout) {
		throwOnError();
		webRequest.setConnectionTimeout(getConnectionTimeout(timeout));
		return this;
	}

	private int getConnectionTimeout(ConnectionTimeout timeout) {
		switch (timeout) {
		case SHORT:
			return configuration.getConnectionTimeoutShort();
		case MEDIUM:
			return configuration.getConnectionTimeoutMedium();
		case LONG:
			return configuration.getConnectionTimeoutLong();
		case VERYLONG:
			return configuration.getConnectionTimeoutVeryLong();
		default:
			return 1000;
		}
	}

	/**
	 * Sets the ID of the {@link ServiceProcessor} that should handle the
	 * {@link WebRequest}
	 * 
	 * @param processorid
	 *            the processor's id
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder withProcessorId(int processorid) {
		throwOnError();
		webRequest.setProcessorId(processorid);
		return this;
	}

	/**
	 * Sets a custom cache time. This method allows overriding the default cache
	 * time which is provided by the {@link WebRequestBuilderConfiguration} used
	 * by this {@link WebRequestBuilder}
	 * 
	 * @param time
	 *            the cachetime
	 * @return the {@link WebRequestBuilder} instance (allows chaining)
	 */
	public WebRequestBuilder setCacheTime(long time) {
		throwOnError();
		webRequest.setCacheTime(time);
		return this;
	}

	/**
	 * Returns the {@link WebRequest} that has been constructed so far
	 * 
	 * @return a {@link WebRequest}
	 */
	public WebRequest getWebRequest() {
		throwOnError();
		return webRequest;
	}

	private void throwOnError() {
		if (configuration == null) {
			throw new IllegalStateException("configuration is null");
		}
		if (webRequest == null) {
			throw new IllegalStateException("request is null");
		}
	}
}
