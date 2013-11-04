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
package at.diamonddogs.service.processor;

import android.content.Context;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.net.HttpService;
import at.diamonddogs.service.processor.DataProcessor.ProcessingData;
import at.diamonddogs.util.CacheManager.CachedObject;

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
	 * @param c
	 *            a {@link Context} object
	 * @return the OUTPUT object like the one produced by
	 *         {@link DataProcessor#processData(byte[])} ->
	 *         {@link ProcessingData#output}
	 */
	public abstract OUTPUT obtainDataObjectFromWebReply(Context c, ReplyAdapter reply);

	/**
	 * This method is called by {@link HttpService} in order to allow post
	 * processing of cache data.
	 * 
	 * @param c
	 *            a {@link Context} object
	 * @param webRequest
	 *            the {@link WebRequest} resulting in this call
	 * @param object
	 *            the {@link CachedObject} retrieved from the cache
	 * @return an OUTPUT object
	 */
	public abstract OUTPUT obtainDataObjectFromCachedObject(Context c, WebRequest webRequest, CachedObject object);
}
