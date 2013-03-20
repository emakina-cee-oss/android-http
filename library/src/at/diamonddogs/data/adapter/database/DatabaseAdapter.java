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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Base class for Database Adapter.
 * 
 * @param <T>
 *            The type of the entity this adapter provides access to
 */
public abstract class DatabaseAdapter<T> implements IDataBaseAdapter<T> {
	protected T dataObject;

	protected List<T> bulkList;

	/**
	 * Allows passing a Dataobject of type {@link T}
	 * 
	 * @param dataObject
	 *            a data object instance
	 */
	public DatabaseAdapter(T dataObject) {
		this.dataObject = dataObject;
	}

	/**
	 * Default constructor
	 */
	public DatabaseAdapter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract ContentValues serialize();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract T deserialize(Cursor c);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract int insert(Context c);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract int update(Context c);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract int delete(Context c);

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final T[] query(Context c, Uri u, Query q) {
		String selection = q.createSelection();
		Cursor cur = c.getContentResolver().query(u, q.projection, selection, q.whereValues, q.sortOrder);
		Object[] ret;
		if (cur.moveToFirst()) {
			ret = new Object[cur.getCount()];
			for (int i = 0; i < cur.getCount(); i++) {
				ret[i] = deserialize(cur);
			}
		} else {
			ret = new Object[0];
		}
		cur.close();
		return (T[]) ret;
	}

	/**
	 * Executes a query and returns an array of results
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param q
	 *            a {@link Query}
	 * @return an array of T[]
	 */
	public T[] query(Context c, Query q) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int commitBulkInsert(Context c) {
		throw new UnsupportedOperationException("If you want to use bulkinsert, implement it.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startBulkInsert(int itemsToInsert) {
		bulkList = new ArrayList<T>(itemsToInsert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startBulkInsert() {
		if (bulkList != null) {
			throw new IllegalStateException("Last bulkInsert has not been commited yet.");
		}
		bulkList = new ArrayList<T>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addToBulkInsert(T dataObject) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.add(dataObject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addToBulkInsert(Cursor c) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.add(deserialize(c));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addToBulkInsert(List<T> dataObjects) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.addAll(dataObjects);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addToBulkInsert(T[] dataObjects) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.addAll(Arrays.asList(dataObjects));
	}

	protected int commitBulkInsert(Context c, Uri uri) {
		if (bulkList == null || bulkList.isEmpty()) {
			return -1;
		}
		ContentValues[] cv = new ContentValues[bulkList.size()];
		for (int i = 0; i < bulkList.size(); i++) {
			setDataObject(bulkList.get(i));
			cv[i] = serialize();
		}
		int row = c.getContentResolver().bulkInsert(uri, cv);
		bulkList = null;
		return row;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getDataObject() {
		return dataObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataObject(T dataObject) {
		this.dataObject = dataObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataObject(Cursor c) {
		this.dataObject = deserialize(c);
	}

	protected int getIdFromUri(Uri u) {
		return Integer.parseInt(u.getPathSegments().get(u.getPathSegments().size() - 1));
	}
}
