/**
 * Copyright 2013, the diamond:dogs|group
 */
package at.diamonddogs.example.http.processor;

import org.w3c.dom.Document;

import android.content.Context;
import android.os.Handler;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.example.http.dataobject.RssItem;
import at.diamonddogs.service.processor.XMLProcessor;
import at.diamonddogs.util.CacheManager.CachedObject;

/**
 * 
 */
public class RssProcessor extends XMLProcessor<RssItem> {
	public static final int ID = 93025;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RssItem parse(Document inputObject) {
		inputObject.getElementsByTagName("title");
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProcessorID() {
		return ID;
	}
}
