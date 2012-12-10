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

public class CacheInformation {

	public static final long CACHE_NO = -1;
	public static final long CACHE_1M = 60000l;
	public static final long CACHE_1H = 3600000;
	public static final long CACHE_24H = 86400000;
	public static final long CACHE_7D = 604800000;
	public static final long CACHE_1MO = 2419200000l;
	public static final long CACHE_FOREVER = -2;

	private int _id = -1;
	private long creationTimeStamp;
	private long cacheTime;
	/**
	 * Must be the md5 hash of the URL containing the original data.
	 * Utils.getMD5Hash(urlString)
	 */
	private String fileName;
	private String filePath;

	public CacheInformation(long creationTimeStamp, long cacheTime, String fileName, String filePath) {
		this.creationTimeStamp = creationTimeStamp;
		this.cacheTime = cacheTime;
		this.fileName = fileName;
		this.filePath = filePath;
	}

	public CacheInformation() {

	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public void setCreationTimeStamp(long creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
