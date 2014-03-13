package at.diamonddogs.example.http.activity;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;
import at.diamonddogs.example.http.R;
import at.diamonddogs.example.http.dataobject.Weather;
import at.diamonddogs.example.http.processor.WeatherProcessor;
import at.diamonddogs.service.net.HttpService.WebRequestReturnContainer;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.HeadRequestProcessor;
import at.diamonddogs.service.processor.ImageProcessor;
import at.diamonddogs.service.processor.ImageProcessor.ImageProcessHandler;
import at.diamonddogs.service.processor.ServiceProcessorMessageUtil;

/**
 * {@link HttpServiceAssisterExampleActivity} illustrates the use of the
 * {@link HttpServiceAssister}
 */
public class HttpServiceAssisterExampleActivity extends Activity {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceAssisterExampleActivity.class.getSimpleName());

	private HttpServiceAssister assister;

	/**
	 * Text view to display a weather string
	 */
	private TextView text;

	/**
	 * Text view to display the temperature
	 */
	private TextView temperature;

	/**
	 * The weather image
	 */
	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httpserviceassisterexampleactivity);
		assister = new HttpServiceAssister(this);
		text = (TextView) findViewById(R.id.httpserviceassisterexampleactivity_text);
		temperature = (TextView) findViewById(R.id.httpserviceassisterexampleactivity_temperature);
		image = (ImageView) findViewById(R.id.httpserviceassisterexampleactivity_image);
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

		// we can start WebRequests without having to wait for service
		// binding.
		// HttpServiceAssister will queue asynchronous WebRequests and
		// execute
		// them once a connection has been established. The ServiceProcessor
		// will automatically be registered as well.

		// --- SYNC WEB REQUEST

		// Synchronous WebRequest cannot be executed on the Main (UI) thread
		// using HttpServiceAssister (technical limitation). The actual request
		// will still block the current thread though!
		new ExampleAsyncTask().execute(weatherUrl);

		// RESETTING THE ASSISTER FOR DEMONSTRATION PURPOSES! REBINDING THE
		// SERVICE
		assister.unbindService();
		assister = new HttpServiceAssister(this);
		assister.bindService();

		// --- ASYNC WEB REQUEST

		WebRequest asyncRequest = new WebRequest();
		// takes a String or URL object!
		asyncRequest.setUrl(weatherUrl);

		// The processorid tells HttpService what to do once a web reply has
		// been received.
		// You MUST set a processor id and the processor needs to be registered
		// with HttpService.
		// You may use DummyProcessor.ID to circumvent processor implementation
		asyncRequest.setProcessorId(WeatherProcessor.ID);

		// run the web request, WeatherHandler will receive a callback once the
		// web request has been finished. This call is alway possible, if the
		// HttpService is not bound yet, the WebRequest will be appended to a
		// queue and processed when HttpService becomes available
		assister.runWebRequest(new WeatherHandler(), asyncRequest, new WeatherProcessor());
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
	 * Runs an Image {@link WebRequest} and displays the image on a given
	 * {@link ImageView}
	 * 
	 * @param w
	 *            a {@link Weather} object
	 */
	private void runImageRequest(Weather w) {
		String imageUrl = "http://openweathermap.org/img/w/" + w.getIcon() + ".png";

		// use this helper method to obtain a valid image WebRequest
		WebRequest imageRequest = ImageProcessor.getDefaultImageRequest(imageUrl);

		// ImageProcessHanlder will take care of displaying the image, no
		// additional work required!
		assister.runWebRequest(new ImageProcessHandler(image, imageUrl), imageRequest, new ImageProcessor());
	}

	/**
	 * This implementation of {@link AsyncTask} is used to execute a synchronous
	 * HEAD {@link WebRequest}. As in {@link HttpExampleActivity}, the headers
	 * will be logged and not displayed on the UI.
	 */
	private final class ExampleAsyncTask extends AsyncTask<String, Integer, Object> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object doInBackground(String... params) {
			WebRequest syncWebRequest = new WebRequest();
			syncWebRequest.setUrl(params[0]);
			syncWebRequest.setRequestType(Type.HEAD);

			// default header request processor
			syncWebRequest.setProcessorId(HeadRequestProcessor.ID);

			return assister.runSynchronousWebRequest(syncWebRequest, new HeadRequestProcessor());
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			Map<String, List<String>> headers = (Map<String, List<String>>) ((WebRequestReturnContainer) result).getPayload();
			if (headers != null) {
				for (String key : headers.keySet()) {
					LOGGER.error("KEY -> " + key);
					for (String value : headers.get(key)) {
						LOGGER.error("    VALUE -> " + value);
					}
				}
			} else {
				Toast.makeText(HttpServiceAssisterExampleActivity.this, "Error while optaining headers", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This handler receives a callback once the web request has been processed.
	 */
	private class WeatherHandler extends Handler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (ServiceProcessorMessageUtil.isFromProcessor(msg, WeatherProcessor.ID)) {
				if (ServiceProcessorMessageUtil.isSuccessful(msg)) {
					Weather w = (Weather) msg.obj;
					text.setText(w.getText());
					temperature.setText(String.valueOf(w.getTemperature()));

					runImageRequest(w);
				} else {
					Toast.makeText(HttpServiceAssisterExampleActivity.this, "Error fetching weather", Toast.LENGTH_LONG).show();
				}
			}

		}
	}

}
