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
package at.diamonddogs.example.http.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.example.http.R;
import at.diamonddogs.example.http.dataobject.WebComic;
import at.diamonddogs.example.http.processor.WebComicProcessor;
import at.diamonddogs.example.http.view.adapter.ImageLoadingExampleAdapter;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.ServiceProcessorMessageUtil;

/**
 * Demonstrates how to populate a {@link ListView} with images
 */
public class ImageLoadingExampleListActivity extends ListActivity {
	/**
	 * Base url for the RSS feed
	 */
	private static final String WEBCOMIC_RSS_URL = "http://www.commitstrip.com/en/feed/";

	/**
	 * The {@link HttpServiceAssister} to run all {@link WebRequest}s
	 */
	private HttpServiceAssister assister;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		assister = new HttpServiceAssister(this);
		setContentView(R.layout.imageloadingexamplelistactivity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		runGetRssWebRequest();
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

	private void runGetRssWebRequest() {
		WebRequest wr = new WebRequest();
		wr.setProcessorId(WebComicProcessor.ID);
		wr.setUrl("http://www.commitstrip.com/en/feed/");
		assister.runWebRequest(new Handler() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (ServiceProcessorMessageUtil.isSuccessful(msg) && ServiceProcessorMessageUtil.isFromProcessor(msg, WebComicProcessor.ID)) {
					WebComic wc = (WebComic) ServiceProcessorMessageUtil.getPayLoad(msg);
					setListAdapter(new ImageLoadingExampleAdapter(ImageLoadingExampleListActivity.this, assister, wc.getImagePaths()));
				}
			}
		}, wr, new WebComicProcessor());
	}

}
