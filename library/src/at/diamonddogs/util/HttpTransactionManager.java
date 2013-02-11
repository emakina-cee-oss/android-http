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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.util.Pair;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.HttpTransaction;
import at.diamonddogs.data.dataobjects.HttpTransactionCached;
import at.diamonddogs.data.dataobjects.HttpTransactionConnected;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.processor.ServiceProcessor;
import at.diamonddogs.util.CacheManager.CachedObject;

/**
 * A utility class for writing HTTP transaction logs
 */
public class HttpTransactionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTransactionManager.class.getSimpleName());

	private ConnectivityHelper connectivityHelper;
	private Map<String, Pair<Date, ServiceProcessor<?>>> trackingMap = Collections
			.synchronizedMap(new HashMap<String, Pair<Date, ServiceProcessor<?>>>());

	public HttpTransactionManager(Context c) {
		this.connectivityHelper = new ConnectivityHelper(c);
	}

	public void begin(String webRequestId, ServiceProcessor<?> processor) {
		if (trackingMap.containsKey(webRequestId)) {
			throw new RuntimeException("Already tracking request with id " + webRequestId);
		}
		trackingMap.put(webRequestId, new Pair<Date, ServiceProcessor<?>>(new Date(), processor));
	}

	public void commit(ReplyAdapter replyAdapter) {
		WebRequest wr = (WebRequest) replyAdapter.getRequest();
		if (!trackingMap.containsKey(wr.getId())) {
			throw new RuntimeException("Commiting a WebRequest whose id is not in the trackingMap " + wr.getId());
		}
		HttpTransactionConnected httpTransaction = new HttpTransactionConnected();
		setCommonAttributes(httpTransaction, wr);
		httpTransaction.setReplyAdapter(replyAdapter);
		LOGGER.info("Transaction commited: " + httpTransaction);
	}

	public void commit(CachedObject cachedObject, WebRequest webRequest) {
		if (!trackingMap.containsKey(webRequest.getId())) {
			throw new RuntimeException("Commiting a WebRequest whose id is not in the trackingMap " + webRequest.getId());
		}
		HttpTransactionCached httpTransaction = new HttpTransactionCached();
		setCommonAttributes(httpTransaction, webRequest);
		httpTransaction.setCachedObject(cachedObject);
		httpTransaction.setWebRequest(webRequest);
		LOGGER.info("Transaction commited: " + httpTransaction);
	}

	private void setCommonAttributes(HttpTransaction httpTransaction, WebRequest wr) {
		Pair<Date, ServiceProcessor<?>> p = trackingMap.get(wr.getId());
		httpTransaction.setStartTime(p.first);
		httpTransaction.setFinishTime(new Date());
		httpTransaction.setProcessorClass((Class<ServiceProcessor<?>>) p.second.getClass());
	}
}
