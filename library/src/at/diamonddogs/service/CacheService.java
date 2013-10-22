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
package at.diamonddogs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import at.diamonddogs.util.Utils;

/**
 * Kills the cache cleaning process if the process is not visible to the user
 */
public class CacheService extends Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class.getSimpleName());

	/**
	 * Argument key
	 */
	public static final String INTENT_EXTRA_START_ARGUMENT = "INTENT_EXTRA_START_ARGUMENT";

	/**
	 * Argument value that kills the {@link CacheService} process
	 */
	public static final int INTENT_EXTRA_KILL_PROCESS = 0;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LOGGER.info("recived start command");
		if (intent != null) {
			handleIntent(intent);
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void handleIntent(Intent intent) {
		int extra = intent.getIntExtra(INTENT_EXTRA_START_ARGUMENT, -1);
		LOGGER.info("handling intent for extra: " + extra);
		switch (extra) {
		case INTENT_EXTRA_KILL_PROCESS:
			LOGGER.debug("trying to kill process");
			killProcess();
			LOGGER.debug("killing process unsuccessfull");
			break;
		default:
			break;
		}
	}

	private void killProcess() {
		Utils.commitCarefulSuicideThreaded(this);
	}

}
