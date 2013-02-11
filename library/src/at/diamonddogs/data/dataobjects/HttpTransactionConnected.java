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

import at.diamonddogs.data.adapter.ReplyAdapter;

/**
 * A {@link HttpTransaction} representing a non cached transaction
 */
public class HttpTransactionConnected extends HttpTransaction {
	/**
	 * The {@link ReplyAdapter} created by the {@link WebRequest}
	 */
	private ReplyAdapter replyAdapter;

	@SuppressWarnings("javadoc")
	public ReplyAdapter getReplyAdapter() {
		return replyAdapter;
	}

	@SuppressWarnings("javadoc")
	public void setReplyAdapter(ReplyAdapter replyAdapter) {
		this.replyAdapter = replyAdapter;
	}

}
