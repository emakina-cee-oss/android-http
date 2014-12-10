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

/**
 * Generalized interface for requests
 */
public interface Request {
	/**
	 * Returns the request's URL
	 * 
	 * @return a URL instance
	 */
	public URL getUrl();

	/**
	 * Gets the cache time of this request. Indicates how long the data of this
	 * request will be cached before issuing the request again.
	 * 
	 * @return the cache time
	 */
	public long getCacheTime();
}
