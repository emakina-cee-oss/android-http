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
package at.diamonddogs.nontimecritical;

import android.content.Context;
import at.diamonddogs.data.dataobjects.NonTimeCriticalTask;
import at.diamonddogs.data.dataobjects.NonTimeCriticalTask.PRIORITY;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskQueue.NonTimeCriticalTaskProcessingListener;
import at.diamonddogs.nontimecritical.NonTimeCriticalTaskQueue.NonTimeCriticalTaskQueueConfiguration;
import at.diamonddogs.service.net.HttpServiceAssister;

/**
 * {@link NonTimeCriticalTaskManager} is used to manage
 * {@link NonTimeCriticalTask}s
 */
public class NonTimeCriticalTaskManager {
	/**
	 * The global queue that stores all {@link NonTimeCriticalTask}s
	 */
	private NonTimeCriticalTaskQueue queue = NonTimeCriticalTaskQueue.getInstance();

	/**
	 * The instance of {@link HttpServiceAssister} that was used to construct
	 * this manager
	 */
	private final HttpServiceAssister assister;

	/**
	 * Default constructor
	 * 
	 * @param queueConfiguration
	 *            the queue configuration
	 * @param assister
	 *            the assister that will be used to process requests
	 */
	public NonTimeCriticalTaskManager(NonTimeCriticalTaskQueueConfiguration queueConfiguration, HttpServiceAssister assister) {
		if (queue.requiresConfiguration()) {
			queue.setConfiguration(queueConfiguration);
		}
		this.assister = assister;
	}

	/**
	 * Adds a new {@link NonTimeCriticalTask} to the queue
	 * 
	 * @param task
	 *            the task to be added
	 */
	public void put(NonTimeCriticalTask task) {
		queue.putNonTimeCriticalTask(task);
	}

	/**
	 * Removes an existing {@link NonTimeCriticalTask} from the queue
	 * 
	 * @param task
	 *            the task to be removed
	 */
	public void remove(NonTimeCriticalTask task) {
		queue.removeNonTimeCriticalTask(task);
	}

	/**
	 * Removes all tasks of the provided {@link PRIORITY}
	 * 
	 * @param p
	 *            the {@link PRIORITY}
	 */
	public void removeByPriority(PRIORITY p) {
		queue.removeNonTimeCriticalTasksByPriority(p);
	}

	/**
	 * Adds a listener that will be notified once a task is being processed
	 * 
	 * @param nonTimeCriticalTaskProcessingListener
	 *            the listener
	 */
	public void addNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener nonTimeCriticalTaskProcessingListener) {
		queue.addNonTimeCriticalTaskProcessingListener(nonTimeCriticalTaskProcessingListener);
	}

	/**
	 * Removes a listener
	 * 
	 * @param nonTimeCriticalTaskProcessingListener
	 *            the listener
	 */
	public void removeNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener nonTimeCriticalTaskProcessingListener) {
		queue.removeNonTimeCriticalTaskProcessingListener(nonTimeCriticalTaskProcessingListener);
	}

	/**
	 * Runs all pending {@link NonTimeCriticalTask}s.
	 * 
	 * @param context
	 *            a {@link Context} {@link Object}
	 */
	public void runTasksIfNecessary(Context context) {
		if (queue.shouldQueueBeProcessed()) {
			runTasks(context);
		}
	}

	/**
	 * Run all tasks in the queue, even if the configuration threshold has not
	 * been hit
	 * 
	 * @param context
	 *            a {@link Context} {@link Object}
	 */
	public void runTasks(Context context) {
		for (NonTimeCriticalTask task : queue.createProcessableTaskList()) {
			queue.informListeners(task);
			task.process(context.getApplicationContext(), assister);
		}
	}

}
