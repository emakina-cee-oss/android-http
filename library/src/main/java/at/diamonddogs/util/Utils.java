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
package at.diamonddogs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Base64;
import at.diamonddogs.data.adapter.database.DatabaseAdapter;

/**
 * Collection of general util methods
 */
public class Utils {

	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getSimpleName());

	/**
	 * Returns an array
	 * 
	 * @deprecated do not use this method, the implementation doesn't make a lot
	 *             of sense and furthermore, the whole method is somewhat
	 *             pointless.
	 * @param <T>
	 *            generic type of the items to place into an array
	 * @param clazz
	 *            the class of generic type <T>
	 * @param values
	 *            an arbitrary number of values
	 * @return an array containing all values passed to this method
	 */
	@Deprecated
	public static <T> T[] asArray(Class<T> clazz, T... values) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(clazz, values.length);
		for (int i = 0; i < values.length; i++) {
			array[i] = values[i];
		}
		return array;
	}

	/**
	 * Checks if a {@link Collection} is not <code>null</code> and not empty
	 * 
	 * @param collection
	 *            the collection to check
	 * @return <code>true</code> if the collection wasn't null and is not empty,
	 *         <code>false</code> otherwise
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return (collection == null || collection.size() == 0);
	}

	/**
	 * Checks if an is not <code>null</code> and not empty
	 * 
	 * @param <T>
	 *            generic type of the array
	 * @param array
	 *            the array to check
	 * @return <code>true</code> if the array wasn't null and is not empty,
	 *         <code>false</code> otherwise
	 */
	public static <T> boolean isEmptyArray(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Creates a {@link List} from a {@link Cursor}
	 * 
	 * @param <T>
	 *            the generic type of the {@link List}
	 * @param cursor
	 *            the {@link Cursor} to be converted to a {@link List}
	 * @param databaseAdapter
	 *            the {@link DatabaseAdapter} that will be used for conversion
	 * @return a {@link List} containing objects created from the input
	 *         {@link Cursor}
	 */
	public static <T> List<T> convertCursorToList(Cursor cursor, DatabaseAdapter<T> databaseAdapter) {
		List<T> list = new ArrayList<T>();
		if (!checkCursor(cursor)) {
			return null;
		}
		cursor.moveToFirst();
		do {
			list.add(databaseAdapter.deserialize(cursor));
		} while (cursor.moveToNext());
		return list;
	}

	/**
	 * Creates a {@link String} {@link List} from a {@link Cursor}
	 * 
	 * @param cursor
	 *            the input {@link Cursor}
	 * @param name
	 *            the name of the colum
	 * @return a {@link String} {@link List}
	 */
	public static List<String> convertColumnToList(Cursor cursor, String name) {
		List<String> list = new ArrayList<String>();
		if (!checkCursor(cursor)) {
			return null;
		}
		cursor.moveToFirst();
		do {
			list.add(cursor.getString(cursor.getColumnIndex(name)));
		} while (cursor.moveToNext());

		return list;
	}

	/**
	 * Get available cache directory
	 * 
	 * @param context
	 *            a {@link Context}
	 * @return a {@link File} pointing to a the external or internal cache
	 *         directory
	 */
	public static File getCacheDir(Context context) {
		File path = context.getExternalCacheDir();
		if (path == null) {
			path = context.getCacheDir();
		}
		return path;
	}

	/**
	 * Brings up the MAIN/LAUNCHER activity and clears the top
	 * 
	 * @param context
	 *            a {@link Context}
	 */
	public static void returnToHome(Context context) {
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setPackage(context.getPackageName());
		List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
		Intent homeIntent = new Intent("android.intent.action.MAIN");
		homeIntent.addCategory("android.intent.category.LAUNCHER");
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		homeIntent.setComponent(new ComponentName(context.getPackageName(), activities.get(0).activityInfo.name));
		context.startActivity(homeIntent);
	}

	/**
	 * Checks if a timestamp (ms) is in the current month
	 * 
	 * @param when
	 *            a ms timestamp
	 * @return <code>true</code> if it is <code>false</code> otherwise
	 */
	public static boolean isSameMonth(long when) {
		Calendar tmp = Calendar.getInstance();
		tmp.setTimeInMillis(when);

		// cal.get(FIELD) does not work :( fields are not recomputed
		int thenYear = tmp.get(Calendar.YEAR);
		int thenMonth = tmp.get(Calendar.MONTH);

		tmp.setTimeInMillis(System.currentTimeMillis());
		int nowYear = tmp.get(Calendar.YEAR);
		int nowMonth = tmp.get(Calendar.MONTH);

		LOGGER.debug("comparing: " + nowMonth + "." + nowYear + " / " + thenMonth + "." + thenYear);
		return (thenYear == nowYear) && (thenMonth == nowMonth);
	}

	/**
	 * Computes a MD5 hash from an input string
	 * 
	 * @param input
	 *            the input string
	 * @return the MD5 hash or <code>null</code> if an error occured
	 */
	public static String getMD5Hash(String input) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			return new String(Hex.encodeHex(md.digest(input.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Creates and fills an {@link Integer} array with integer values
	 * 
	 * @param to
	 *            size of array / max number to be added to array
	 * @return an {@link Integer} array
	 */
	public static Integer[] getDigitArray(int to) {
		to++;
		Integer[] ret = new Integer[to];
		for (int i = 0; i < to; i++) {
			ret[i] = i;
		}
		return ret;
	}

	/**
	 * Computes a MD5 hash from an byte array
	 * 
	 * @param data
	 *            the input data
	 * @return the MD5 hash or <code>null</code> if an error occured
	 */
	public static String getMD5Hash(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return new String(Hex.encodeHex(md.digest(data)));
		} catch (Throwable tr) {
			LOGGER.warn("Could not md5 data");
			return null;
		}
	}

	/**
	 * Checks a cursor for validity
	 * 
	 * @param c
	 *            the {@link Cursor} to check
	 * @return <code>true</code> if the cursor is not <code>null</code>, not
	 *         closed and not empty, <code>false</code> otherwise
	 */
	public static boolean checkCursor(Cursor c) {
		if (c == null || c.isClosed()) {
			return false;
		}

		if (c.getCount() <= 0) {
			c.close();
			return false;
		}
		return true;
	}

	/**
	 * String to base64
	 * 
	 * @param inMsg
	 *            the message to be converted to base64
	 * @return the base64 string
	 */
	public static String encrypt(String inMsg) {
		return encrypt(inMsg.getBytes());
	}

	/**
	 * Byte array to base64
	 * 
	 * @param inMsg
	 *            the message to be converted to base64
	 * @return the base64 string
	 */
	public static String encrypt(byte[] inMsg) {
		return Base64.encodeToString(inMsg, Base64.DEFAULT);
	}

	/**
	 * Base64 string to string
	 * 
	 * @param inMsg
	 *            the message to be converted from base64
	 * @return the string
	 */
	public static String decrypt(String inMsg) {
		return decrypt(inMsg.getBytes());
	}

	/**
	 * Base64 byte array to string
	 * 
	 * @param encMsg
	 *            the message to be converted from base64
	 * @return the string
	 */
	public static String decrypt(byte[] encMsg) {
		return new String(Base64.decode(encMsg, Base64.DEFAULT));
	}

	/**
	 * Checks if the current process is a foreground process (visible by the
	 * user)
	 * 
	 * @param context
	 *            a {@link Context}
	 * @return <code>true</code> if the process is visible, <code>false</code>
	 *         otherwise
	 */
	public static boolean isInForground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE && appProcess.pid == Process.myPid()) {
				LOGGER.info("visible");
				return true;
			}
		}
		LOGGER.info("Running in background");
		return false;
	}

	/**
	 * Converts input values to a {@link Calendar}
	 * 
	 * @param dayOfWeek
	 * @param hourOfDay
	 * @param minute
	 * @param second
	 * @return a {@link Calendar}r with the provided date
	 */
	public static final Calendar getScheduledDate(int dayOfWeek, int hourOfDay, int minute, int second) {
		Calendar c = Calendar.getInstance();
		int weekDay = c.get(Calendar.DAY_OF_WEEK);
		int days = dayOfWeek - weekDay;
		if (days < 0) {
			days += 7;
		}
		c.add(Calendar.DAY_OF_YEAR, days);
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		return c;
	}

	/**
	 * Checks if the current process is a foreground process and kills it if it
	 * is not
	 * 
	 * @param c
	 *            a {@link Context}
	 */
	public static final void commitCarefulSuicide(Context c) {
		try {
			if (!new ForegroundCheckTask().execute(c).get()) {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Same as {@link Utils#commitCarefulSuicide(Context)} but threaded (non
	 * blocking)
	 * 
	 * @param context
	 */
	public static final void commitCarefulSuicideThreaded(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
				if (appProcesses == null) {
					return;
				}

				LOGGER.debug("app counter: " + appProcesses.size());
				final String packageName = context.getPackageName();
				for (RunningAppProcessInfo appProcess : appProcesses) {
					LOGGER.debug("process: " + appProcess.processName);
					if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
						LOGGER.debug("isINF: " + true);
						return;
					}
				}
				LOGGER.debug("isINF: " + false);
				commitSuicide();
			}
		}).start();

	}

	/**
	 * Kills the process without asking questions
	 */
	public static final void commitSuicide() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * Creates a bitmap from an input uri
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param uri
	 *            an image {@link Uri}
	 * @param inSampleSize
	 *            the sample size to be used when creating the bitmap
	 * @return a {@link Bitmap}
	 * @throws FileNotFoundException
	 *             if the image file could not be found
	 */
	public static final Bitmap getBitmapFromUri(Context c, Uri uri, int inSampleSize) throws FileNotFoundException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, options);
	}

	/**
	 * Creates a bitmap from an input uri
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param uri
	 *            an image {@link Uri}
	 * @return a {@link Bitmap}
	 * @throws FileNotFoundException
	 *             if the image file could not be found
	 */
	public static final Bitmap getBitmapFromUri(Context c, String uri) throws FileNotFoundException {
		return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(Uri.parse(uri)));
	}

	private static class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

		private static final Logger LOGGER = LoggerFactory.getLogger(Utils.ForegroundCheckTask.class.getSimpleName());

		@Override
		protected Boolean doInBackground(Context... params) {
			final Context context = params[0].getApplicationContext();
			boolean isInForeground = isAppOnForeground(context);
			LOGGER.debug("isINF: " + isInForeground);
			return isInForeground;
		}

		private boolean isAppOnForeground(Context context) {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
			if (appProcesses == null) {
				return false;
			}

			LOGGER.error("app counter: " + appProcesses.size());
			final String packageName = context.getPackageName();
			for (RunningAppProcessInfo appProcess : appProcesses) {
				LOGGER.debug("process: " + appProcess.processName);
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
					return true;
				}
			}
			return false;
		}
	}
}
