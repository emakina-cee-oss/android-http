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

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerQueue {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkerQueue.class.getSimpleName());
	private LinkedBlockingQueue<Runnable> outstandingRequests;

	private ThreadPoolExecutor threadPoolExecuter;

	public WorkerQueue(int corePoolSize, int maxPoolSize, long keepAliveTimeMs) {
		outstandingRequests = new LinkedBlockingQueue<Runnable>();
		threadPoolExecuter = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeMs, TimeUnit.MILLISECONDS, outstandingRequests);
	}

	public void runTask(Runnable task) {
		if (!threadPoolExecuter.isShutdown()) {
			threadPoolExecuter.execute(task);
		}
	}

	public void runTasks(Runnable[] tasks) {
		if (!threadPoolExecuter.isShutdown()) {
			for (Runnable r : tasks) {
				threadPoolExecuter.execute(r);

			}
		}
	}

	public Future<?> runCancelableTask(Runnable task) {
		if (!threadPoolExecuter.isShutdown()) {
			return threadPoolExecuter.submit(task);
		}
		return null;
	}

	public void runTasks(List<Runnable> tasks) {
		if (!threadPoolExecuter.isShutdown()) {
			for (Runnable r : tasks) {
				threadPoolExecuter.execute(r);
			}
		}
	}

	public boolean isShutDown() {
		return threadPoolExecuter.isShutdown();
	}

	public void shutDown() {
		LOGGER.debug("shuting down NOW");
		threadPoolExecuter.shutdownNow();
	}
}
