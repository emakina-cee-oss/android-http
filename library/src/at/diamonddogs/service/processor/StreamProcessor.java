package at.diamonddogs.service.processor;

import java.io.InputStream;

import android.content.Context;
import android.os.Handler;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.util.CacheManager.CachedObject;

public class StreamProcessor extends ServiceProcessor<InputStream> implements SynchronousProcessor<InputStream> {

	public static final int ID = 723565;

	@Override
	public InputStream obtainDataObjectFromWebReply(Context c, ReplyAdapter reply) {
		return ((WebReply) reply.getReply()).getInputStream();
	}

	@Override
	public InputStream obtainDataObjectFromCachedObject(Context c, WebRequest webRequest, CachedObject object) {
		throw new UnsupportedOperationException("chaching not supported");
	}

	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		InputStream inputStream = ((WebReply) r.getReply()).getInputStream();
		handler.sendMessage(createReturnMessage(r, inputStream));
	}

	@Override
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request) {
		throw new UnsupportedOperationException("chaching not supported");
	}

	@Override
	public int getProcessorID() {
		return ID;
	}

}
