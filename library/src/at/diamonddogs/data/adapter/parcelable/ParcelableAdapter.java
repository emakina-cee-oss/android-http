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
package at.diamonddogs.data.adapter.parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base class for any parcelable adapter. Allows seperation of data objects and
 * parcelation code.
 * 
 * @param <T>
 *            the type of object to be parceled
 */
public abstract class ParcelableAdapter<T> implements Parcelable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParcelableAdapter.class);

	protected T dataObject;

	/**
	 * Default constructor
	 */
	public ParcelableAdapter() {
	}

	/**
	 * Android callback constructor
	 * 
	 * @param in
	 *            the parcel to be used to build the data object
	 */
	public ParcelableAdapter(Parcel in) {

	}

	/**
	 * Constructor that should be used to create a parcelable adapter for the
	 * supplied data object
	 * 
	 * @param dataObject
	 */
	public ParcelableAdapter(T dataObject) {
		this.dataObject = dataObject;
	}

	/**
	 * Returns the current data object
	 * 
	 * @return a data object
	 */
	public T getDataObject() {
		return dataObject;
	}

	/**
	 * Sets the current data object
	 * 
	 * @param dataObject
	 *            a data object
	 */
	public void setDataObject(T dataObject) {
		this.dataObject = dataObject;
	}

	/**
	 * Creates an array of {@link ParcelableAdapter}s containing the data
	 * objects provided by the input array. Each item of the input array will be
	 * wrapped in an instance of {@link ParcelableAdapter}
	 * 
	 * @param <V>
	 *            the type of the {@link ParcelableAdapter} to be used to wrap
	 *            the data objects
	 * @param parcelableAdapterClass
	 *            the class object of the {@link ParcelableAdapter} specified by
	 *            <V>
	 * @param input
	 *            an input array
	 * @param output
	 *            the output array. Should be empty, all objects will be
	 *            overwritten by this method.
	 * @return an array of {@link ParcelableAdapter} objects containing the data
	 *         objects of input
	 */
	@SuppressWarnings("unchecked")
	public <V extends ParcelableAdapter<T>> V[] getParcelableAdapterArray(Class<?> parcelableAdapterClass, T[] input, V[] output) {
		if (input == null) {
			throw new IllegalArgumentException("Input may not be null");
		}
		if (output == null) {
			LOGGER.warn("output was null, not parcelling -> returning null");
			return output;
		}
		if (output.length != input.length) {
			throw new IllegalArgumentException("Input / Output length mismatch or null");
		}

		try {
			for (int i = 0; i < input.length; i++) {
				output[i] = (V) parcelableAdapterClass.newInstance();
				output[i].dataObject = input[i];
			}
		} catch (Throwable tr) {
			LOGGER.warn("Error while parcelling " + input, tr);
		}

		return output;
	}

	/**
	 * Same as
	 * {@link ParcelableAdapter#getParcelableAdapterArray(Class, Object[], ParcelableAdapter[])}
	 * , but the output will not be filled automatically.
	 * 
	 * @param <V>
	 *            the type of the {@link ParcelableAdapter} to be used to wrap
	 *            the data objects
	 * @param parcelableAdapterClass
	 *            the class object of the {@link ParcelableAdapter} specified by
	 *            <V>
	 * @param input
	 *            an input array
	 * @param output
	 *            the output array. MUST NOT be empty
	 * @return an array of {@link ParcelableAdapter} objects containing the data
	 *         objects of input
	 */
	@SuppressWarnings("unchecked")
	public <V extends ParcelableAdapter<T>> T[] getArrayFromParcelableArray(Parcelable[] input, T[] output) {
		if (input == null) {
			throw new IllegalArgumentException("Input may not be null");
		}
		if (output == null) {
			LOGGER.warn("output was null, not parcelling -> returning null");
			return output;
		}
		if (output.length != input.length) {
			throw new IllegalArgumentException("Input / Output length mismatch or null");
		}

		for (int i = 0; i < input.length; i++) {
			output[i] = ((V) input[i]).dataObject;
		}

		return output;
	}

	protected void writeHeaderMap(Parcel dest, Map<String, List<String>> map) {
		Set<String> k = map.keySet();
		String[] keys = k.toArray(new String[k.size()]);

		Collection<List<String>> values = map.values();

		Iterator<List<String>> iterator = values.iterator();
		int length = values.size();

		Bundle b = new Bundle();
		b.putStringArray("keys", keys);
		b.putInt("length", length);
		for (int i = 0; i < values.size(); i++) {
			List<String> l = iterator.next();
			ArrayList<String> list = new ArrayList<String>(l);
			b.putStringArrayList("" + i, list);
		}
		dest.writeBundle(b);
	}

	protected Map<String, List<String>> readHeaderMap(Parcel dest, Map<String, List<String>> list) {

		Map<String, List<String>> map = new HashMap<String, List<String>>();
		Bundle b = dest.readBundle();
		String[] keys = b.getStringArray("keys");
		int length = b.getInt("length");
		for (int i = 0; i < length; i++) {
			List<String> l = b.getStringArrayList("" + i);
			map.put(keys[i], l);
		}
		return map;
	}

	protected void writeStringMap(Parcel dest, Map<String, String> map) {
		// Why so compilcated?
		// 1) parcel.readMap(..) needs a classloader ... WTF
		// 2) even though string array sizes are stored in the parcel (by
		// default!) you need to know the size when reading the array (ergo
		// saving the size twice)
		// 3) bundle handles stuff differently ... see below
		// 4) we are sorry for this mess, but we consider it to be android's
		// fault
		if (map != null) {
			Set<String> k = map.keySet();
			String[] keys = k.toArray(new String[k.size()]);
			Collection<String> v = map.values();
			String[] values = v.toArray(new String[v.size()]);

			Bundle b = new Bundle();
			b.putStringArray("keys", keys);
			b.putStringArray("values", values);

			dest.writeBundle(b);
		}
	}

	protected Map<String, String> readStringMap(Parcel in) {
		Bundle b = in.readBundle();
		String[] keys = b.getStringArray("keys");
		String[] values = b.getStringArray("values");
		HashMap<String, String> map = new HashMap<String, String>();

		if (keys != null && values != null) {
			if (keys.length != values.length) {
				throw new IllegalStateException("Key & Value map don't match");
			}

			for (int i = 0; i < keys.length; i++) {
				map.put(keys[i], values[i]);
			}
		}
		return map;
	}
}
