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
package at.diamonddogs.service.processor;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Abstract {@link ServiceProcessor} that should be used to process
 * {@link JSONArray}s
 * 
 * @param <OUTPUT>
 *            the type of the output object that is constructed from the input
 *            {@link JSONArray}
 */
public abstract class JSONArrayProcessor<OUTPUT> extends DataProcessor<JSONArray, OUTPUT> {

	@Override
	protected JSONArray createParsedObjectFromByteArray(byte[] data) {

		try {
			String jsonString = new String(data, 0, data.length);

			JSONArray jsonData = new JSONArray(jsonString);

			return jsonData;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
