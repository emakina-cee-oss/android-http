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
package at.diamonddogs.data.dataobjects;

import org.ksoap2.serialization.SoapSerializationEnvelope;

/**
 * {@link SoapRequest} represents a normal SOAP {@link WebRequest}
 */
public class SoapRequest extends WebRequest {
	/** the SOAP action */
	private String soapAction;

	/** the SOAP method */
	private String methodName;

	/** the SOAP namespace */
	private String namespace;

	/** the envelope related to this request */
	private SoapSerializationEnvelope envelope;

	/**
	 * Default constructor
	 */
	public SoapRequest() {

	}

	/**
	 * Creates a {@link SoapRequest} from a {@link WebRequest}
	 * 
	 * @param request
	 */
	public SoapRequest(WebRequest request) {
		this.processorId = request.processorId;
		this.url = request.url;
		this.readTimeout = request.readTimeout;
		this.connectionTimeout = request.connectionTimeout;
		this.followRedirects = request.followRedirects;
		this.header = request.header;
		this.cacheTime = request.cacheTime;
		this.numberOfRetries = request.numberOfRetries;
		this.retryInterval = request.retryInterval;
		this.tmpFile = request.tmpFile;
		this.httpEntity = request.httpEntity;
	}

	@SuppressWarnings("javadoc")
	public String getSoapAction() {
		return soapAction;
	}

	@SuppressWarnings("javadoc")
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	@SuppressWarnings("javadoc")
	public String getMethodName() {
		return methodName;
	}

	@SuppressWarnings("javadoc")
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	@SuppressWarnings("javadoc")
	public String getNamespace() {
		return namespace;
	}

	@SuppressWarnings("javadoc")
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@SuppressWarnings("javadoc")
	public SoapSerializationEnvelope getEnvelope() {
		return envelope;
	}

	@SuppressWarnings("javadoc")
	public void setEnvelope(SoapSerializationEnvelope envelope) {
		this.envelope = envelope;
	}
}
