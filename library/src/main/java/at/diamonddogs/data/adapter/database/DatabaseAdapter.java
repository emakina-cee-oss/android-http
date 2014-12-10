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
public abstract class DatabaseAdapter<T> {
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
	 * Serializes the dataObject (null check!) to ContentValues that can be used
	 * in database queries
	 * 
	 * @return ContentValues representing the data object
	 */
	public abstract ContentValues serialize();

	/**
	 * Deserialize a dataobject from a cursor, uses the current cursor position
	 * 
	 * @param c
	 *            the cursor
	 * 
	 * @return an object of type {@link T}
	 */
	public abstract T deserialize(Cursor c);

	/**
	 * Inserts the current dataObject into the database
	 * 
	 * @param c
	 *            a context
	 * @return the rowid (or pk, if a pk has been defined) of the new entry
	 */
	public abstract int insert(Context c);

	/**
	 * Updates the current dataObject in the database
	 * 
	 * @param c
	 *            a context
	 * @return the number of updates (i.e. when updating using a join all
	 *         changes should be accounted for)
	 */
	public abstract int update(Context c);

	/**
	 * Deletes the current dataObject in the database
	 * 
	 * @param c
	 *            a context
	 * @return the number of deletes (i.e. when updating using a join all
	 *         changes should be accounted for)
	 */
	public abstract int delete(Context c);

	/**
	 * Simple query method that returns an array of {@link Object}s
	 * 
	 * @param c
	 *            a context
	 * @param u
	 *            the content uri to use
	 * @param query
	 *            a query object
	 * @return an array containing the results of the {@link Query}
	 */
	protected final Object[] query(Context c, Uri u, Query q) {
		String selection = q.createSelection();
		Cursor cur = c.getContentResolver().query(u, q.projection, selection, q.whereValues, q.sortOrder);
		Object[] ret;
		if (cur.getCount() != 0) {
			ret = new Object[cur.getCount()];
			cur.moveToFirst();
			for (int i = 0; i < cur.getCount(); i++) {
				ret[i] = deserialize(cur);
				cur.move(1);
			}
		} else {
			ret = new Object[0];
		}
		cur.close();
		return ret;
	}

	/**
	 * Simple query method that returns a {@link Cursor}
	 * 
	 * @param c
	 *            a context
	 * @param u
	 *            the content uri to use
	 * @param query
	 *            a query object
	 * @return a cursor containing the result of the {@link Query}
	 */
	protected final Cursor queryCursor(Context c, Uri u, Query q) {
		String selection = q.createSelection();
		return c.getContentResolver().query(u, q.projection, selection, q.whereValues, q.sortOrder);
	}

	/**
	 * Simple query method that returns a {@link Cursor}
	 * 
	 * @param c
	 *            a context
	 * @param q
	 *            a query object
	 * @return a cursor containing the result of the {@link Query}
	 */
	public Cursor queryCursor(Context c, Query q) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Convenience query method that returns all available data
	 * 
	 * @param c
	 *            a {@link Context} object
	 * @return a {@link Cursor}
	 */
	public Cursor queryCursor(Context c) {
		return queryCursor(c, new Query());
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
	 * Convenience query method that returns all available data
	 * 
	 * @param c
	 *            a {@link Context} object
	 * @return an array of T[]
	 */
	public T[] query(Context c) {
		return query(c, new Query());
	}

	/**
	 * Commits the current bulk insert
	 * 
	 * @param c
	 *            a contect object
	 * @return the number of items that were inserted into the database
	 */
	public int commitBulkInsert(Context c) {
		throw new UnsupportedOperationException("If you want to use bulkinsert, implement it.");
	}

	/**
	 * Starts a transaction based bulk insert
	 * 
	 * @param itemsToInsert
	 *            the number of items to insert
	 */
	public void startBulkInsert(int itemsToInsert) {
		bulkList = new ArrayList<T>(itemsToInsert);
	}

	/**
	 * Starts a transaction based bulk insert
	 */
	public void startBulkInsert() {
		if (bulkList != null) {
			throw new IllegalStateException("Last bulkInsert has not been commited yet.");
		}
		bulkList = new ArrayList<T>();
	}

	/**
	 * Adds an object to the current bulk insert
	 * 
	 * @param dataObject
	 *            an object of type {@link T} to be inserted when
	 *            {@link IDataBaseAdapter#commitBulkInsert(Context)} is called
	 */
	public void addToBulkInsert(T dataObject) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.add(dataObject);
	}

	/**
	 * Adds items in a {@link Cursor} to the bulk update
	 * 
	 * @param c
	 *            the {@link Cursor} containing items to be bulk inserted
	 */
	public void addToBulkInsert(Cursor c) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.add(deserialize(c));
	}

	/**
	 * Adds a list of objects to the current bulk insert
	 * 
	 * @param dataObjects
	 *            a list of type {@link T} to be inserted when
	 *            {@link IDataBaseAdapter#commitBulkInsert(Context)} is called
	 */
	public void addToBulkInsert(List<T> dataObjects) {
		if (bulkList == null) {
			throw new IllegalStateException("Trying to add items to a bulkInsert, but no bulkInsert has been started yet.");
		}
		bulkList.addAll(dataObjects);
	}

	/**
	 * Adds an array of objects to the current bulk insert
	 * 
	 * @param dataObjects
	 *            an array of type {@link T} to be inserted when
	 *            {@link IDataBaseAdapter#commitBulkInsert(Context)} is called
	 */
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
	 * Returns the current dataObject
	 * 
	 * @return a dataObject of type {@link T}
	 */
	public T getDataObject() {
		return dataObject;
	}

	/**
	 * Sets the dataObject
	 * 
	 * @param dataObject
	 *            the dataObject to be set
	 */
	public void setDataObject(T dataObject) {
		this.dataObject = dataObject;
	}

	/**
	 * Sets the dataObject from a cursor
	 * 
	 * @param c
	 *            the cursor
	 */
	public void setDataObject(Cursor c) {
		this.dataObject = deserialize(c);
	}

	protected int getIdFromUri(Uri u) {
		return Integer.parseInt(u.getPathSegments().get(u.getPathSegments().size() - 1));
	}
}
