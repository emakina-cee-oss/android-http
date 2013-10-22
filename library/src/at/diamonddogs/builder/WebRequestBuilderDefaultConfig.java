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

import at.diamonddogs.data.dataobjects.CacheInformation;

/**
 * The default implementation for {@link WebRequestBuilder} configuration.
 */
public class WebRequestBuilderDefaultConfig implements WebRequestBuilderConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getReadTimeoutShort() {
		return 1000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getReadTimeoutMedium() {
		return 5000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getReadTimeoutLong() {
		return 20000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getReadTimeoutVeryLong() {
		return 60000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConnectionTimeoutShort() {
		return 1000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConnectionTimeoutMedium() {
		return 5000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConnectionTimeoutLong() {
		return 10000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConnectionTimeoutVeryLong() {
		return 15000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFollowRedirectEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOfflineCachingEnabled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnectivityCheckEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnectivityPingEnabled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRetryCount() {
		return 3;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRetryInterval() {
		return 500;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getDefaultCacheTime() {
		return CacheInformation.CACHE_NO;
	}

}
