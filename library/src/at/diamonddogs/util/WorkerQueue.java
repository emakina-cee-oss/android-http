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

/**
 * A simple worker queue
 */
public class WorkerQueue {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkerQueue.class.getSimpleName());
	private LinkedBlockingQueue<Runnable> outstandingRequests;

	private ThreadPoolExecutor threadPoolExecuter;

	/**
	 * Creates a {@link WorkerQueue}
	 * 
	 * @param corePoolSize
	 *            the core pool size
	 * @param maxPoolSize
	 *            the max pool size
	 * @param keepAliveTimeMs
	 *            the keep alive time in ms
	 */
	public WorkerQueue(int corePoolSize, int maxPoolSize, long keepAliveTimeMs) {
		outstandingRequests = new LinkedBlockingQueue<Runnable>();
		threadPoolExecuter = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeMs, TimeUnit.MILLISECONDS, outstandingRequests);
	}

	/**
	 * Run a task in the context of this {@link WorkerQueue}
	 * 
	 * @param task
	 */
	public void runTask(Runnable task) {
		if (!threadPoolExecuter.isShutdown()) {
			threadPoolExecuter.execute(task);
		}
	}

	/**
	 * Run mutiple tasks in the context of the {@link WorkerQueue}
	 * 
	 * @param tasks
	 */
	public void runTasks(Runnable[] tasks) {
		if (!threadPoolExecuter.isShutdown()) {
			for (Runnable r : tasks) {
				threadPoolExecuter.execute(r);

			}
		}
	}

	/**
	 * Cancel a running task
	 * 
	 * @param task
	 *            the task to cancel
	 * @return returns the {@link Future} of the task that has been canceled or
	 *         <code>null</code> if the executer was shutdown
	 */
	public Future<?> runCancelableTask(Runnable task) {
		if (!threadPoolExecuter.isShutdown()) {
			return threadPoolExecuter.submit(task);
		}
		return null;
	}

	/**
	 * Cancel multiple running tasks
	 * 
	 * @param tasks
	 *            the tasks to cancel
	 */
	public void runTasks(List<Runnable> tasks) {
		if (!threadPoolExecuter.isShutdown()) {
			for (Runnable r : tasks) {
				threadPoolExecuter.execute(r);
			}
		}
	}

	/**
	 * Checks if the executer was shut down
	 * 
	 * @return <code>true</code> if it was, <code>false</code> otherwise
	 */
	public boolean isShutDown() {
		return threadPoolExecuter.isShutdown();
	}

	/**
	 * Shuts down the executor
	 */
	public void shutDown() {
		LOGGER.debug("shuting down NOW");
		threadPoolExecuter.shutdownNow();
	}
}
