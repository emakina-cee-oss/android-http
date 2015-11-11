/*
 * Copyright (C) 2013 the diamond:dogs|group
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

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * A collection of Android specific utils
 */
public class AndroidUtils {
	private static AndroidUtils INSTANCE;

	private AndroidUtils() {
	}

	/**
	 * Gets the singleton instance of {@link AndroidUtils}
	 * 
	 * @return the singleton instance of {@link AndroidUtils}
	 */
	public static AndroidUtils getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AndroidUtils();
		}
		return INSTANCE;
	}

	/**
	 * Taken and adapted from: http://stackoverflow.com/a/6529160/775241
	 * Checks if a {@link Service} is available.
	 * 
	 * @param context
	 *            a {@link Context} object
	 * @param cls
	 *            the class of the {@link Service}
	 * @return <code>true</code> if the {@link Service} is available,
	 *         <code>false</code> otherwise
	 */
	public boolean isServiceAvailable(Context context, Class<?> cls) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(context, cls);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return resolveInfo.size() > 0;
	}

}
