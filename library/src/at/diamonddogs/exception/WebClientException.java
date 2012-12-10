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

import at.diamonddogs.net.WebClient;

/**
 * An {@link Exception} thrown by {@link WebClient}s
 */
public class WebClientException extends RuntimeException {

	private static final long serialVersionUID = 1602076872190690882L;

	/**
	 * Default Constructor
	 */
	public WebClientException() {
		super();
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public WebClientException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	/**
	 * 
	 * @param detailMessage
	 */
	public WebClientException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * 
	 * @param throwable
	 */
	public WebClientException(Throwable throwable) {
		super(throwable);
	}

}
