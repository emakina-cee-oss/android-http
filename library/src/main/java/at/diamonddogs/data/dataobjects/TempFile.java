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


/**
 * Represents a temorary file
 */
public class TempFile {

	/** the url of the file represented by this object */
	private String url;

	/** the file's checksum */
	private String checksum;

	/** the file's path */
	private String path;

	/** the date */
	private long date;

	/** the size of the file */
	private int size;

	/** controls wether the checksum will be used for validation */
	private boolean useChecksum;

	/** append data to an existing temp file */
	private boolean append;

	/**
	 * Default constructor
	 */
	public TempFile() {
		url = new String();
		checksum = new String();
		path = new String();
		date = 0L;
		size = 0;
		useChecksum = true;
		append = false;
	}

	@SuppressWarnings("javadoc")
	public boolean isAppend() {
		return append;
	}

	@SuppressWarnings("javadoc")
	public void setAppend(boolean append) {
		this.append = append;
	}

	@SuppressWarnings("javadoc")
	public boolean isUseChecksum() {
		return useChecksum;
	}

	@SuppressWarnings("javadoc")
	public void setUseChecksum(boolean useChecksum) {
		this.useChecksum = useChecksum;
	}

	@SuppressWarnings("javadoc")
	public int getSize() {
		return size;
	}

	@SuppressWarnings("javadoc")
	public void setSize(int size) {
		this.size = size;
	}

	@SuppressWarnings("javadoc")
	public String getPath() {
		return path;
	}

	@SuppressWarnings("javadoc")
	public void setPath(String path) {
		this.path = path;
	}

	@SuppressWarnings("javadoc")
	public String getUrl() {
		return url;
	}

	@SuppressWarnings("javadoc")
	public void setUrl(String url) {
		this.url = url;
	}

	@SuppressWarnings("javadoc")
	public String getChecksum() {
		return checksum;
	}

	@SuppressWarnings("javadoc")
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@SuppressWarnings("javadoc")
	public long getDate() {
		return date;
	}

	@SuppressWarnings("javadoc")
	public void setDate(long date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "url: " + url + " checksum: " + checksum + " date: " + date + " path: " + path + " FileSize: " + size;
	}
}
