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
import android.content.Intent;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.net.HttpService.WebRequestReturnContainer;
import at.diamonddogs.service.net.HttpServiceAssister;

/**
 * A generic import service to be used for normal {@link WebRequest}. The
 * behaviour of {@link GenericImportService} is controlled by the provided
 * {@link ImportServiceContract}, which is fully customizable.
 * 
 * @param <T>
 *            the type of data this service imports
 */
public class GenericImportService<T extends Serializable> extends IntentService implements ImportService<T> {

	/**
	 * The contract for all import services
	 */
	protected ImportServiceContract<T> contract;

	/**
	 * An instance of {@link HttpServiceAssister} that is used to execute the
	 * import {@link WebRequest}
	 */
	private HttpServiceAssister assister;

	/**
	 * Default constructor
	 */
	public GenericImportService() {
		super(GenericImportService.class.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		assister = new HttpServiceAssister(this);

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {
		if (contract == null) {
			throw new NullPointerException("You forgot to set the contract");
		}
		contract.getBroadcastManager(intent).registerReceiver(contract.getBroadcastReceiver(intent), contract.getIntentFilter(intent));
		if (contract.shouldImport(intent)) {
			WebRequestReturnContainer container = assister.runSynchronousWebRequest(contract.getWebRequest(intent),
					contract.getServiceProcessor(intent), contract.getDownloadProgressListener(intent));
			if (container.isSuccessful()) {
				contract.sendImportSuccessful(intent, (T) container.getPayload());
			} else {
				contract.sendImportFailedIntent(intent);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContract(ImportServiceContract<T> contract) {
		this.contract = contract;
	}

}
