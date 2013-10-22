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
package at.diamonddogs.example.http.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.example.http.R;
import at.diamonddogs.example.http.processor.RssProcessor;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * A simple caching example
 */
public class CachingExampleActivity extends ListActivity {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingExampleActivity.class.getSimpleName());

	private static final Uri RSS = Uri.parse("http://rss.golem.de/rss.php?tp=inet&feed=RSS2.0");

	private HttpServiceAssister assister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cachingexampleactivity);
		assister = new HttpServiceAssister(this);
		assister.runWebRequest(new RssHandler(), createGetRssRequest(), new RssProcessor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		assister.bindService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPause() {
		super.onPause();
		assister.unbindService();
	}

	private WebRequest createGetRssRequest() {
		WebRequest wr = new WebRequest();
		wr.setUrl(RSS);
		wr.setProcessorId(RssProcessor.ID);

		// This is the important part, telling HttpService how long a WebRequest
		// will be saved. Since RssProcessor extends XMLProcessor, which extends
		// DataProcessor, the WebRequest's data will be cached automatically,
		// provided that cacheTime is not CACHE_NO.
		wr.setCacheTime(5000);

		// Enables offline caching. usually, cache data is deleted on retrieval
		// if it has expired even if the device is not online. If this flag is
		// set to true, cache data will not be removed if it has expired as long
		// as the device was offline during the request
		wr.setUseOfflineCache(true);
		return wr;
	}

	private final class RssHandler extends Handler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == RssProcessor.ID) {
				if (msg.arg1 == ServiceProcessor.RETURN_MESSAGE_OK) {
					String[] items = (String[]) msg.obj;
					setListAdapter(new ArrayAdapter<String>(CachingExampleActivity.this, android.R.layout.simple_list_item_1,
							android.R.id.text1, items));
					Toast.makeText(CachingExampleActivity.this,
							"From cache -> " + msg.getData().getSerializable(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_FROMCACHE),
							Toast.LENGTH_SHORT).show();
				} else {
					LOGGER.error("An error has occured.", msg.getData().getSerializable(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_THROWABLE));
				}
			}
		}
	}
}
