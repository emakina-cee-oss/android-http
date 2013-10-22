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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import at.diamonddogs.data.dataobjects.NonTimeCriticalTask;
import at.diamonddogs.data.dataobjects.NonTimeCriticalTask.PRIORITY;

/**
 * Manages a global {@link PriorityBlockingQueue} containing all tasks that were
 * deemed non time critical.
 */
public class NonTimeCriticalTaskQueue {
	private static NonTimeCriticalTaskQueue INSTANCE;

	/**
	 * This {@link PriorityBlockingQueue} contains all non time critical tasks
	 */
	private PriorityBlockingQueue<NonTimeCriticalTask> tasks;

	/**
	 * The {@link NonTimeCriticalTaskQueueConfiguration} {@link Object} used to
	 * configure the queuing behaviour
	 */
	private NonTimeCriticalTaskQueueConfiguration configuration;

	/**
	 * A list of listeners that will receive callbacks when a
	 * {@link NonTimeCriticalTask} has been started
	 */
	private List<NonTimeCriticalTaskProcessingListener> listeners = Collections
			.synchronizedList(new ArrayList<NonTimeCriticalTaskProcessingListener>());

	private NonTimeCriticalTaskQueue() {
	}

	protected static NonTimeCriticalTaskQueue getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NonTimeCriticalTaskQueue();
		}
		return INSTANCE;
	}

	/**
	 * Pushes a {@link NonTimeCriticalTask} to the {@link PriorityBlockingQueue}
	 * used to store tasks.
	 * 
	 * @param task
	 *            the task to be put into the queue
	 */
	protected void putNonTimeCriticalTask(NonTimeCriticalTask task) {
		synchronized (tasks) {
			tasks.put(task);
		}
	}

	/**
	 * Polls the next {@link NonTimeCriticalTask} from
	 * {@link PriorityBlockingQueue} the queue
	 * 
	 * @return the next {@link NonTimeCriticalTask}
	 */
	protected NonTimeCriticalTask pollNonTimeCriticalTask() {
		synchronized (tasks) {
			return tasks.poll();
		}
	}

	/**
	 * Removes a {@link NonTimeCriticalTask} from the
	 * {@link PriorityBlockingQueue}
	 * 
	 * @param task
	 *            the task to be removed
	 */
	protected void removeNonTimeCriticalTask(NonTimeCriticalTask task) {
		synchronized (tasks) {
			tasks.remove(task);
		}
	}

	/**
	 * Removes all {@link NonTimeCriticalTask}s with a certain priority from the
	 * {@link PriorityBlockingQueue}
	 * 
	 * @param priority
	 *            the priority of the tasks to be removed
	 */
	protected void removeNonTimeCriticalTasksByPriority(PRIORITY priority) {
		synchronized (tasks) {
			Iterator<NonTimeCriticalTask> taskIterator = tasks.iterator();
			while (taskIterator.hasNext()) {
				NonTimeCriticalTask task = taskIterator.next();
				if (task.getPriority().equals(priority)) {
					taskIterator.remove();
				}
			}
		}
	}

	/**
	 * Removes all {@link NonTimeCriticalTask}s from the
	 * {@link PriorityBlockingQueue}
	 */
	protected void removeAllNonTimeCriticalTasks() {
		synchronized (tasks) {
			tasks.clear();
		}
	}

	/**
	 * Initializes the {@link NonTimeCriticalTaskQueue#tasks} if it has not been
	 * initialized yet.
	 * 
	 * @throws IllegalStateException
	 *             if no configuration has been provided yet
	 */
	private void initializePriorityQueueIfRequired() {
		if (tasks == null) {
			if (configuration == null) {
				throw new IllegalStateException(
						"NonTimeCriticalTaskQueue has not been configured yet! Configuration must happen before calling any other method. Call setConfiguration(...) to do so!");
			}
			tasks = new PriorityBlockingQueue<NonTimeCriticalTask>(configuration.getInitialQueueSize(), new NonTimeCriticalTaskComperator());
		}
	}

	/**
	 * Creates and returns a {@link List} of all processable tasks, even if
	 * {@link NonTimeCriticalTaskQueueConfiguration#getProcessAtSize()} has not
	 * been reached yet. Clears the {@link PriorityBlockingQueue} used to store
	 * the tasks!
	 * 
	 * @return a {@link List} of {@link NonTimeCriticalTask}s
	 */
	protected List<NonTimeCriticalTask> createProcessableTaskList() {
		synchronized (tasks) {

			ArrayList<NonTimeCriticalTask> processableTasks = new ArrayList<NonTimeCriticalTask>(tasks.size());
			NonTimeCriticalTask task;
			while ((task = tasks.poll()) != null) {
				processableTasks.add(task);
			}
			return processableTasks;
		}
	}

	/**
	 * Checks if {@link NonTimeCriticalTaskQueue#tasks} is ready for processing
	 * according to the provided {@link NonTimeCriticalTaskQueueConfiguration}
	 * 
	 * @return <code>true</code> if the queue can be processed now,
	 *         <code>false</code> otherwise
	 */
	protected boolean shouldQueueBeProcessed() {
		synchronized (tasks) {
			return tasks.size() >= configuration.getProcessAtSize();
		}
	}

	/**
	 * Sets the global configuration for {@link NonTimeCriticalTaskQueue}. Use
	 * {@link NonTimeCriticalTaskQueue#requiresConfiguration()} method to check
	 * if {@link NonTimeCriticalTaskQueue} has already been configured.
	 * 
	 * @throws IllegalStateException
	 *             if the configuration has already been provided
	 */
	protected void setConfiguration(NonTimeCriticalTaskQueueConfiguration configuration) {
		if (this.configuration != null) {
			throw new IllegalStateException("NonTimeCriticalTaskQueue may only be configured once");
		}
		this.configuration = configuration;
		initializePriorityQueueIfRequired();
	}

	/**
	 * Checks if {@link NonTimeCriticalTaskQueue} has been configured.
	 * 
	 * @return <code>true</code> if it has, <code>false</code> if it hasn't.
	 */
	protected boolean requiresConfiguration() {
		return this.configuration == null;
	}

	/**
	 * {@link NonTimeCriticalTaskQueueConfiguration} provides an interface to
	 * the configuration of {@link NonTimeCriticalTaskQueue}
	 */
	public interface NonTimeCriticalTaskQueueConfiguration {
		/**
		 * Returns the initial size of the queue
		 * 
		 * @return the initial size of the queue
		 */
		public int getInitialQueueSize();

		/**
		 * Returns the minimum size of the queue that triggers processing
		 * 
		 * @return min. processing size
		 */
		public int getProcessAtSize();

	}

	/**
	 * Method to inform listeners of started tasks
	 * 
	 * @param task
	 *            the {@link NonTimeCriticalTask} that has been started
	 */
	protected void informListeners(NonTimeCriticalTask task) {
		for (NonTimeCriticalTaskProcessingListener listener : listeners) {
			listener.onProcessingStarted(task);
		}
	}

	/**
	 * Adds a listener that will be notified once a task is being processed
	 * 
	 * @param nonTimeCriticalTaskProcessingListener
	 *            the listener
	 */
	protected void addNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener nonTimeCriticalTaskProcessingListener) {
		listeners.add(nonTimeCriticalTaskProcessingListener);
	}

	/**
	 * Removes a listener
	 * 
	 * @param nonTimeCriticalTaskProcessingListener
	 *            the listener
	 */
	protected void removeNonTimeCriticalTaskProcessingListener(NonTimeCriticalTaskProcessingListener nonTimeCriticalTaskProcessingListener) {
		listeners.remove(nonTimeCriticalTaskProcessingListener);
	}

	/**
	 * An interface describing a listener for {@link NonTimeCriticalTask}
	 * related events
	 */
	public interface NonTimeCriticalTaskProcessingListener {
		/**
		 * Gets called whenever the given {@link NonTimeCriticalTask} has been
		 * started
		 * 
		 * @param task
		 *            the task that was started
		 */
		public void onProcessingStarted(NonTimeCriticalTask task);
	}

	/**
	 * An abstract factory interface that should be used to create factories
	 * constructing {@link NonTimeCriticalTaskQueueConfiguration}
	 */
	public interface NonTimeCriticalTaskQueueConfigurationFactory {
		/**
		 * Creates a new instance of
		 * {@link NonTimeCriticalTaskQueueConfiguration}
		 * 
		 * @return the configuration
		 */
		public NonTimeCriticalTaskQueueConfiguration newInstance();
	}

	/**
	 * This {@link Comparator} takes care of sorting {@link NonTimeCriticalTask}
	 * s according to their {@link PRIORITY}
	 */
	private static final class NonTimeCriticalTaskComperator implements Comparator<NonTimeCriticalTask> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(NonTimeCriticalTask lhs, NonTimeCriticalTask rhs) {
			return lhs.getPriority().compareTo(rhs.getPriority());
		}

	}
}
