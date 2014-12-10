/*
 * Copyright (C) 2013 the diamond:dogs|group
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


/**
 * This processor dispatches the raw data to the caller.
 */
public class RawDataProcessor extends DataProcessor<byte[], byte[]> {
	public static final int ID = 32917626;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected byte[] createParsedObjectFromByteArray(byte[] data) {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected byte[] parse(byte[] inputObject) {
		return inputObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProcessorID() {
		return 32917626;
	}

}
