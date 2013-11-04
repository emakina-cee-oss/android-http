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
package at.diamonddogs.data.adapter;

import at.diamonddogs.data.dataobjects.Reply;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * The {@link ReplyAdapter} holds the status, request, reply and a throwable
 * object if applicable.
 */
public class ReplyAdapter {
	/**
	 * The status of the reply
	 */
	public enum Status {
		/**
		 * indicates that the request was successfully executed (no exceptions
		 * during {@link WebRequest} execution)
		 */
		OK,
		/**
		 * indicates that the request failed (exception during
		 * {@link WebRequest} execution
		 */
		FAILED,
	}

	/**
	 * Status of the request
	 */
	private Status status;

	/**
	 * Abitrary exception
	 */
	private Throwable throwable;

	/**
	 * The actual reply
	 */
	private Reply reply;

	/**
	 * The request that caused this reply
	 */
	private Request request;

	@SuppressWarnings("javadoc")
	public Status getStatus() {
		return status;
	}

	@SuppressWarnings("javadoc")
	public void setStatus(Status status) {
		this.status = status;
	}

	@SuppressWarnings("javadoc")
	public Throwable getThrowable() {
		return throwable;
	}

	@SuppressWarnings("javadoc")
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	@SuppressWarnings("javadoc")
	public Reply getReply() {
		return reply;
	}

	@SuppressWarnings("javadoc")
	public void setReply(Reply reply) {
		this.reply = reply;
	}

	@SuppressWarnings("javadoc")
	public Request getRequest() {
		return request;
	}

	@SuppressWarnings("javadoc")
	public void setRequest(Request request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "ReplyAdapter [status=" + status + ", throwable=" + throwable + ", reply=" + reply + ", request=" + request + "]";
	}

}
