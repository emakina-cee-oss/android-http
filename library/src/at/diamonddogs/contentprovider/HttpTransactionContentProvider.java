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
package at.diamonddogs.contentprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 */
public class HttpTransactionContentProvider extends ContentProvider {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTransactionContentProvider.class.getSimpleName());

	private static String HTTPTRANSACTIONCONTENTPROVIDER_AUTHORITY;

	private static final String DATABASE_NAME = "httptransaction.db";

	private static final int DATABASE_VERSION = 1;

	/**
	 * The content uri used by this provider
	 */
	public static Uri CONTENT_URI;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void attachInfo(Context context, ProviderInfo info) {
		HTTPTRANSACTIONCONTENTPROVIDER_AUTHORITY = info.authority;
		CONTENT_URI = Uri.parse("content://" + HTTPTRANSACTIONCONTENTPROVIDER_AUTHORITY);
		super.attachInfo(context, info);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType(Uri arg0) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreate() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}

}
