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
package at.diamonddogs.data.parser;

/**
 * General parser proxy, clean interface that can be used to (de)serialize data
 * 
 * @param <INPUT>
 *            the input type
 * @param <OUTPUT>
 *            the output type
 */
public abstract class ParserProxy<INPUT, OUTPUT> {

	/**
	 * The dataObject to be converted to INPUT
	 */
	protected OUTPUT dataObject;

	/**
	 * The source
	 */
	protected INPUT dataSource;

	/**
	 * Serializes the dataObject into an INPUT object
	 * 
	 * @return the INPUT object
	 */
	public abstract INPUT serialize();

	/**
	 * Deserializes an input object into an output object
	 * 
	 * @param input
	 *            the input object
	 * @return the output object
	 */
	public abstract OUTPUT deserialize(INPUT input);

	/**
	 * Gets the dataObject
	 * 
	 * @return the dataObject
	 */
	public OUTPUT getDataObject() {
		return dataObject;
	}

	/**
	 * Sets the dataObject
	 * 
	 * @param dataObject
	 *            the dataObject
	 */
	public void setDataObject(OUTPUT dataObject) {
		this.dataObject = dataObject;
	}
}
