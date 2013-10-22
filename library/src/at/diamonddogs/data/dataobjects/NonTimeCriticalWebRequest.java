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
import android.os.Handler;
import at.diamonddogs.service.net.HttpServiceAssister;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * Convenience class that can be used to run represent non time critical
 * {@link WebRequest}s. All non time critical {@link WebRequest}s are executed
 * asynchronously
 */
public class NonTimeCriticalWebRequest extends WebRequest implements NonTimeCriticalTask {

	private PRIORITY priority;
	private ServiceProcessor<?> serviceProcessor;
	private Handler.Callback callback;

	/**
	 * Construct a non time critical {@link WebRequest}. IMPORTANT: make sure
	 * not to provide a local {@link Context} like {@link Activity},
	 * {@link Service}, etc in any of the parameters provided to this
	 * constructor. Doing so will result in memory leaks!
	 * 
	 * @param priority
	 *            the priority of the request
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should handle the request
	 * @param callback
	 *            a {@link Handler.Callback}
	 */
	public NonTimeCriticalWebRequest(PRIORITY priority, ServiceProcessor<?> serviceProcessor, Handler.Callback callback) {
		super();
		this.timeCritical = false;
		this.priority = priority;
		this.serviceProcessor = serviceProcessor;
		this.callback = callback;
	}

	/**
	 * IMPORTANT: make sure
	 * not to provide a local {@link Context} like {@link Activity},
	 * {@link Service}, etc in any of the parameters provided to this
	 * constructor. Doing so will result in memory leaks!
	 * 
	 * @param serviceProcessor
	 *            the {@link ServiceProcessor} that should handle the request
	 * @param callback
	 *            a {@link Handler.Callback}
	 */
	public NonTimeCriticalWebRequest(ServiceProcessor<?> serviceProcessor, Handler.Callback callback) {
		super();
		this.timeCritical = false;
		this.priority = PRIORITY.NORMAL;
		this.serviceProcessor = serviceProcessor;
		this.callback = callback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PRIORITY getPriority() {
		return priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPriority(PRIORITY priority) {
		this.priority = priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(Context context, HttpServiceAssister assister) {
		assister.runWebRequest(callback, this, serviceProcessor);
	}
}
