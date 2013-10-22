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
package at.diamonddogs.util;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import at.diamonddogs.data.dataobjects.WebRequest;

/**
 * A helper class that allows programmers to verify connectivity according to
 * {@link WebRequest} defined rules
 */
public class ConnectivityHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityHelper.class.getSimpleName());

	/**
	 * An instance of {@link ConnectivityManager} used to check for connectivity
	 * (o rly?! :>)
	 */
	private ConnectivityManager connectivityManager;

	/**
	 * A {@link Context} object
	 */
	private Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            a {@link Context} object
	 */
	public ConnectivityHelper(Context context) {
		this.context = context;
		this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Checks the connectivity of the device according to the rules specified in
	 * the {@link WebRequest} and the permissions the application has. If
	 * permissions are not sufficient, <code>true</code> will be returned as a
	 * fallback value.
	 * 
	 * @param wr
	 *            the {@link WebRequest} whose rules will determine if the
	 *            connectivity test fails
	 * @return <code>true</code> if the device is connected according to the
	 *         rules provided in {@link WebRequest}
	 */
	public boolean checkConnectivityWebRequest(WebRequest wr) {
		return isConnected(wr) && isPingAble(wr);
	}

	private boolean isConnected(WebRequest wr) {
		if (wr.isCheckConnectivity()) {
			if (!hasAccessNetworkStatePermission()) {
				// @formatter:off
				LOGGER.warn("WebRequest (" + wr.getUrl() + ", " + wr.getId() + ") requested a connectivity check, but the caller lacks the required permission (ACCESS_NETWORK_STATE). Returning true");
				// @formatter:on
				return true;
			}
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			return networkInfo == null ? false : networkInfo.isConnected();
		} else {
			LOGGER.info("WebRequest does not require connectivity check, returning true");
			return true;
		}
	}

	private boolean isPingAble(WebRequest wr) {
		if (wr.isCheckConnectivityPing()) {
			if (!hasChangeNetworkStatePermission()) {
				// @formatter:off
				LOGGER.warn("WebRequest (" + wr.getUrl() + ", " + wr.getId() + ") requested a connectivity PING check, but the caller lacks the required permission (CHANGE_NETWORK_STATE). Returning true");
				// @formatter:on
				return true;
			}

			InetAddress addr;
			try {
				addr = InetAddress.getByName(wr.getUrl().getHost());
				return addr.isReachable(5000);
			} catch (Throwable tr) {
				LOGGER.warn("Hostname could not be resolved and therefore not be pinged. Returning false", tr);
				return false;
			}
		} else {
			LOGGER.info("WebRequest does not require connectivity check, returning true");
			return true;
		}
	}

	private boolean hasAccessNetworkStatePermission() {
		return context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
	}

	private boolean hasChangeNetworkStatePermission() {
		return context.checkCallingOrSelfPermission(android.Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
	}
}
