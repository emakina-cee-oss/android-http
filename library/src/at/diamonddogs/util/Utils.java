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
package at.diamonddogs.util;

import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
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

public class Utils {

	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getSimpleName());

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

	public static String getMD5Hash(String url) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			return new String(Hex.encodeHex(md.digest(url.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public static Integer[] getDigitArray(int to) {
		to++;
		Integer[] ret = new Integer[to];
		for (int i = 0; i < to; i++) {
			ret[i] = i;
		}
		return ret;
	}

	public static String getMD5Hash(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return new String(Hex.encodeHex(md.digest(data)));
		} catch (Throwable tr) {
			LOGGER.warn("Could not md5 data");
			return null;
		}
	}

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

	public static String encrypt(String inMsg) {
		return encrypt(inMsg.getBytes());
	}

	public static String encrypt(byte[] inMsg) {
		return Base64.encodeToString(inMsg, Base64.DEFAULT);
	}

	public static String decrypt(String inMsg) {
		return decrypt(inMsg.getBytes());
	}

	public static String decrypt(byte[] encMsg) {
		return new String(Base64.decode(encMsg, Base64.DEFAULT));
	}

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

	public static final void commitSuicide() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static final Bitmap getBitmapFromUri(Context c, Uri uri, int inSampleSize) throws FileNotFoundException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, options);
	}

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
