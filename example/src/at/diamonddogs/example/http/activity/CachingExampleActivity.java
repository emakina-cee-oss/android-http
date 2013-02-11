/**
 * Copyright 2013, the diamond:dogs|group
 */
package at.diamonddogs.example.http.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.example.http.R;
import at.diamonddogs.service.net.HttpServiceAssister;

/**
 * 
 */
public class CachingExampleActivity extends Activity {
	private static final Uri RSS = Uri.parse("http://rss.golem.de/rss.php?tp=inet&feed=RSS2.0");

	private HttpServiceAssister assister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httpserviceassisterexampleactivity);
		assister = new HttpServiceAssister(this);
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

	private WebRequest createGetRssRequest(String term) {
		WebRequest wr = new WebRequest();
		wr.setUrl(RSS);

		// this is the important part, telling HttpService how long a WebRequest
		// will be saved
		wr.setCacheTime(CacheInformation.CACHE_1H);
		return wr;
	}
}
