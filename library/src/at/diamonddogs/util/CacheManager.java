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

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import at.diamonddogs.android.support.v4.util.LruCache;
import at.diamonddogs.data.adapter.database.DataBaseAdapterCacheInformation;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.exception.CacheManagerException;
import at.diamonddogs.service.CacheService;

public class CacheManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

	private static CacheManager INSTANCE;

	private static final int CACHE_SIZE_MAX = 20;

	private LruCache<String, CacheItem> cache;

	public static final String ACTION_INTENT_SCHEDULE_CACHE = "at.diamonddogs.action.schedule.cache";

	private CacheManager() {
		// cache = new ConcurrentLRUCache<String,
		// CacheManager.CacheItem>(CACHE_SIZE_MAX, CACHE_SIZE_INITIAL);
		cache = new LruCache<String, CacheManager.CacheItem>(CACHE_SIZE_MAX);
	}

	public static synchronized CacheManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CacheManager();
		}
		return INSTANCE;
	}

	public void addToCache(Context context, CacheInformation cacheInformation) {
		DataBaseAdapterCacheInformation dbaci = new DataBaseAdapterCacheInformation(cacheInformation);
		dbaci.insert(context);
	}

	public CachedObject getFromCache(Context c, Request request) {
		try {
			return getFromMemoryCache(c, request);
		} catch (Throwable tr) {
			throw new CacheManagerException(tr);
		}
	}

	private CachedObject getFromMemoryCache(Context c, Request request) {
		CacheItem i = cache.get(request.getUrl().toString());
		if (i == null) {
			return getFromFileCache(c, request);
		} else {
			return new CachedObject(i.data, CachedObject.From.MEMORY);
		}
	}

	public void addToMemoryCache(String fileUrl, Object tag, Object data) {
		cache.put(fileUrl, new CacheItem(tag, data));
		LOGGER.debug("new cache size: " + cache.size());
	}

	public void clearMemoryCache() {
		// cache.clear();
		cache.evictAll();
		LOGGER.debug("Manually cleaned complete cache.");
	}

	public void clearMemoryCache(Object tag) {
		Iterator<Entry<String, CacheItem>> i = cache.getMap().entrySet().iterator();
		int count = 0;
		while (i.hasNext()) {
			Entry<String, CacheItem> item = i.next();
			CacheItem cacheItem = item.getValue();
			String key = item.getKey();
			if (cacheItem.tag.equals(tag)) {
				cache.remove(key);
				count++;
			}
		}
		LOGGER.debug("Manually cleaned cache for tag '" + tag + "' total of " + count + " items removed.");
	}

	private CachedObject getFromFileCache(Context c, Request request) {
		String fileName = Utils.getMD5Hash(request.getUrl().toString());
		DataBaseAdapterCacheInformation daci = new DataBaseAdapterCacheInformation();
		CacheInformation ci;

		try {
			CacheInformation[] cacheInformation = daci.query(c, fileName);
			if (cacheInformation.length == 0) {
				return null;
			}
			ci = cacheInformation[0];
		} catch (Throwable tr) {
			throw new CacheManagerException(tr);
		}

		long creationTimeStamp = ci.getCreationTimeStamp();
		long cacheTime = ci.getCacheTime();
		String filePath = ci.getFilePath();

		File f = new File(filePath, fileName);

		if (fileExpired(creationTimeStamp, cacheTime) || !f.exists()) {
			daci.setDataObject(ci);
			daci.delete(c);
			f.delete();
			return null;
		} else {
			try {
				byte[] buffer = new byte[(int) f.length()];
				FileInputStream fis;
				fis = new FileInputStream(f);
				fis.read(buffer);
				fis.close();
				return new CachedObject(buffer, CachedObject.From.FILE);
			} catch (Throwable e) {
				throw new CacheManagerException(e);
			}
		}
	}

	public boolean cleanExpired(Context c) {
		DataBaseAdapterCacheInformation dbaci = new DataBaseAdapterCacheInformation();
		CacheInformation[] cacheInformation = dbaci.query(c, null);
		if (cacheInformation != null) {
			for (CacheInformation ci : cacheInformation) {
				long creationTimeStamp = ci.getCreationTimeStamp();
				long cacheTime = ci.getCacheTime();
				if (fileExpired(creationTimeStamp, cacheTime) && (cacheTime != CacheInformation.CACHE_FOREVER)) {
					String fileName = ci.getFileName();
					LOGGER.info("File " + fileName + " expired");
					File f = new File(ci.getFilePath(), fileName);
					f.delete();
					dbaci.setDataObject(ci);
					dbaci.delete(c);
				}
			}
			return true;
		}
		return false;
	}

	private boolean fileExpired(long creationTime, long cacheTime) {
		if (cacheTime == CacheInformation.CACHE_NO) {
			return true;
		}
		if (cacheTime == CacheInformation.CACHE_FOREVER) {
			return false;
		}
		LOGGER.debug((System.currentTimeMillis() - creationTime) + " >= " + cacheTime);
		return (System.currentTimeMillis() - creationTime) >= cacheTime;
	}

	public void enableScheduledCacheCleaner(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// @formatter:off
		am.setInexactRepeating(
				AlarmManager.RTC,
				Utils.getScheduledDate(Calendar.SUNDAY, 3, 0, 0).getTimeInMillis(),
				7 * AlarmManager.INTERVAL_DAY,
				getAlarmIntent(context)
		);
		// @formatter:on
		LOGGER.info("Cache cleaning alarm has been set.");
	}

	public void disableScheduledCacheCleaner(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(getAlarmIntent(context));
		LOGGER.info("Cache cleaning alarm has been disabled.");
	}

	private PendingIntent getAlarmIntent(Context context) {
		Intent intent = new Intent(context.getApplicationContext(), CacheAlarmReceiver.class);
		intent.setAction(ACTION_INTENT_SCHEDULE_CACHE);
		PendingIntent pi = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}

	public static final class CachedObject {
		public enum From {
			MEMORY, FILE
		}

		private Object cachedObject;
		private From from;

		public CachedObject(Object cachedObject, From from) {
			this.cachedObject = cachedObject;
			this.from = from;
		}

		public Object getCachedObject() {
			return cachedObject;
		}

		public From getFrom() {
			return from;
		}
	}

	private static final class CacheItem {
		public Object tag;
		public Object data;

		public CacheItem(Object tag, Object data) {
			this.tag = tag;
			this.data = data;
		}
	}

	@TargetApi(14)
	public void registerComponentCallback(Context c) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			c.registerComponentCallbacks(new ComponentCallbackListener());
		}
	}

	private final class ComponentCallbackListener implements ComponentCallbacks2 {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onLowMemory() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTrimMemory(int level) {
			if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
				// cache.clear();
				cache.evictAll();
			}
			if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
				// trim cache to reasonable size here
			}
		}

	}

	/**
	 * Handles cache cleaning scheduling
	 * 
	 */
	public static final class CacheAlarmHook extends BroadcastReceiver {
		private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.CacheAlarmHook.class);

		@Override
		public void onReceive(final Context context, Intent intent) {
			LOGGER.info("CacheAlarmHook: Scheduling Cache Clean");
			CacheManager.getInstance().enableScheduledCacheCleaner(context);
			Intent serviceIntent = new Intent(context, CacheService.class);
			serviceIntent.putExtra(CacheService.INTENT_EXTRA_START_ARGUMENT, CacheService.INTENT_EXTRA_KILL_PROCESS);
			context.startService(serviceIntent);
		}
	}

	/**
	 * Hook for cleaning cache
	 */
	public static final class CacheAlarmReceiver extends BroadcastReceiver {
		private static final Logger LOGGER = LoggerFactory.getLogger(CacheAlarmReceiver.class);

		@Override
		public void onReceive(final Context context, Intent intent) {
			LOGGER.info("CacheAlarmReceiver: Alarm Received");
			if (intent.getAction().equals(CacheManager.ACTION_INTENT_SCHEDULE_CACHE)) {
				CacheManager.getInstance().cleanExpired(context);
				Intent serviceIntent = new Intent(context, CacheService.class);
				serviceIntent.putExtra(CacheService.INTENT_EXTRA_START_ARGUMENT, CacheService.INTENT_EXTRA_KILL_PROCESS);
				context.startService(serviceIntent);
			}
		}
	}

}
