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
package at.diamonddogs.data.adapter.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import at.diamonddogs.contentprovider.CacheContentProvider;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.exception.DatabaseAdapterException;

/**
 * Database Adapter for {@link CacheInformation}
 */
public class DataBaseAdapterCacheInformation extends DatabaseAdapter<CacheInformation> {

	/** name of the table */
	public static final String TABLE = "cache";

	/** _id colum */
	public static final String _ID = "_id";

	/** creationtimestamp colum */
	public static final String CREATIONTIMESTAMP = "creationtimestamp";

	/** cachetime colum */
	public static final String CACHETIME = "cachetime";

	/** filename colum */
	public static final String FILENAME = "filename";

	/** filepath colum */
	public static final String FILEPATH = "filepath";

	/** useofflinecache column */
	public static final String USEOFFLINECACHE = "useofflinecache";

	/**
	 * Sets dataObject to the {@link CacheInformation} item currently selected
	 * in c
	 * 
	 * @param c
	 *            the cursor used the create the dataObject. Make sure the
	 *            cursor points to the correct item.
	 */
	public DataBaseAdapterCacheInformation(Cursor c) {
		dataObject = deserialize(c);
	}

	/**
	 * Allows passing a {@link CacheInformation} dataObject
	 * 
	 * @param dataObject
	 *            a {@link CacheInformation} instance
	 */
	public DataBaseAdapterCacheInformation(CacheInformation dataObject) {
		super(dataObject);
	}

	/**
	 * Creates a new {@link CacheInformation} instance to act as dataObject
	 */
	public DataBaseAdapterCacheInformation() {
		super(new CacheInformation());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContentValues serialize() {
		ContentValues cv = new ContentValues();
		int id = dataObject.get_id();
		if (id != -1) {
			cv.put(_ID, id);
		}
		cv.put(CREATIONTIMESTAMP, dataObject.getCreationTimeStamp());
		cv.put(CACHETIME, dataObject.getCacheTime());
		cv.put(FILENAME, dataObject.getFileName());
		cv.put(FILEPATH, dataObject.getFilePath());
		cv.put(USEOFFLINECACHE, dataObject.isUseOfflineCache() ? 1 : 0);
		return cv;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CacheInformation deserialize(Cursor c) {
		dataObject = new CacheInformation();
		dataObject.set_id(c.getInt(c.getColumnIndexOrThrow(_ID)));
		dataObject.setCreationTimeStamp(c.getLong(c.getColumnIndexOrThrow(CREATIONTIMESTAMP)));
		dataObject.setCacheTime(c.getLong(c.getColumnIndexOrThrow(CACHETIME)));
		dataObject.setFileName(c.getString(c.getColumnIndexOrThrow(FILENAME)));
		dataObject.setFilePath(c.getString(c.getColumnIndexOrThrow(FILEPATH)));
		dataObject.setUseOfflineCache(c.getInt(c.getColumnIndexOrThrow(USEOFFLINECACHE)) == 1);
		return dataObject;
	}

	/**
	 * Get a {@link CacheInformation} array identified by the provided hash
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param hash
	 *            the hash used to lookup the {@link CacheInformation}
	 * @return a {@link CacheInformation} array
	 */
	public CacheInformation[] query(Context c, Object hash) {
		Cursor cursor;
		if (hash == null) {
			cursor = c.getContentResolver().query(CacheContentProvider.CONTENT_URI, null, null, null, null);
		} else if (!(hash instanceof String)) {
			throw new IllegalArgumentException("The hash must be of type String");
		} else {
			cursor = c.getContentResolver().query(Uri.withAppendedPath(CacheContentProvider.CONTENT_URI, hash.toString()), null, null,
					null, null);
		}
		if (cursor.getCount() == 0) {
			cursor.close();
			return new CacheInformation[0];
		}
		ArrayList<CacheInformation> ret = new ArrayList<CacheInformation>(cursor.getCount());
		while (cursor.moveToNext()) {
			ret.add(deserialize(cursor));
		}
		cursor.close();
		return ret.toArray(new CacheInformation[ret.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int insert(Context c) {
		if (dataObject == null) {
			throw new DatabaseAdapterException("cannot insert a null reference");
		}
		List<String> pathSegments = c.getContentResolver().insert(CacheContentProvider.CONTENT_URI, serialize()).getPathSegments();
		return Integer.parseInt(pathSegments.get(pathSegments.size() - 1));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Context c) {
		if (dataObject == null) {
			throw new DatabaseAdapterException("cannot update a null reference");
		}
		return c.getContentResolver().update(CacheContentProvider.CONTENT_URI, serialize(), DataBaseAdapterCacheInformation._ID + " = ?",
				new String[] { String.valueOf(dataObject.get_id()) });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(Context c) {
		if (dataObject == null) {
			throw new DatabaseAdapterException("cannot delete a null reference");
		}
		return c.getContentResolver().delete(CacheContentProvider.CONTENT_URI, DataBaseAdapterCacheInformation._ID + " = ?",
				new String[] { String.valueOf(dataObject.get_id()) });
	}
}
