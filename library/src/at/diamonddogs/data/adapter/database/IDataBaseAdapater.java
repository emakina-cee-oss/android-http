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

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * An interface for database adapters
 * 
 * @param <T>
 *            the type the database adapter interfaces to
 */
public interface IDataBaseAdapater<T> {

	/**
	 * Serializes the dataObject (null check!) to ContentValues that can be used
	 * in database queries
	 * 
	 * @return ContentValues representing the data object
	 */
	public ContentValues serialize();

	/**
	 * Deserialize a dataobject from a cursor, uses the current cursor position
	 * 
	 * @param c
	 *            the cursor
	 * 
	 * @return an object of type {@link T}
	 */
	public T deserialize(Cursor c);

	/**
	 * Inserts the current dataObject into the database
	 * 
	 * @param c
	 *            a context
	 * @return the rowid (or pk, if a pk has been defined) of the new entry
	 */
	public int insert(Context c);

	/**
	 * Updates the current dataObject in the database
	 * 
	 * @param c
	 *            a context
	 * @return the number of updates (i.e. when updating using a join all
	 *         changes should be accounted for)
	 */
	public int update(Context c);

	/**
	 * Deletes the current dataObject in the database
	 * 
	 * @param c
	 *            a context
	 * @return the number of deletes (i.e. when updating using a join all
	 *         changes should be accounted for)
	 */
	public int delete(Context c);

	/**
	 * Simple query method that returns an array of {@link T}s
	 * 
	 * @param c
	 *            a context
	 * @param input
	 *            an abitrary filter
	 * @return an array containing the results of the query
	 */
	public T[] query(Context c, Object input);

	/**
	 * Commits the current bulk insert
	 * 
	 * @param c
	 *            a contect object
	 * @return the number of items that were inserted into the database
	 */
	public int commitBulkInsert(Context c);

	/**
	 * Starts a transaction based bulk insert
	 * 
	 * @param itemsToInsert
	 *            the number of items to insert
	 */
	public void startBulkInsert(int itemsToInsert);

	/**
	 * Starts a transaction based bulk insert
	 */
	public void startBulkInsert();

	/**
	 * Adds an object to the current bulk insert
	 * 
	 * @param dataObject
	 *            an object of type {@link T} to be inserted when
	 *            {@link IDataBaseAdapater#commitBulkInsert(Context)} is called
	 */
	public void addToBulkInsert(T dataObject);

	/**
	 * Adds a list of objects to the current bulk insert
	 * 
	 * @param dataObjects
	 *            a list of type {@link T} to be inserted when
	 *            {@link IDataBaseAdapater#commitBulkInsert(Context)} is called
	 */
	public void addToBulkInsert(List<T> dataObjects);

	/**
	 * Adds an array of objects to the current bulk insert
	 * 
	 * @param dataObjects
	 *            an array of type {@link T} to be inserted when
	 *            {@link IDataBaseAdapater#commitBulkInsert(Context)} is called
	 */
	public void addToBulkInsert(T[] dataObjects);

	/**
	 * Adds items in a {@link Cursor} to the bulk update
	 * 
	 * @param c
	 *            the {@link Cursor} containing items to be bulk inserted
	 */
	public void addToBulkInsert(Cursor c);

	/**
	 * Returns the current dataObject
	 * 
	 * @return a dataObject of type {@link T}
	 */
	public T getDataObject();

	/**
	 * Sets the dataObject
	 * 
	 * @param dataObject
	 *            the dataObject to be set
	 */
	public void setDataObject(T dataObject);

	/**
	 * Sets the dataObject from a cursor
	 * 
	 * @param c
	 *            the cursor
	 */
	public void setDataObject(Cursor c);

}
