/**
 * Copyright 2013, the diamond:dogs|group
 */
package at.diamonddogs.data.adapter.soap;

import org.kxml2.io.KXmlParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import at.diamonddogs.data.dataobjects.SoapReply;
import at.diamonddogs.org.ksoap2.SoapEnvelope;
import at.diamonddogs.org.ksoap2.serialization.SoapSerializationEnvelope;
import at.diamonddogs.util.Log;

/**
 * @author siyb
 * 
 */
public class SoapByteArrayAdapter {
	private static final String TAG = SoapByteArrayAdapter.class.getSimpleName();

	private byte[] data;
	private SoapSerializationEnvelope envelope;

	/**
	 * Default constructor
	 * 
	 * @param data
	 *            the data
	 */
	public SoapByteArrayAdapter(byte[] data) {
		this.data = data;
		initReply();
	}

	private void initReply() {
		try {
			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
			parseResponse(envelope, data);
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
	public SoapSerializationEnvelope getEnvelope() {
		return envelope;
	}
}
