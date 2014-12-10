/*
 * Copyright (C) 2013 the diamond:dogs|group
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
package at.diamonddogs.service.importservice;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister.HttpOrderedAsyncRequest;

/**
 * This import service supports the execution of {@link HttpOrderedAsyncRequest}
 * s.
 * 
 * @param <T>
 *            the type of data that is returned by this service
 */
public abstract class OrderedImportService<T extends Serializable> extends Service implements ImportService<T> {

	/**
	 * Logger ;)
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderedImportService.class.getSimpleName());

	/**
	 * The contract controlling the service's behaviour
	 */
	protected OrderedImportServiceContract<T> contract;

	/**
	 * The assister used to run the {@link HttpOrderedAsyncRequest}s
	 */
	private HttpOrderedAsyncAssister assister;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		assister = new HttpOrderedAsyncAssister(getApplicationContext());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (contract == null) {
			throw new NullPointerException("You forgot to set the contract");
		}
		if (!assister.bindService()) {
			LOGGER.warn("Could not bind HttpService");
			stopSelf();
			return START_NOT_STICKY;
		}
		contract.getBroadcastManager(intent).registerReceiver(contract.getBroadcastReceiver(intent), contract.getIntentFilter(intent));
		if (contract.shouldImport(intent)) {
			assister.runRequests(contract.getOrderedAsyncWebRequest(intent));
		} else {
			contract.sendImportSuccessful(intent, null);
			stopSelf();
		}
		return START_REDELIVER_INTENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		assister.safelyUnbindService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContract(ImportServiceContract<T> contract) {
		if (contract instanceof OrderedImportServiceContract<?>) {
			throw new IllegalArgumentException("The provided contract is not an OrderedImportServiceContract");
		}
		this.contract = (OrderedImportServiceContract<T>) contract;
	}

	@SuppressWarnings("javadoc")
	public HttpOrderedAsyncAssister getAssister() {
		return assister;
	}

}
