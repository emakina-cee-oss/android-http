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

import java.util.Arrays;

import android.content.ContentResolver;

/**
 * A Query implementation to be used in conjunction with {@link DatabaseAdapter}
 */
public class Query {
	/**
	 * An array containing the field names of all where parameters
	 */
	public String[] whereFields;
	/**
	 * An array containing the values to be checked
	 */
	public String[] whereValues;
	/**
	 * An array containing all operators for corresponding whereValues and
	 * whereFields
	 */
	public String[] whereOperators;
	/**
	 * Projection array
	 */
	public String[] projection = null;
	/**
	 * A sortorder
	 */
	public String sortOrder = null;

	private boolean validate() {
		if ((whereFields != null && whereFields != null) && (whereFields.length != whereValues.length)) {
			return false;
		}
		if (whereOperators != null && whereOperators.length != whereFields.length) {
			return false;
		}
		return true;
	}

	/**
	 * Create the selection parameter for
	 * {@link ContentResolver#query(android.net.Uri, String[], String, String[], String)}
	 * 
	 * @return a {@link String} containing the where clause
	 */
	public String createSelection() {
		if (!validate()) {
			throw new RuntimeException("Query invalid");
		}
		if (whereOperators == null) {
			whereOperators = new String[whereFields.length];
			Arrays.fill(whereOperators, 0, whereOperators.length, "=");
		}
		String ret = "";
		for (int i = 0; i < whereFields.length; i++) {
			ret += whereFields[0] + whereOperators[0] + "?";
		}
		return ret;
	}
}
