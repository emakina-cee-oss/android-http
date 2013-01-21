package at.diamonddogs.example.http.activity;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;
import at.diamonddogs.example.http.R;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.HeadRequestProcessor;

/**
 * {@link HttpserviceassisterExampleActivity} illustrates the use of the
 * {@link HttpServiceAssister}
 */
public class HttpserviceassisterExampleActivity extends Activity {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpserviceassisterExampleActivity.class.getSimpleName());

	private HttpServiceAssister assister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httpserviceassisterexample);
		assister = new HttpServiceAssister(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();
		assister.bindService();
		String weatherUrl = getWeatherUrl("Austria", "Vienna");
		LOGGER.info("URL: " + weatherUrl);

		// we can start WebRequests without having to wait for service binding.
		// HttpServiceAssister will queue asynchronous WebRequests and execute
		// them once a connection has been established. The ServiceProcessor
		// will automatically be registered as well.

		// --- SYNC WEB REQUEST

		// sync webrequest POC, usually you should not execute synchronous web
		// requests on the main thread. The result of this call will be logged,
		// but not displayed in the UI. Please be aware of the fact that this
		// call will wait for a the service binging as well as the actual
		// WebRequest.
		WebRequest syncWebRequest = new WebRequest();
		syncWebRequest.setUrl(weatherUrl);
		syncWebRequest.setRequestType(Type.HEAD);

		// default header request processor
		syncWebRequest.setProcessorId(HeadRequestProcessor.ID);

		// required for HEAD request (yahoo specific!)
		syncWebRequest.addHeaderField("Accept-Encoding", "gzip, deflate");

		Map<String, List<String>> headers = (Map<String, List<String>>) assister.runSynchronousWebRequest(syncWebRequest,
				new HeadRequestProcessor());
		if (headers != null) {
			for (String key : headers.keySet()) {
				LOGGER.error("KEY -> " + key);
				for (String value : headers.get(key)) {
					LOGGER.error("    VALUE -> " + value);
				}
			}
		} else {
			Toast.makeText(this, "Error while optaining headers", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPause() {
		super.onPause();
		assister.unbindService();
	}

	/**
	 * Formats the yahoo weather URL
	 * 
	 * @param country
	 *            the country
	 * @param city
	 *            the city
	 * @return the weather url for country & city
	 */
	private String getWeatherUrl(String country, String city) {
		Uri u = Uri.parse("http://query.yahooapis.com/v1/public/yql");
		// @formatter:off
		u = u.buildUpon()
			.appendQueryParameter("q", "select * from weather.forecast where location in (select id from weather.search where query=\""+country+","+ city +"\")")
			.appendQueryParameter("format", "xml")
			.appendQueryParameter("env", "store://datatables.org/alltableswithkeys")
		.build();
		// @formatter:on
		return u.toString();
	}

}
