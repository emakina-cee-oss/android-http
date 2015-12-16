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

import org.kxml2.io.KXmlParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import at.diamonddogs.data.dataobjects.SoapReply;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.org.ksoap2.SoapEnvelope;
import at.diamonddogs.org.ksoap2.serialization.SoapSerializationEnvelope;
import at.diamonddogs.util.Log;


/**
 * Packs an instace of {@link WebReply} into a soap envelope
 */
public class SoapReplyAdapter {

	private static final String TAG = SoapReplyAdapter.class.getSimpleName();

	private SoapReply reply;

	/**
	 * Default constructor
	 * 
	 * @param webReply
	 *            the
	 */
	public SoapReplyAdapter(WebReply webReply) {
		reply = new SoapReply(webReply);
		initReply();
	}

	private void initReply() {
		try {
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
			parseResponse(envelope, reply.getData());
			reply.setEnvelope(envelope);
		} catch (Throwable tr) {
			Log.w(TAG, "Error parsing response", tr);
		}
	}

	/**
	 * Code taken (and modified) from ksoap2 - Transport.java | MIT
	 * 
	 * Sets up the parsing to hand over to the envelope to deserialize.
	 */
	private void parseResponse(SoapEnvelope envelope, byte[] data) throws XmlPullParserException, IOException {
		XmlPullParser xp = new KXmlParser();
		xp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		xp.setInput(new ByteArrayInputStream(data), null);
		envelope.parse(xp);
	}

	/**
	 * Returns the {@link SoapReply} contained in this adapter
	 * 
	 * @return a {@link SoapReply}
	 */
	public SoapReply getReply() {
		return reply;
	}
}
