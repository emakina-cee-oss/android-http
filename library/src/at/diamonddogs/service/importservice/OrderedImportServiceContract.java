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
package at.diamonddogs.service.importservice;

import java.io.Serializable;

import android.content.Intent;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister.HttpOrderedAsyncRequest;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * The {@link ImportServiceContract} for {@link OrderedImportService}s.
 * 
 * @param <T>
 *            the type of data returned by the import server
 */
public abstract class OrderedImportServiceContract<T extends Serializable> extends ImportServiceContract<T> {

	/**
	 * @deprecated This method is _not_ used in ordered imports
	 */
	@Deprecated
	@Override
	protected final WebRequest getWebRequest(Intent i) {
		return null;
	}

	/**
	 * @deprecated This method is _not_ used in ordered imports
	 */
	@Deprecated
	@Override
	protected final ServiceProcessor<T> getServiceProcessor(Intent i) {
		return null;
	}

	/**
	 * Gets the import {@link HttpOrderedAsyncRequest}
	 * 
	 * @param i
	 *            the intent used to start the service
	 * @return a {@link HttpOrderedAsyncRequest}
	 */
	protected abstract HttpOrderedAsyncRequest getOrderedAsyncWebRequest(Intent i);
}
