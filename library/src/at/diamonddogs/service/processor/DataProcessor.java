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
package at.diamonddogs.service.processor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Message;
import at.diamonddogs.data.dataobjects.CacheInformation;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager;
import at.diamonddogs.util.Utils;

/**
 * 
 */
public abstract class DataProcessor<INPUT, OUTPUT> extends ServiceProcessor {

	/**
	 * Parses the content of data into an INPUT object
	 * 
	 * @param data
	 *            the raw data
	 * @return a parsed object
	 */
	protected abstract INPUT createParsedObjectFromByteArray(byte[] data);

	/**
	 * The actual parsing of the data
	 * 
	 * @param inputObject
	 *            the object returned by createParsedObjectFromByteArray
	 * @return return data that can be handed to createReturnMessage
	 */
	protected abstract OUTPUT parse(INPUT inputObject);

	/**
	 * Constructs a reply message that can be used to inform activities about
	 * processing
	 * 
	 * @param data
	 *            the resulting data that was created from INPUT
	 * 
	 * @return a message object
	 */
	protected abstract Message createReturnMessage(OUTPUT data);

	/**
	 * Handles processing using the provided callback methods of the respective
	 * child classes
	 * 
	 * @param data
	 *            the raw input data
	 * @return a processing object containing the data extracted from the raw
	 *         input byte[] data and a Message object. Message.what will be set
	 *         to the current processors id.
	 */
	protected ProcessingData<OUTPUT> processData(byte[] data) {
		INPUT input = createParsedObjectFromByteArray(data);
		OUTPUT output = parse(input);
		Message message = createReturnMessage(output);
		message.what = getProcessorID();
		return new ProcessingData<OUTPUT>(message, output);
	}

	protected void cacheObjectToFile(Context context, WebRequest request, byte[] data) {
		String filename = Utils.getMD5Hash(request.getUrl().toString());
		BufferedOutputStream bos = null;
		try {
			if (filename != null && data != null) {
				if (request.getCacheTime() != CacheInformation.CACHE_NO) {
					File path = context.getExternalCacheDir();
					FileOutputStream fos = new FileOutputStream(new File(path, filename));
					bos = new BufferedOutputStream(fos);
					bos.write(data);

					CacheInformation ci = createCachingInformation(request.getCacheTime(), path.toString(), filename);

					CacheManager cm = CacheManager.getInstance();
					cm.addToCache(context, ci);
				}
			}
		} catch (Throwable th) {
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					bos = null;
				}
			}
		}
	}

	private CacheInformation createCachingInformation(long chacheTime, String filePath, String fileName) {
		CacheInformation c = new CacheInformation();
		c.setCacheTime(chacheTime);
		c.setCreationTimeStamp(System.currentTimeMillis());
		c.setFileName(fileName);
		c.setFilePath(filePath);
		return c;
	}

	protected final static class ProcessingData<OUTPUT> {
		public Message returnMessage;
		public OUTPUT output;

		public ProcessingData(Message returnMessage, OUTPUT output) {
			this.returnMessage = returnMessage;
			this.output = output;
		}
	}

}
