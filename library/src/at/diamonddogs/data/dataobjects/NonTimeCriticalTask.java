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
package at.diamonddogs.data.dataobjects;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import at.diamonddogs.service.net.HttpServiceAssister;

/**
 * Use this interface to implement non time critical tasks. Make sure not to
 * hold instances to local {@link Context}s like {@link Activity} or
 * {@link Service}, as all tasks will be stored globally, even if the
 * {@link Context} in question is outdated!
 */
public interface NonTimeCriticalTask {

	/**
	 * Different priorities
	 */
	// @formatter:off
	@SuppressWarnings("javadoc")
	public enum PRIORITY {
		HIGHEST,
		HIGHER,
		HIGH,
		NORMAL,
		LOWER,
		LOWEST,
	}
	// @formatter:on

	/**
	 * Gets the {@link PRIORITY} of the current task
	 * 
	 * @return the {@link PRIORITY}
	 */
	public PRIORITY getPriority();

	/**
	 * Sets the {@link PRIORITY} of the current task
	 * 
	 * @param priority
	 *            the {@link PRIORITY} to set
	 * @return
	 */
	public void setPriority(PRIORITY priority);

	/**
	 * Gets called when the current {@link NonTimeCriticalTask} should be
	 * processed
	 * 
	 * @param context
	 *            the {@link Context} obtained by calling
	 *            {@link Context#getApplicationContext()}
	 * @param assister
	 *            an {@link HttpServiceAssister} to be used to execute
	 *            {@link WebRequest}s resulting from this task.
	 */
	public void process(Context context, HttpServiceAssister assister);

}
