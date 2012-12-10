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
package at.diamonddogs.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

/**
 * 
 */
public class SSLHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(SSLHelper.class.getSimpleName());
	private static SSLHelper INSTANCE;
	private SSLState sslState;

	public SSLSocketFactory SSL_FACTORY_APACHE = null;

	public javax.net.ssl.SSLSocketFactory SSL_FACTORY_JAVA = null;

	private SSLHelper() {
	}

	public static SSLHelper getInstance() {

		synchronized (SSLHelper.class) {

			if (INSTANCE == null) {
				INSTANCE = new SSLHelper();
				INSTANCE.sslState = new SSLState();
			}
			return INSTANCE;

		}
	}

	public boolean initSSLFactoryApache(Context c, int resourceId, String password) {
		try {
			if (c == null || resourceId == -1 || password == null) {
				LOGGER.info("No keystore specified, using alltrust");
				makeAllTrustManagerForApache();
				return true;
			} else {
				KeyStore store = getKeyStore(c, resourceId, password);
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				SSL_FACTORY_APACHE = new SSLSocketFactory(store);
				schemeRegistry.register(new Scheme("https", SSL_FACTORY_APACHE, 443));
				sslState.trustAll = false;
				return true;
			}
		} catch (Throwable tr) {
			LOGGER.warn("Error initializing SSLFactoryApache, trusting all certs", tr);
			try {
				makeAllTrustManagerForApache();
				sslState.tr = tr;
				return true;
			} catch (Throwable tr1) {
				sslState.tr1 = tr1;
				sslState.sslOk = false;
				LOGGER.warn("Error trusting all certs, no ssl connection possible", tr);
			}
			return false;
		}
	}

	private void makeAllTrustManagerForApache() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			KeyManagementException, UnrecoverableKeyException {
		KeyStore store;
		store = KeyStore.getInstance(KeyStore.getDefaultType());
		store.load(null, null);
		SSL_FACTORY_APACHE = new AllTrustingApacheSSLFactory(null);
	}

	public boolean initSSLFactoryJava(Context c, int resourceId, String password) {
		try {
			if (c == null || resourceId == -1 || password == null) {
				LOGGER.info("No keystore specified, using alltrust");
				makeAllTrustManagerForJava();
				return true;
			} else {
				KeyStore store = getKeyStore(c, resourceId, password);
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(store);
				SSLContext sslCtx = SSLContext.getInstance("TLS");
				sslCtx.init(null, tmf.getTrustManagers(), null);
				SSL_FACTORY_JAVA = sslCtx.getSocketFactory();
				sslState.trustAll = false;
				return true;
			}
		} catch (Throwable tr) {
			LOGGER.warn("Error initializing SSLFactoryJava", tr);
			try {
				makeAllTrustManagerForJava();
				sslState.tr = tr;
				return true;
			} catch (Throwable tr1) {
				sslState.tr1 = tr1;
				sslState.sslOk = false;
				LOGGER.warn("Error trusting all certs, no ssl connection possible", tr);
			}
			return false;
		}
	}

	private void makeAllTrustManagerForJava() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(null, getAllTrustingManager(), new java.security.SecureRandom());
		SSL_FACTORY_JAVA = sslCtx.getSocketFactory();
	}

	private TrustManager[] getAllTrustingManager() {
		return new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
	}

	private KeyStore getKeyStore(Context c, int resourceId, String password) throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException {
		KeyStore localTrustStore = KeyStore.getInstance("BKS");
		InputStream in = c.getResources().openRawResource(resourceId);
		localTrustStore.load(in, password.toCharArray());
		return localTrustStore;
	}

	public SSLState getSslState() {
		return sslState;
	}

	public final class AllTrustingApacheSSLFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public AllTrustingApacheSSLFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
				UnrecoverableKeyException {
			super(truststore);
			sslContext.init(null, getAllTrustingManager(), null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	public static final class SSLState {
		public Throwable tr;
		public Throwable tr1;
		public boolean trustAll = true;
		public boolean sslOk = true;

		@Override
		public String toString() {
			return "SSLState [tr=" + tr + ", tr1=" + tr1 + ", trustAll=" + trustAll + ", sslOk=" + sslOk + "]";
		}

	}

}
