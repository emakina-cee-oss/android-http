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

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * A processor that handles image post processing
 */
public class AdjustableImageProcessor extends ImageProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdjustableImageProcessor.class.getSimpleName());

	private int ID = -1;

	private Context context;

	private Bitmap.Config config;

	/**
	 * Default constructor
	 * 
	 * @param context
	 *            a {@link Context}
	 * @param config
	 *            the bitm
	 */
	public AdjustableImageProcessor(Context context, Bitmap.Config config) {
		useMemCache = false;
		this.context = context;
		this.config = config;
	}

	@Override
	protected Bitmap createParsedObjectFromByteArray(byte[] data) {
		int reqWidth = getDisplayMetrics().widthPixels;
		int reqHeight = getDisplayMetrics().heightPixels;

		return decodeSampledBitmapFromResource(data, reqWidth, reqHeight);
	}

	private DisplayMetrics getDisplayMetrics() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displaymetrics);
		return displaymetrics;
	}

	@Override
	public int getProcessorID() {
		if (ID == -1) {
			ID = new Random().nextInt();
		}
		return ID;
	}

	// thanks to android training lessons for the code
	// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	private Bitmap decodeSampledBitmapFromResource(byte[] data, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		try {
			if (options != null) {
				options.inPreferredConfig = config;
				options.inDither = true;
			}
			return BitmapFactory.decodeByteArray(data, 0, data.length, options);
		} catch (OutOfMemoryError e) {
			System.gc();
			LOGGER.error("OutOfMemoryError", e);
			return null;
		}

	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		LOGGER.error("calculated sample size: " + inSampleSize);

		return inSampleSize;
	}
}
