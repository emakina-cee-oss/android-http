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

/**
 * This interface should be implemented by all import services
 * 
 * @param <T>
 *            the type of data returned by the import service
 */
public interface ImportService<T extends Serializable> {
	/**
	 * Sets the contract for this import service
	 * 
	 * @param contract
	 *            the contract to use
	 */
	void setContract(ImportServiceContract<T> contract);

}
