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
import android.database.Cursor;
import android.os.Build;
import at.diamonddogs.android.support.v4.util.LruCache;
import at.diamonddogs.contentprovider.CacheContentProvider;
import at.diamonddogs.data.adapter.database.DataBaseAdapterCacheInformation;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.exception.CacheManagerException;
import at.diamonddogs.service.CacheService;

/**
 * This class manages the file system and memory cache. Please use this class
 * instead of writing to the file system and database directly
 */
public class CacheManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

	/**
	 * Holds singleton instance
	 */
	private static CacheManager INSTANCE;

	/**
	 * Cache cleaning scheduling {@link Integer} action
	 */
	public static final String ACTION_INTENT_SCHEDULE_CACHE = "at.diamonddogs.action.schedule.cache";

	/**
	 * Max cache entries
	 */
	private static final int CACHE_SIZE_MAX_ENTRIES = 20;

	/**
	 * The {@link LruCache} that will be used as an in memory cache
	 */
	private LruCache<String, CacheItem> cache;

	private CacheManager() {
		cache = new LruCache<String, CacheManager.CacheItem>(CACHE_SIZE_MAX_ENTRIES);
	}

	/**
	 * Obtains the {@link CacheManager} singleton instance
	 * 
	 * @return an instance of {@link CacheManager}
	 */
	public static synchronized CacheManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CacheManager();
		}
		return INSTANCE;
	}

	/**
	 * Adds data to the cache
	 * 
	 * @param context
	 *            a {@link Context}
	 * @param cacheInformation
	 *            the information related to the object that gets cached
	 */
	public void addToCache(Context context, CacheInformation cacheInformation) {
		DataBaseAdapterCacheInformation dbaci = new DataBaseAdapterCacheInformation(cacheInformation);
		dbaci.insert(context);
	}

	/**
	 * Retrieves an item from the cache. Memory cache has precedence over file
	 * cache
	 * 
	 * @param c
	 *            a {@link Context}
	 * @param request
	 *            the request whose data is cached
	 * @return a cached item
	 */
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

	/**
	 * Adds an item to the memory cache.
	 * 
	 * @param fileUrl
	 *            the url of the file
	 * @param tag
	 *            a tag (used to group cached items)
	 * @param data
	 *            the data to be cached
	 */
	public void addToMemoryCache(String fileUrl, Object tag, Object data) {
		cache.put(fileUrl, new CacheItem(tag, data));
		LOGGER.debug("new cache size: " + cache.size());
	}

	/**
	 * Evicts all items from the memory cache
	 */
	public void clearMemoryCache() {
		cache.evictAll();
		LOGGER.debug("Manually cleaned complete cache.");
	}

	/**
	 * Evicts all items with a specific tag from the memory cache
	 * 
	 * @param tag
	 *            an arbitrary tag, see
	 *            {@link CacheManager#addToMemoryCache(String, Object, Object)}
	 */
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
		ConnectivityHelper connectivityHelper = new ConnectivityHelper(c);
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
			LOGGER.warn("Problem querying database", tr);
			return null;
		}

		long creationTimeStamp = ci.getCreationTimeStamp();
		long cacheTime = ci.getCacheTime();
		String filePath = ci.getFilePath();

		File f = new File(filePath, fileName);

		boolean connected = connectivityHelper.checkConnectivityWebRequest((WebRequest) request);
		// @formatter:off
		if (
				(fileExpired(creationTimeStamp, cacheTime) || !f.exists()) && 
				(!ci.isUseOfflineCache() || connected)
		) {
		// @formatter:on
			daci.setDataObject(ci);
			daci.delete(c);
			f.delete();
			return null;
		} else {
			LOGGER.info("Obtaining file from Cache. Expired: " + fileExpired(creationTimeStamp, cacheTime) + " File Exists: " + f.exists()
					+ " UseOfflineCache: " + ci.isUseOfflineCache() + " Connectivity: " + connected);
			try {
				byte[] buffer = new byte[(int) f.length()];
				FileInputStream fis;
				fis = new FileInputStream(f);
				fis.read(buffer);
				fis.close();
				return new CachedObject(buffer, CachedObject.From.FILE);
			} catch (Throwable e) {
				LOGGER.warn("Could not read cached file", e);
				return null;
			}
		}
	}

	/**
	 * Deleted expired files from the file cache
	 * 
	 * @param c
	 *            a {@link Context}
	 * @return <code>true</code> if the file cache was cleaned successfully,
	 *         <code>false</code> otherwise
	 */
	public void cleanExpired(Context c) {
		Cursor cursor = c.getContentResolver().query(CacheContentProvider.CONTENT_URI, null, null, null, null);

		if (!Utils.checkCursor(cursor)) {
			return;
		}
		cursor.moveToFirst();
		DataBaseAdapterCacheInformation dbaci = new DataBaseAdapterCacheInformation();
		do {
			dbaci.setDataObject(cursor);
			CacheInformation cacheInfo = dbaci.getDataObject();
			long creationTimeStamp = cacheInfo.getCreationTimeStamp();
			long cacheTime = cacheInfo.getCacheTime();

			if (fileExpired(creationTimeStamp, cacheTime) && (cacheTime != CacheInformation.CACHE_FOREVER)) {
				String fileName = cacheInfo.getFileName();
				File f = new File(cacheInfo.getFilePath(), fileName);
				f.delete();
				dbaci.delete(c);
			}
		} while (cursor.moveToNext());

		cursor.close();
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

	/**
	 * Turn on scheduled cache cleaning (cache will be cleaned even if app is
	 * not running)
	 * 
	 * @param context
	 *            a {@link Context}
	 */
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

	/**
	 * Turn off scheduled cache cleaning
	 * 
	 * @param context
	 *            a {@link Context}
	 */
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

	/**
	 * Representation of a cached object. Includes the actual object and
	 * metadata
	 */
	public static final class CachedObject {
		/**
		 * Cache source
		 */
		public enum From {
			/**
			 * {@link CachedObject} was obtained from memory
			 */
			MEMORY,
			/**
			 * {@link CachedObject} was obtained from the file system
			 */
			FILE
		}

		private Object cachedObject;
		private From from;

		/**
		 * Constructor
		 * 
		 * @param cachedObject
		 *            the actual object that was cached
		 * @param from
		 *            the source cache
		 */
		public CachedObject(Object cachedObject, From from) {
			this.cachedObject = cachedObject;
			this.from = from;
		}

		@SuppressWarnings("javadoc")
		public Object getCachedObject() {
			return cachedObject;
		}

		@SuppressWarnings("javadoc")
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

	/**
	 * Registers a component callback for cache cleaning on low memory
	 * 
	 * @param c
	 *            a {@link Context}
	 */
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
