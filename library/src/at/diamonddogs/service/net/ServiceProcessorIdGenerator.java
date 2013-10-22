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
package at.diamonddogs.service.net;

import java.util.concurrent.atomic.AtomicInteger;

import android.util.SparseArray;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * 
 */
public class ServiceProcessorIdGenerator {

	private static ServiceProcessorIdGenerator INSTANCE;

	private AtomicInteger processorId = new AtomicInteger(0);

	private SparseArray<Class<ServiceProcessor<?>>> generatedIds = new SparseArray<Class<ServiceProcessor<?>>>();

	private ServiceProcessorIdGenerator() {
	}

	protected static ServiceProcessorIdGenerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServiceProcessorIdGenerator();
		}
		return INSTANCE;
	}

	protected int getIdForProcessor(Class<ServiceProcessor<?>> cls) {
		int idx = generatedIds.indexOfValue(cls);
		int ret;
		if (idx < 0) {
			ret = processorId.incrementAndGet();
			generatedIds.put(ret, cls);
		} else {
			ret = generatedIds.keyAt(idx);
		}
		return ret;
	}
}
