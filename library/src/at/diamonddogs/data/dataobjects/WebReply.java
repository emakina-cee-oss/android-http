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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Web reply representation
 */
public class WebReply implements Reply {
	/** the http status code of this reply */
	protected int httpStatusCode;
	/** the data returned by the request */
	protected byte[] data;
	/** the header of the reply */
	protected Map<String, List<String>> replyHeader;

	@SuppressWarnings("javadoc")
	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	@SuppressWarnings("javadoc")
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	@SuppressWarnings("javadoc")
	public byte[] getData() {
		return data;
	}

	@SuppressWarnings("javadoc")
	public void setData(byte[] data) {
		this.data = data;
	}

	@SuppressWarnings("javadoc")
	public Map<String, List<String>> getReplyHeader() {
		return replyHeader;
	}

	@SuppressWarnings("javadoc")
	public void setReplyHeader(Map<String, List<String>> replyHeader) {
		this.replyHeader = replyHeader;
	}

	@Override
	public String toString() {
		return "WebReply [httpStatusCode=" + httpStatusCode + ", data=" + (data == null ? null : Arrays.toString(data)) + ", replyHeader="
				+ replyHeader + "]";
	}

}
