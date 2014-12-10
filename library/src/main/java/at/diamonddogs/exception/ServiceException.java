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
package at.diamonddogs.exception;

import android.app.Service;

/**
 * This {@link RuntimeException} will be thrown by {@link Service}s
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 99569179782163929L;

	/**
	 * Default Constructor
	 */
	public ServiceException() {
		super();
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public ServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	/**
	 * @param detailMessage
	 */
	public ServiceException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public ServiceException(Throwable throwable) {
		super(throwable);
	}

}
