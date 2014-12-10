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
package at.diamonddogs.util;

import java.io.PrintWriter;
import java.io.Writer;

import org.ksoap2.SoapEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * This class holds SOAP related util methods
 */
public class SoapUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SoapUtil.class);

	/**
	 * Prints the given {@link SoapEnvelope} to stderr
	 * 
	 * @param e
	 *            the envelope to print
	 */
	public static final void printSoapEnvelopeToStderr(SoapEnvelope e) {
		printSoapEnvelope(e, new PrintWriter(System.err));
	}

	/**
	 * Prints the given {@link SoapEnvelope} to stdout
	 * 
	 * @param e
	 *            the envelope to print
	 */
	public static final void printSoapEnvelopeToStdout(SoapEnvelope e) {
		printSoapEnvelope(e, new PrintWriter(System.out));
	}

	/**
	 * Prints the given {@link SoapEnvelope} to the provided {@link Writer}
	 * instance
	 * 
	 * @param e
	 *            the envelope to print
	 * @param w
	 *            the write to print the envelope to
	 */
	public static final void printSoapEnvelope(SoapEnvelope e, Writer w) {
		try {
			XmlSerializer x = XmlPullParserFactory.newInstance().newSerializer();
			x.setOutput(w);
			e.write(x);
			x.flush();
		} catch (Throwable tr) {
			LOGGER.warn("Could not print envelope!", tr);
		}
	}
}
