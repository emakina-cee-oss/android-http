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

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.net.WebClient.DownloadProgressListener;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * This contract describes the functionality that needs to be implemented in
 * order to make any import service work.
 * 
 * @param <T>
 *            the type of data that is returned by the import
 */
public abstract class ImportServiceContract<T extends Serializable> {
	/**
	 * Start import action
	 */
	private static final String INTENT_ACTION_STARTIMPORT = "at.diamonddogs.service.GenericImportService.INTENT_ACTION_STARTIMPORT";

	/**
	 * A default category for the start {@link Intent}. Users of
	 * {@link ImportServiceContract} should really provide an own category in
	 * order to distinguish between import intents
	 */
	private static final String INTENT_CATEGORY_STARTIMPORTDEFAULTCATEGORY = "at.diamonddogs.service.GenericImportService.INTENT_CATEGORY_STARTIMPORTDEFAULTCATEGORY";

	/**
	 * The action of the {@link Intent} that will be send when the import has
	 * completed successfully
	 */
	private static final String INTENT_ACTION_IMPORTCOMPLETED = "at.diamonddogs.service.GenericImportService.INTENT_ACTION_IMPORTCOMPLETED";

	/**
	 * Return status of the import (key)
	 */
	private static final String INTENT_EXTRA_STATUS = "INTENT_EXTRA_STATUS";

	/**
	 * Failed import status
	 */
	private static final int INTENT_EXTRA_STATUS_FAILED = 0;

	/**
	 * Successful import status
	 */
	private static final int INTENT_EXTRA_STATUS_OK = 1;

	/**
	 * Import payload (key)
	 */
	private static final String INTENT_EXTRA_PAYLOAD = "INTENT_EXTRA_PAYLOAD";

	protected Intent getSuccessfulImportIntent(T payload) {
		Intent i = new Intent(INTENT_ACTION_IMPORTCOMPLETED);
		i.putExtra(INTENT_EXTRA_STATUS, INTENT_EXTRA_STATUS_OK);
		i.putExtra(INTENT_EXTRA_PAYLOAD, payload);
		return i;
	}

	protected Intent getFailedImportIntent() {
		Intent i = new Intent();
		i.putExtra(INTENT_EXTRA_STATUS, INTENT_EXTRA_STATUS_FAILED);
		return i;
	}

	/**
	 * Checks if an import is required
	 * 
	 * @param i
	 *            the {@link Intent} used to start the {@link Service}
	 * @return <code>true</code> if the import should be started,
	 *         <code>false</code> otherwise
	 */
	protected abstract boolean shouldImport(Intent i);

	/**
	 * Returns the instance of {@link GenericBroadcastManager}, usually this
	 * method will return a wrapped LocalBroadcastManager. By doing so, there is
	 * no need to introduce a compatibility library dependency
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @return a {@link GenericBroadcastManager}
	 */
	protected abstract GenericBroadcastManager getBroadcastManager(Intent i);

	/**
	 * Returns the instance of {@link BroadcastReceiver} that should receive
	 * information on import state
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @return an instance of {@link BroadcastReceiver}
	 */
	protected abstract BroadcastReceiver getBroadcastReceiver(Intent i);

	/**
	 * Returns the {@link IntentFilter} that the {@link BroadcastReceiver}
	 * returned by {@link ImportServiceContract#getBroadcastReceiver(Intent)}
	 * will be registered to.
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @return an {@link IntentFilter}
	 */
	protected IntentFilter getIntentFilter(Intent i) {
		IntentFilter intentFilter = new IntentFilter(INTENT_ACTION_IMPORTCOMPLETED);
		return intentFilter;
	}

	/**
	 * Sends the import failed broadcast
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 */
	public void sendImportFailedIntent(Intent i) {
		getBroadcastManager(i).sendBroadcast(getFailedImportIntent());
	}

	/**
	 * Sends the import successful import
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @param payload
	 *            the payload
	 */
	public void sendImportSuccessful(Intent i, T payload) {
		getBroadcastManager(i).sendBroadcast(getSuccessfulImportIntent(payload));
	}

	/**
	 * Returns the import {@link WebRequest}
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @return the {@link WebRequest}
	 */
	protected abstract WebRequest getWebRequest(Intent i);

	/**
	 * Returns the {@link ServiceProcessor} used by the
	 * {@link GenericImportService}
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @return an instance of {@link ServiceProcessor}
	 */
	protected abstract ServiceProcessor<T> getServiceProcessor(Intent i);

	/**
	 * The {@link DownloadProgressListener} returned by this method will receive
	 * updates related to the import
	 * 
	 * @param i
	 *            the {@link Intent} that was used to start the service
	 * @return a {@link DownloadProgressListener}
	 */
	protected DownloadProgressListener getDownloadProgressListener(Intent i) {
		return null;
	}

	/**
	 * Checks if the import was successful. The {@link Intent} passed to this
	 * method must be the broadcasted intent received after the imported
	 * completed.
	 * 
	 * @param i
	 *            the broadcasted {@link Intent}
	 * @return <code>true</code> if the import was successful,
	 *         <code>false</code> otherwise
	 */
	public static final boolean wasSuccessful(Intent i) {
		return i.getIntExtra(INTENT_EXTRA_STATUS, INTENT_EXTRA_STATUS_FAILED) == INTENT_EXTRA_STATUS_OK;
	}

	/**
	 * Returns the payload of the broadcast {@link Intent}.
	 * 
	 * @param <T>
	 *            the type of payload, should be the same as the type defined by
	 *            the implementation of {@link GenericImportService}.
	 * @param i
	 *            the broadcasted {@link Intent}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T getPayloadFromIntent(Intent i) {
		if (!i.hasExtra(INTENT_EXTRA_PAYLOAD)) {
			return null;
		}
		return (T) i.getSerializableExtra(INTENT_EXTRA_PAYLOAD);
	}

	/**
	 * Start the import
	 * 
	 * @param c
	 *            a {@link Context}
	 */
	public static final void startImport(Context c) {
		startImport(c, new Bundle(), INTENT_CATEGORY_STARTIMPORTDEFAULTCATEGORY);
	}

	/**
	 * Start the import
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param extras
	 *            optional extras that will set into the {@link Intent} to be
	 *            passed to {@link IntentService#onHandleIntent(Intent)}
	 * @param category
	 *            a category for this import
	 */
	public static final void startImport(Context c, Bundle extras, String category) {
		Intent i = new Intent(INTENT_ACTION_STARTIMPORT);
		i.putExtras(extras);
		i.addCategory(category);
		c.sendBroadcast(i);
	}

	/**
	 * This interface provides a contract for implementations that are able to
	 * send local broadcasts.
	 */
	public interface GenericBroadcastManager {
		/**
		 * Sends the broadcast
		 * 
		 * @param i
		 *            the {@link Intent} to broadcast
		 * @return <code>true</code> if the broadcast was handled by any
		 *         {@link BroadcastReceiver}s, <code>false</code> otherwise
		 */
		public boolean sendBroadcast(Intent i);

		/**
		 * Register a {@link BroadcastReceiver}
		 * 
		 * @param receiver
		 *            the {@link BroadcastReceiver} to register
		 * @param filter
		 *            the {@link IntentFilter} that describes the events that
		 *            the {@link BroadcastReceiver} will receive
		 */
		public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter);
	}
}
