/*
 * Copyright (C) 2014 the diamond:dogs|group
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
package at.diamonddogs.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomSSLSocketFactory implements SocketFactory, LayeredSocketFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomSSLSocketFactory.class.getSimpleName());

	private SSLContext sslcontext = null;

	public CustomSSLSocketFactory(KeyStore store) {
		super();
		sslcontext = createCustomSSLContext(store);
	}

	private SSLContext createCustomSSLContext(KeyStore store) {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(store);

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, CustomX509TrustManager.getWrappedTrustmanager(tmf.getTrustManagers()), null);
			return context;
		} catch (Exception e) {
			LOGGER.error("unable to create ssl context", e);
			return null;
		}
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket,
	 *      java.lang.String, int, java.net.InetAddress, int,
	 *      org.apache.http.params.HttpParams)
	 */
	@Override
	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);
		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

		if ((localAddress != null) || (localPort > 0)) {
			// we need to bind explicitly
			if (localPort < 0) {
				localPort = 0; // indicates "any"
			}
			InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
			sslsock.bind(isa);
		}

		sslsock.connect(remoteAddress, connTimeout);
		sslsock.setSoTimeout(soTimeout);
		return sslsock;
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
	 */
	@Override
	public Socket createSocket() throws IOException {
		if (sslcontext != null) {
			return sslcontext.getSocketFactory().createSocket();
		} else {
			try {
				return SSLContext.getDefault().getSocketFactory().createSocket();
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
	 */
	@Override
	@Deprecated
	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}

	/**
	 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
	 *      java.lang.String, int, boolean)
	 */
	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		if (sslcontext != null) {
			return sslcontext.getSocketFactory().createSocket(socket, host, port, autoClose);
		} else {
			try {
				return SSLContext.getDefault().getSocketFactory().createSocket(socket, host, port, autoClose);
			} catch (Exception e) {
				return null;
			}
		}
	}

	// -------------------------------------------------------------------
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :
	// Both Object.equals() and Object.hashCode() must be overridden
	// for the correct operation of some connection managers
	// -------------------------------------------------------------------

	@Override
	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(CustomSSLSocketFactory.class));
	}

	@Override
	public int hashCode() {
		return CustomSSLSocketFactory.class.hashCode();
	}
}
