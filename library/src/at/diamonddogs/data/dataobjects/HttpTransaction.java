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

import java.util.Date;

import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * Represents a HTTP transaction. Primary use: HTTP transaction object for db
 * logging.
 */
public abstract class HttpTransaction {
	/**
	 * Database id
	 */
	private long _id;
	/**
	 * The time at which this {@link HttpTransaction} was started
	 */
	private Date startTime;
	/**
	 * The time at which this {@link HttpTransaction} finished
	 */
	private Date finishTime;
	/**
	 * The {@link ServiceProcessor} handling the {@link WebRequest} of this
	 * transaction
	 */
	private Class<ServiceProcessor<?>> processorClass;

	/**
	 * A flag indicating if the device was connected to a network while the
	 * {@link WebRequest} took place
	 */
	private boolean connected;

	/**
	 * A flag indicating if the device was able to ping the target host provided
	 * by {@link WebRequest#getUrl()}
	 */
	private boolean pingable;

	@SuppressWarnings("javadoc")
	public long get_id() {
		return _id;
	}

	@SuppressWarnings("javadoc")
	public void set_id(long _id) {
		this._id = _id;
	}

	@SuppressWarnings("javadoc")
	public Date getStartTime() {
		return startTime;
	}

	@SuppressWarnings("javadoc")
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@SuppressWarnings("javadoc")
	public Date getFinishTime() {
		return finishTime;
	}

	@SuppressWarnings("javadoc")
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	@SuppressWarnings("javadoc")
	public Class<ServiceProcessor<?>> getProcessorClass() {
		return processorClass;
	}

	@SuppressWarnings("javadoc")
	public void setProcessorClass(Class<ServiceProcessor<?>> processorClass) {
		this.processorClass = processorClass;
	}

	@SuppressWarnings("javadoc")
	public boolean isConnected() {
		return connected;
	}

	@SuppressWarnings("javadoc")
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@SuppressWarnings("javadoc")
	public boolean isPingable() {
		return pingable;
	}

	@SuppressWarnings("javadoc")
	public void setPingable(boolean pingable) {
		this.pingable = pingable;
	}

	@Override
	public String toString() {
		return "HttpTransaction [_id=" + _id + ", startTime=" + startTime + ", finishTime=" + finishTime + ", processorClass="
				+ processorClass + ", connected=" + connected + ", pingable=" + pingable + "]";
	}
}
