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

import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.processor.DataProcessor.ProcessingData;

/**
 * {@link SynchronousProcessor} has to be implemented by all processors that
 * wish to handle synchronous {@link WebRequest}
 * 
 * @param <OUTPUT>
 *            the type of object that will be returned by
 *            {@link SynchronousProcessor#obtainDataObjectFromWebReply(ReplyAdapter)}
 */
public interface SynchronousProcessor<OUTPUT> {
	/**
	 * Creates an OUTPUT object from a given {@link ReplyAdapter}, this method
	 * should be used for synchronous {@link WebRequest} only!
	 * 
	 * @param reply
	 *            a {@link ReplyAdapter}
	 * @return the OUTPUT object like the one produced by
	 *         {@link DataProcessor#processData(byte[])} ->
	 *         {@link ProcessingData#output}
	 */
	public abstract OUTPUT obtainDataObjectFromWebReply(ReplyAdapter reply);
}
