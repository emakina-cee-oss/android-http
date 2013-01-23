/*
 * Copyright (C) 2012 the diamond:dogs|group
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
package at.diamonddogs.service.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.exception.ProcessorExeception;
import at.diamonddogs.util.CacheManager;
import at.diamonddogs.util.CacheManager.CachedObject;
import at.diamonddogs.util.Utils;

/**
 * This processor should be used if the data returned by the {@link WebRequest}
 * contains an image. This {@link ServiceProcessor} handles caching and handles
 * image creation.
 */
public class ImageProcessor extends DataProcessor<Bitmap, Bitmap> {

	/**
	 * The processor id
	 */
	public static final int ID = 13452;

	/**
	 * {@link Bundle} key that is used to store the image
	 */
	public static final String BUNDLE_EXTRA_BITMAP = "BUNDLE_EXTRA_BITMAP";

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessor.class);

	/**
	 * Determines if the request should be stored in memory, might cause OOM
	 * errors if the image is very large
	 */
	protected boolean useMemCache = true;

	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		LOGGER.info("processing imagerequest from: " + r.getRequest().getUrl());
		if (r.getStatus() == Status.OK) {
			WebReply reply = (WebReply) r.getReply();
			WebRequest request = (WebRequest) r.getRequest();
			ProcessingData<Bitmap> processingData = processData(reply.getData());
			Bitmap b = processingData.output;
			saveBitmapToFile(c, request, handler, b);
		} else if (r.getStatus() == Status.FAILED) {
			handler.sendMessage(createErrorMessage(ID, r.getThrowable(), (WebRequest) r.getRequest()));
		}
	}

	private void saveBitmapToFile(Context c, WebRequest request, Handler handler, Bitmap b) {
		try {
			String filename = Utils.getMD5Hash(request.getUrl().toString());
			if (filename != null && b != null) {
				if (request.getCacheTime() != CacheInformation.CACHE_NO) {
					File path = c.getExternalCacheDir();
					FileOutputStream fos = new FileOutputStream(new File(path, filename));
					b.compress(CompressFormat.PNG, 0, fos);

					CacheInformation ci = createImage(request, path.toString(), filename);

					CacheManager cm = CacheManager.getInstance();
					cm.addToCache(c, ci);
					if (useMemCache) {
						cm.addToMemoryCache(request.getUrl().toString(), ID, b);
					}
				}
				handler.sendMessage(createReturnMessage(b));

			} else {
				handler.sendMessage(createReturnMessage(b));
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
			handler.sendMessage(createErrorMessage(ID, e, request));
		}
	}

	@Override
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request) {
		WebRequest wr = (WebRequest) request;
		switch (cachedObject.getFrom()) {
		case MEMORY:
			LOGGER.info("processing image from memory cache");
			processMemCache(cachedObject.getCachedObject(), handler, wr);
			break;
		case FILE:
			LOGGER.info("processing image from file cache");
			processFileCache((byte[]) cachedObject.getCachedObject(), handler, wr);
			break;
		}
	}

	private void processFileCache(byte[] data, Handler handler, WebRequest webRequest) throws IllegalArgumentException {
		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (b == null) {
			handler.sendMessage(createErrorMessage(ID, new IllegalArgumentException("Couldn't decode Bitmap from data"), webRequest));
			return;
		}
		CacheManager.getInstance().addToMemoryCache(webRequest.getUrl().toString(), webRequest.getUrl().toString(), b);
		handler.sendMessage(createReturnMessage(b));
	}

	private void processMemCache(Object data, Handler handler, WebRequest webRequest) throws IllegalArgumentException {
		if (data instanceof Bitmap) {
			Bitmap b = (Bitmap) data;
			handler.sendMessage(createReturnMessage(b));
		} else {
			handler.sendMessage(createErrorMessage(ID, new IllegalArgumentException("Data must be a Bitmap object"), webRequest));
		}
	}

	private CacheInformation createImage(WebRequest request, String filePath, String fileName) {
		CacheInformation c = new CacheInformation();
		c.setCacheTime(request.getCacheTime());
		c.setCreationTimeStamp(System.currentTimeMillis());
		c.setFileName(fileName);
		c.setFilePath(filePath);
		return c;
	}

	/**
	 * Returns the absolute path of an image file as stored on the SDCard
	 * 
	 * @param url
	 *            the url of the image file (web url)
	 * @param context
	 *            a {@link Context}
	 * @return the absolute path of the image file on the file system or null if
	 *         it does not exist
	 */
	public static String getImageFileUrl(String url, Context context) {
		String filename = Utils.getMD5Hash(url);
		File dir = context.getExternalCacheDir();
		File file = new File(dir, filename);
		if (file.exists()) {
			return file.getAbsolutePath();
		}
		return null;
	}

	@Override
	public int getProcessorID() {
		return ID;
	}

	@Override
	protected Bitmap createParsedObjectFromByteArray(byte[] data) {
		if (data == null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(data, 0, data.length);

	}

	@Override
	protected Bitmap parse(Bitmap inputObject) {
		return inputObject;
	}

	@Override
	protected Message createReturnMessage(Bitmap data) {
		Message m = new Message();
		m.what = ID;
		m.arg1 = ServiceProcessor.RETURN_MESSAGE_OK;
		Bundle b = new Bundle();
		b.putParcelable(BUNDLE_EXTRA_BITMAP, data);
		m.setData(b);
		return m;
	}

	/**
	 * Constructs a default image {@link WebRequest}
	 * 
	 * @param url
	 * @return
	 */
	public static WebRequest getDefaultImageRequest(String url) {
		WebRequest wr = new WebRequest();
		wr.setUrl(url);
		wr.setProcessorId(ID);
		wr.setCacheTime(CacheInformation.CACHE_1MO);
		return wr;
	}

	/**
	 * A default {@link Handler} for image {@link WebRequest} will take care of
	 * displaying the image on an {@link ImageView}
	 */
	public static class ImageProcessHandler extends Handler {
		protected ImageView imageView;
		private String url;
		private Animation fadeInAnimation;
		private boolean useDrawingCache = false;

		/**
		 * Constructor
		 * 
		 * @param imageView
		 *            the {@link ImageView} the image should be displayed on
		 * @param url
		 *            the of the image, will be set as {@link ImageView} tag in
		 *            order to identify the correct {@link ImageView}
		 * @param fadeInAnimation
		 *            an optional {@link Animation}
		 */
		public ImageProcessHandler(ImageView imageView, String url, Animation fadeInAnimation) {
			if (imageView == null) {
				throw new IllegalArgumentException("ImageView must not be null");
			}
			this.imageView = imageView;
			this.imageView.setTag(url);
			this.fadeInAnimation = fadeInAnimation;
			this.url = url;
		}

		/**
		 * Convenience constructor for {@link URL} url types
		 * 
		 * @param imageView
		 *            the {@link ImageView} the image should be displayed on
		 * @param url
		 *            the of the image, will be set as {@link ImageView} tag in
		 *            order to identify the correct {@link ImageView}
		 * @param fadeInAnimation
		 *            an optional {@link Animation}
		 */
		public ImageProcessHandler(ImageView imageView, URL url, Animation fadeInAnimation) {
			this(imageView, url.toString(), fadeInAnimation);
		}

		/**
		 * Convenience constructor that causes {@link ImageProcessHandler} to
		 * use a default animation and {@link URL} type
		 * 
		 * @param imageView
		 *            the {@link ImageView} the image should be displayed on
		 * @param url
		 *            the of the image, will be set as {@link ImageView} tag in
		 *            order to identify the correct {@link ImageView}
		 */
		public ImageProcessHandler(ImageView imageView, URL url) {
			this(imageView, url.toString(), AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in));
		}

		/**
		 * Convenience constructor that causes {@link ImageProcessHandler} to
		 * use a default animation
		 * 
		 * @param imageView
		 *            the {@link ImageView} the image should be displayed on
		 * @param url
		 *            the of the image, will be set as {@link ImageView} tag in
		 *            order to identify the correct {@link ImageView}
		 */
		public ImageProcessHandler(ImageView imageView, String url) {
			this(imageView, url, AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in));
		}

		/**
		 * Enables the drawing cache of the {@link ImageView}
		 */
		public void enableDrawingCache() {
			useDrawingCache = true;
		}

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == ID) {
				if (msg.arg1 == ServiceProcessor.RETURN_MESSAGE_OK) {

					if (imageView == null || imageView.getTag() == null) {
						throw new ProcessorExeception("Tag for ImageView has not been set or ImageView was null");
					}
					LOGGER.info("adding image from url:" + url + " for tag: " + imageView.getTag());
					if (imageView.getTag().equals(url)) {
						Bundle b = msg.getData();
						Bitmap bitmap = b.getParcelable(BUNDLE_EXTRA_BITMAP);

						imageView.setImageBitmap(bitmap);
						if (useDrawingCache) {
							imageView.setDrawingCacheEnabled(true);
							imageView.buildDrawingCache(true);
						}
						if (fadeInAnimation != null) {
							imageView.startAnimation(fadeInAnimation);
						}
						imageView.invalidate();
					}
				}
			} else {
				ProcessorExeception e = new ProcessorExeception("Image request failed", (Throwable) msg.getData().getParcelable(
						ServiceProcessor.BUNDLE_EXTRA_MESSAGE_THROWABLE));
				LOGGER.error("Image request failed", e);
			}
		}
	}
}
