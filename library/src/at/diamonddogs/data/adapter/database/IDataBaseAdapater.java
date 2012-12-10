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

	public T[] query(Context c, Object input);

	public int commitBulkInsert(Context c);

	public void startBulkInsert(int itemsToInsert);

	public void startBulkInsert();

	public void addToBulkInsert(T dataObject);

	public void addToBulkInsert(List<T> dataObjects);

	public void addToBulkInsert(T[] dataObjects);

	public void addToBulkInsert(Cursor c);

	public T getDataObject();

	public void setDataObject(T dataObject);

	public void setDataObject(Cursor c);

}
