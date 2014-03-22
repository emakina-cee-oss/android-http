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

import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomX509TrustManager implements X509TrustManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomX509TrustManager.class.getSimpleName());

	private X509TrustManager originalTrustManager;

	private CustomX509TrustManager(X509TrustManager originalTrustManager) {
		this.originalTrustManager = originalTrustManager;
	}

	public static TrustManager[] getWrappedTrustmanager(TrustManager[] trustManagers) {
		TrustManager[] tm = new TrustManager[1];
		tm[0] = new CustomX509TrustManager((X509TrustManager) trustManagers[0]);
		return tm;
	}

	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return originalTrustManager.getAcceptedIssuers();
	}

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType) {
		try {
			originalTrustManager.checkClientTrusted(certs, authType);
		} catch (CertificateException e) {
			LOGGER.error("CertificateException", e);
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
		try {
			originalTrustManager.checkServerTrusted(certs, authType);
		} catch (CertificateExpiredException e) {
			LOGGER.error("CertificateExpiredException", e);
		} catch (CertificateException e) {
			if (manuallyValidate(certs)) {
				return;
			} else {
				LOGGER.error("CertificateException", e);
				throw e;
			}
		}
	}

	private boolean manuallyValidate(X509Certificate[] certs) {
		X509Certificate[] acceptedIssuers = getAcceptedIssuers();
		for (X509Certificate c : acceptedIssuers) {
			for (X509Certificate cert : certs) {
				if (c.getSerialNumber().equals(cert.getSerialNumber())) {
					if (Arrays.equals(c.getSignature(), cert.getSignature())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
