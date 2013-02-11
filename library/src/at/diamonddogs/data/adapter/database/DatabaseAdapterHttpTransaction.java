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
package at.diamonddogs.data.adapter.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.database.Cursor;
import at.diamonddogs.data.dataobjects.HttpTransaction;
import at.diamonddogs.service.processor.ServiceProcessor;

public abstract class DatabaseAdapterHttpTransaction<T extends HttpTransaction> extends DatabaseAdapter<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAdapterHttpTransaction.class.getSimpleName());
	/** id column */
	public static final String _ID = "_id";
	/** the starttime column */
	public static final String STARTTIME = "starttime";
	/** the finishtime column */
	public static final String FINISHTIME = "finishtime";
	/** the processor column */
	public static final String PROCESSOR = "processor";
	/** the connected column */
	public static final String CONNECTED = "connected";
	/** the pingable column */
	public static final String PINGABLE = "pingable";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContentValues serialize() {
		ContentValues cv = new ContentValues();
		if (dataObject.get_id() != -1) {
			cv.put(_ID, dataObject.get_id());
		}
		cv.put(STARTTIME, dataObject.getStartTime());
		cv.put(FINISHTIME, dataObject.getFinishTime());
		cv.put(PROCESSOR, dataObject.getProcessorClass().toString());
		cv.put(CONNECTED, dataObject.isConnected() ? 1 : 0);
		cv.put(PINGABLE, dataObject.isPingable() ? 1 : 0);
		return cv;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T deserialize(Cursor c) {
		dataObject = getHttpTransaction();
		dataObject.set_id(c.getLong(c.getColumnIndexOrThrow(_ID)));
		dataObject.setConnected(c.getInt(c.getColumnIndexOrThrow(CONNECTED)) == 1);
		dataObject.setPingable(c.getInt(c.getColumnIndexOrThrow(PINGABLE)) == 1);
		try {
			dataObject.setProcessorClass((Class<ServiceProcessor<?>>) Class.forName(c.getString(c.getColumnIndexOrThrow(PROCESSOR))));
		} catch (Throwable t) {
			LOGGER.warn("Could not load ServiceProcessor class", t);
		}
		dataObject.setStartTime(c.getLong(c.getColumnIndexOrThrow(STARTTIME)));
		dataObject.setFinishTime(c.getLong(c.getColumnIndexOrThrow(FINISHTIME)));
		return dataObject;
	}

	protected abstract T getHttpTransaction();
}
