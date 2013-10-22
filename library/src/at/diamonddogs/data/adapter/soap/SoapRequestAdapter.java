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
package at.diamonddogs.data.adapter.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.entity.ByteArrayEntity;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.kxml2.io.KXmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlSerializer;

import at.diamonddogs.data.dataobjects.SoapRequest;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;

/**
 * 
 */
public class SoapRequestAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SoapRequestAdapter.class);
	private static final String USERAGENT = "android-http";

	private String xmlVersionTag = "";
	private SoapRequest request;

	/**
	 * Default constructor
	 * 
	 * @param webRequest
	 *            the {@link WebRequest} to be soapanized
	 * @param soapAction
	 *            the SOAP action
	 * @param envelope
	 *            the envelope
	 */
	public SoapRequestAdapter(WebRequest webRequest, String soapAction, SoapSerializationEnvelope envelope) {
		request = new SoapRequest(webRequest);
		initRequest(soapAction, envelope);
	}

	private void initRequest(String soapAction, SoapSerializationEnvelope envelope) {
		byte[] requestData = new byte[1];
		try {
			requestData = createRequestData(envelope);
		} catch (Throwable tr) {
			LOGGER.warn("Could not create requestdata from envelope", tr);
		}
		request.setRequestType(Type.POST);
		request.addHeaderField("User-Agent", USERAGENT);
		request.addHeaderField("SOAPAction", soapAction);
		request.addHeaderField("Content-Type", "text/xml");
		request.addHeaderField("Connection", "close");
		request.addHeaderField("Content-Length", String.valueOf(requestData.length));
		request.setHttpEntity(new ByteArrayEntity(requestData));
		request.setEnvelope(envelope);
	}

	/**
	 * Code taken from ksoap2 - Transport.java | MIT
	 * 
	 * Serializes the request.
	 */
	private byte[] createRequestData(SoapEnvelope envelope) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(xmlVersionTag.getBytes());
		XmlSerializer xw = new KXmlSerializer();
		xw.setOutput(bos, null);
		envelope.write(xw);
		xw.flush();
		bos.write('\r');
		bos.write('\n');
		return bos.toByteArray();
	}

	/**
	 * Code taken from ksoap2 - Transport.java | MIT
	 * 
	 * Sets the version tag for the outgoing soap call. Example <?xml
	 * version=\"1.0\" encoding=\"UTF-8\"?>
	 * 
	 * @param tag
	 *            the xml string to set at the top of the soap message.
	 */
	public void setXmlVersionTag(String tag) {
		xmlVersionTag = tag;
	}

	/**
	 * Returns the usable {@link SoapRequest}
	 * 
	 * @return a {@link SoapRequest}
	 */
	public SoapRequest getRequest() {
		return request;
	}
}
