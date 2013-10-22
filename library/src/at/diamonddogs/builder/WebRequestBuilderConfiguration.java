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

import at.diamonddogs.builder.WebRequestBuilder.ConnectionTimeout;
import at.diamonddogs.builder.WebRequestBuilder.ReadTimeout;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * This interface defines the configuration options utilized by
 * {@link WebRequestBuilder} and allows the creation of custom configurations.
 */
public interface WebRequestBuilderConfiguration {
	/**
	 * Gets the integer value associated with {@link ReadTimeout#SHORT}
	 * 
	 * @return the short read timeout
	 */
	public int getReadTimeoutShort();

	/**
	 * Gets the integer value associated with {@link ReadTimeout#MEDIUM}
	 * 
	 * @return the medium read timeout
	 */
	public int getReadTimeoutMedium();

	/**
	 * Gets the integer value associated with {@link ReadTimeout#LONG}
	 * 
	 * @return the long read timeout
	 */
	public int getReadTimeoutLong();

	/**
	 * Gets the integer value associated with {@link ReadTimeout#VERYLONG}
	 * 
	 * @return the very long read timeout
	 */
	public int getReadTimeoutVeryLong();

	/**
	 * Gets the integer value associated with {@link ConnectionTimeout#SHORT}
	 * 
	 * @return the short connection timeout
	 */
	public int getConnectionTimeoutShort();

	/**
	 * Gets the integer value associated with {@link ConnectionTimeout#MEDIUM}
	 * 
	 * @return the medium connection timeout
	 */
	public int getConnectionTimeoutMedium();

	/**
	 * Gets the integer value associated with {@link ConnectionTimeout#LONG}
	 * 
	 * @return the long connection timeout
	 */
	public int getConnectionTimeoutLong();

	/**
	 * Gets the integer value associated with {@link ConnectionTimeout#VERYLONG}
	 * 
	 * @return the very long connection timeout
	 */
	public int getConnectionTimeoutVeryLong();

	/**
	 * Determines if the resulting {@link WebRequest} follows redirects
	 * 
	 * @return <code>true</code> if the {@link WebRequest} allows redirects,
	 *         <code>false</code> otherwise
	 */
	public boolean isFollowRedirectEnabled();

	/**
	 * Determines if the resulting {@link WebRequest} uses offline caching
	 * 
	 * @return <code>true</code> if the {@link WebRequest} uses offline caching,
	 *         <code>false</code> otherwise
	 */
	public boolean isOfflineCachingEnabled();

	/**
	 * Determines if the resulting {@link WebRequest} uses connectivity checks
	 * 
	 * @return <code>true</code> if the {@link WebRequest} uses connectivity
	 *         checks, <code>false</code> otherwise
	 */
	public boolean isConnectivityCheckEnabled();

	/**
	 * Determines if the resulting {@link WebRequest} uses connectivity ping
	 * checks
	 * 
	 * @return <code>true</code> if the {@link WebRequest} uses connectivity
	 *         ping checks, <code>false</code> otherwise
	 */
	public boolean isConnectivityPingEnabled();

	/**
	 * Returns the number of retry attempts
	 * 
	 * @return the number of retry attempts
	 */
	public int getRetryCount();

	/**
	 * Returns the retry attempt interval (time between retry attempts)
	 * 
	 * @return the retry attempt interval
	 */
	public int getRetryInterval();

	/**
	 * Returns the default cache time
	 * 
	 * @return the default cache time
	 */
	public long getDefaultCacheTime();
}
