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

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link ServiceProcessor} that should be used to process
 * {@link JSONObject}s
 * 
 * @param <OUTPUT>
 *            the type of the data object that will be constructed from the
 *            {@link JSONObject} input object
 */
public abstract class JSONProcessor<OUTPUT> extends DataProcessor<JSONObject, OUTPUT> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONProcessor.class.getSimpleName());

	@Override
	protected JSONObject createParsedObjectFromByteArray(byte[] data) {

		try {
			String jsonString = new String(data, 0, data.length);

			JSONObject jsonData = new JSONObject(jsonString);

			return jsonData;
		} catch (JSONException e) {
			LOGGER.error("Could not create JSON Object", e);
		}
		return null;

	}
}
