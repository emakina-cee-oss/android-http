/**
 * Copyright 2013, the diamond:dogs|group
 */
package at.diamonddogs.example.http.activity;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;
import at.diamonddogs.example.http.R;
import at.diamonddogs.example.http.dataobject.Weather;
import at.diamonddogs.example.http.processor.WeatherProcessor;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister.HttpOrderedAsyncHandler2;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister.HttpOrderedAsyncRequest;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister.NextWebRequestDelegate;
import at.diamonddogs.service.net.HttpOrderedAsyncAssister.NoNextWebRequestDelegate;
import at.diamonddogs.service.processor.HeadRequestProcessor;
import at.diamonddogs.service.processor.ServiceProcessorMessageUtil;

/**
 * A simple example that illustrates how asynchronous {@link WebRequest}s can be
 * executed in order, according to certain conditions.
 */
public class HttpOrderedAsyncAssisiterExampleActivity extends Activity {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceAssisterExampleActivity.class.getSimpleName());

	private HttpOrderedAsyncAssister assister;

	/**
	 * Text view to display a weather string
	 */
	private TextView text;

	/**
	 * Text view to display the temperature
	 */
	private TextView temperature;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httporderedasyncassisterexampleactivity);
		assister = new HttpOrderedAsyncAssister(this);
		text = (TextView) findViewById(R.id.httporderedasyncassisiterexampleactivity_text);
		temperature = (TextView) findViewById(R.id.httporderedasyncassisiterexampleactivity_temperature);
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected void onResume() {
		super.onResume();
		assister.bindService();
		String weatherUrl = getWeatherUrl("Austria", "Vienna");
		LOGGER.info("URL: " + weatherUrl);
		WebRequest headRequest = getHeadRequest();
		// @formatter:off
		HttpOrderedAsyncRequest initialRequest = new HttpOrderedAsyncRequest(
				headRequest,
				new WeatherHandler(assister),
				new NextWebRequestDelegate() {
					@Override
					public HttpOrderedAsyncRequest getNextWebRequest(Message message) {
						Map<String, List<String>> header = (Map<String, List<String>>) message.obj;
						if (header != null) {
							if (header.containsKey("Content-Type") && header.get("Content-Type").get(0).equals("application/json; charset=utf-8")) {
								LOGGER.error("Content-Type is application/json; charset=utf-8, will run actual WebRequest now!");
								return new HttpOrderedAsyncRequest(
										getWeatherRequest(),
										new WeatherHandler(assister),
										new NoNextWebRequestDelegate(),
										new WeatherProcessor()
								);
							} else {
							LOGGER.error("Content-Type is notapplication/json; charset=utf-8 but " + header.get("Content-Type") + " not running any successive WebRequests!");
								return null;
							}
						} else {
							LOGGER.error("Headers are null, not sending WebRequest");
							return null;
						}
					}
				},
				new HeadRequestProcessor()
		);
		// @formatter:on
		assister.runRequests(initialRequest);
	}

	private WebRequest getHeadRequest() {
		WebRequest webRequest = new WebRequest();
		webRequest.setUrl(getWeatherUrl("Austria", "Vienna"));
		webRequest.setRequestType(Type.HEAD);

		// default header request processor
		webRequest.setProcessorId(HeadRequestProcessor.ID);

		// required for HEAD request
		webRequest.addHeaderField("Accept-Encoding", "gzip, deflate");
		return webRequest;
	}

	private WebRequest getWeatherRequest() {
		WebRequest webRequest = new WebRequest();
		webRequest.setUrl(getWeatherUrl("Austria", "Vienna"));
		webRequest.setRequestType(Type.GET);

		// default header request processor
		webRequest.setProcessorId(WeatherProcessor.ID);

		return webRequest;
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
	 * Formats the openweathermap.org weather URL
	 * 
	 * @param country
	 *            the country
	 * @param city
	 *            the city
	 * @return the weather url for country & city
	 */
	private String getWeatherUrl(String country, String city) {
		Uri u = Uri.parse("http://api.openweathermap.org/data/2.5/weather/");
		// @formatter:off
		u = u.buildUpon()
			.appendQueryParameter("q", city + "," + country)
			.appendQueryParameter("units", "metric")
		.build();
		// @formatter:on
		return u.toString();
	}

	/**
	 * This handler receives a callback once the web request has been processed.
	 */
	private class WeatherHandler extends HttpOrderedAsyncHandler2 {
		/**
		 * @param arg0
		 */
		public WeatherHandler(HttpOrderedAsyncAssister arg0) {
			super(arg0);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean onNextWebRequestComplete(Message msg) {
			if (ServiceProcessorMessageUtil.isFromProcessor(msg, HeadRequestProcessor.ID)) {
				return ServiceProcessorMessageUtil.isSuccessful(msg);
			} else {
				return false;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onWebRequestChainCompleted(Message msg) {
			if (ServiceProcessorMessageUtil.isFromProcessor(msg, WeatherProcessor.ID)) {
				if (ServiceProcessorMessageUtil.isSuccessful(msg)) {
					Weather w = (Weather) msg.obj;
					text.setText(w.getText());
					temperature.setText(String.valueOf(w.getTemperature()));
				} else {
					Toast.makeText(HttpOrderedAsyncAssisiterExampleActivity.this, "Error fetching weather", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
}
