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
package at.diamonddogs.contentprovider;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import at.diamonddogs.data.adapter.database.DataBaseAdapterCacheInformation;
import at.diamonddogs.data.dataobjects.CacheInformation;

/**
 * The {@link CacheContentProvider} provides a standardized interface to cache
 * information
 */
public class CacheContentProvider extends ContentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheContentProvider.class.getSimpleName());

	private static String CACHECONTENTPROVIDER_AUTHORITY;

	private static final String DATABASE_NAME = "cache.db";

	private static final int DATABASE_VERSION = 6;

	/**
	 * The content uri used by this provider
	 */
	public static Uri CONTENT_URI;

	private CacheContentProviderDatabaseHelper databaseHelper;

	private static final class CacheContentProviderDatabaseHelper extends SQLiteOpenHelper {

		public CacheContentProviderDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// @formatter:off
			if(oldVersion < 6){
				try {
					LOGGER.info("starting upgrade");
					db.execSQL("ALTER TABLE " + DataBaseAdapterCacheInformation.TABLE + " RENAME TO old");
					createTable(db);
					db.execSQL("INSERT INTO " + DataBaseAdapterCacheInformation.TABLE + "(" + 
							DataBaseAdapterCacheInformation.CREATIONTIMESTAMP+ "," + 
							DataBaseAdapterCacheInformation.CACHETIME + "," + 
							DataBaseAdapterCacheInformation.FILENAME + ","+ 
							DataBaseAdapterCacheInformation.FILEPATH + ") select "+
							DataBaseAdapterCacheInformation.CREATIONTIMESTAMP+ "," + 
							DataBaseAdapterCacheInformation.CACHETIME + "," + 
							DataBaseAdapterCacheInformation.FILENAME + ","+ 
							DataBaseAdapterCacheInformation.FILEPATH + " FROM old");
					db.execSQL("UPDATE CACHE SET " + DataBaseAdapterCacheInformation.USEOFFLINECACHE + " = '0'");
					db.execSQL("DROP TABLE old");
					LOGGER.info("upgrade complete");
				} catch (Exception e) {
					LOGGER.error("upgrade failed",e);
				}
			}
			// @formatter:on
		}

		private void createTable(SQLiteDatabase db) {
			// @formatter:off
			String s = "CREATE TABLE " + 
				DataBaseAdapterCacheInformation.TABLE + " (" + 
				DataBaseAdapterCacheInformation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				DataBaseAdapterCacheInformation.CREATIONTIMESTAMP + " INTEGER, " + 
				DataBaseAdapterCacheInformation.CACHETIME + " INTEGER, " +
				DataBaseAdapterCacheInformation.FILENAME + " TEXT UNIQUE, " +
				DataBaseAdapterCacheInformation.FILEPATH + " TEXT, " +
				DataBaseAdapterCacheInformation.USEOFFLINECACHE + " INTEGER);";
			LOGGER.info("Creating cache: " + s);
			db.execSQL(s);
			LOGGER.info("cache created");
			// @formatter:on
		}

		@SuppressWarnings("unused")
		private CacheInformation[] query(SQLiteDatabase db, Object hash) {
			DataBaseAdapterCacheInformation dbaci = new DataBaseAdapterCacheInformation();
			Cursor cursor;
			if (hash == null) {
				cursor = db.query(DataBaseAdapterCacheInformation.TABLE, null, null, null, null, null, null);
			} else if (!(hash instanceof String)) {
				throw new IllegalArgumentException("The hash must be of type String");
			} else {
				cursor = db.query(DataBaseAdapterCacheInformation.TABLE, null, DataBaseAdapterCacheInformation.FILENAME + " = ?",
						new String[] { hash.toString() }, null, null, null, null);
			}
			if (cursor.getCount() == 0) {
				cursor.close();
				return new CacheInformation[0];
			}
			ArrayList<CacheInformation> ret = new ArrayList<CacheInformation>(cursor.getCount());
			while (cursor.moveToNext()) {
				ret.add(dbaci.deserialize(cursor));
			}
			cursor.close();
			return ret.toArray(new CacheInformation[ret.size()]);
		}
	}

	@Override
	public void attachInfo(Context context, ProviderInfo info) {
		CACHECONTENTPROVIDER_AUTHORITY = info.authority;
		CONTENT_URI = Uri.parse("content://" + CACHECONTENTPROVIDER_AUTHORITY);
		super.attachInfo(context, info);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreate() {
		databaseHelper = new CacheContentProviderDatabaseHelper(getContext());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String hash = uri.getLastPathSegment();
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		Cursor c;
		if (hash == null) {
			c = db.query(DataBaseAdapterCacheInformation.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
		} else {
			String[] args = { hash };
			c = db.query(DataBaseAdapterCacheInformation.TABLE, projection, DataBaseAdapterCacheInformation.FILENAME + " = ?", args, null,
					null, sortOrder);
		}
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType(Uri uri) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		long rowId = db.insertWithOnConflict(DataBaseAdapterCacheInformation.TABLE, DataBaseAdapterCacheInformation.TABLE, values,
				SQLiteDatabase.CONFLICT_REPLACE);
		if (rowId > 0) {
			Uri newUri = Uri.withAppendedPath(CONTENT_URI, "/" + rowId);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String hash = uri.getLastPathSegment();
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count;

		if (hash == null) {
			count = db.delete(DataBaseAdapterCacheInformation.TABLE, selection, selectionArgs);
		} else {
			String[] args = { hash };
			count = db.delete(DataBaseAdapterCacheInformation.TABLE, DataBaseAdapterCacheInformation._ID + " = ?", args);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String hash = uri.getLastPathSegment();
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count;
		if (hash == null) {
			count = db.update(DataBaseAdapterCacheInformation.TABLE, values, selection, selectionArgs);
		} else {
			String[] args = { hash };
			count = db.update(DataBaseAdapterCacheInformation.TABLE, values, DataBaseAdapterCacheInformation._ID + " = ?", args);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
