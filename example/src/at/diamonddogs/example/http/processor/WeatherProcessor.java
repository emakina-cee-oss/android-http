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
package at.diamonddogs.example.http.processor;

import org.json.JSONObject;

import at.diamonddogs.example.http.dataobject.Weather;
import at.diamonddogs.exception.ProcessorExeception;
import at.diamonddogs.service.processor.JSONProcessor;

/**
 * All methods in this class, except getProcessorID(), will be executed in a
 * worker thread.
 * 
 * 
 */
public class WeatherProcessor extends JSONProcessor<Weather> {

	public static final int ID = 124345724;

	/**
	 * Callback method that will be called once a {@link JSONObject} has been
	 * created from the byte data contained in the reply
	 */
	@Override
	protected Weather parse(JSONObject inputObject) {
		try {
			Weather w = new Weather();
			w.setTemperature(inputObject.getJSONObject("main").getDouble("temp"));
			w.setText(inputObject.getJSONArray("weather").getJSONObject(0).getString("description"));
			w.setIcon(inputObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
			return w;
		} catch (Throwable tr) {
			// This ProcessorException will be handled automatically by the
			// DataProcessor super class
			throw new ProcessorExeception(tr);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProcessorID() {
		return ID;
	}
}
