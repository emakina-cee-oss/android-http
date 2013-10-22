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
package at.diamonddogs.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.net.WebClient.DownloadProgressListener;
import at.diamonddogs.service.net.HttpService.WebRequestReturnContainer;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 *
 */
public abstract class GenericImportService<T> extends IntentService {

	private static final String INTENT_ACTION_STARTIMPORT = "at.diamonddogs.service.GenericImportService.INTENT_ACTION_STARTIMPORT";

	private static final String INTENT_EXTRA_STATUS = "INTENT_EXTRA_STATUS";

	private static final int INTENT_EXTRA_STATUS_FAILED = 0;
	private static final int INTENT_EXTRA_STATUS_OK = 1;

	private static final String INTENT_EXTRA_PAYLOAD = "INTENT_EXTRA_PAYLOAD";

	private HttpServiceAssister assister;

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
	@Override
	protected void onHandleIntent(Intent intent) {
		WebRequestReturnContainer container = assister.runSynchronousWebRequest(getWebRequest(), getServiceProcessor(),
				getDownloadProgressListener());
		if (container.isSuccessful()) {

		} else {

		}
	}

	protected abstract WebRequest getWebRequest();

	protected abstract ServiceProcessor<T> getServiceProcessor();

	protected DownloadProgressListener getDownloadProgressListener() {
		return null;
	}

	public static final boolean wasSuccessful(Intent i) {
		return i.getIntExtra(INTENT_EXTRA_STATUS, INTENT_EXTRA_STATUS_FAILED) == INTENT_EXTRA_STATUS_OK;
	}

	public static final Object getPayloadFromIntent(Intent i) {
		if (!i.hasExtra(INTENT_EXTRA_PAYLOAD)) {
			return null;
		}
		return i.getSerializableExtra(INTENT_EXTRA_PAYLOAD);
	}

	public static final void startImport(Context c) {
		startImport(c, new Bundle());
	}

	public static final void startImport(Context c, Bundle extras) {
		Intent i = new Intent(INTENT_ACTION_STARTIMPORT);
		i.putExtras(extras);
		c.startService(i);
	}
}
