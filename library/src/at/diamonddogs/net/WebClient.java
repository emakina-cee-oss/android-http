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
package at.diamonddogs.net;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.TempFile;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.http.BuildConfig;
import at.diamonddogs.util.Utils;

/**
 * An abstract {@link WebClient} to be used when implementing new
 * {@link WebClient} flavours
 */
public abstract class WebClient implements Callable<ReplyAdapter> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebClient.class);

	/**
	 * The read buffer size
	 */
	private static final int READ_BUFFER_SIZE = 4096;

	/**
	 * The {@link WebRequest} executed by this {@link WebClient}
	 */
	protected WebRequest webRequest = null;

	/**
	 * The listener that will be informed once a {@link WebReply} has been
	 * received
	 */
	protected WebClientReplyListener webClientReplyListener;

	/**
	 * A {@link DownloadProgressListener} that will receive progress updated
	 */
	private DownloadProgressListener downloadProgressListener;

	/**
	 * Allow protocol redirects (http -> https / http -> https / etc)
	 */
	protected boolean followProtocolRedirect;

	protected abstract void buildHeader();

	/**
	 * Constructs a {@link WebClient}
	 * 
	 * @param context
	 *            a context
	 */
	public WebClient(Context context) {

		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			followProtocolRedirect = ai.metaData.getBoolean(context.getPackageName() + ".followProtocolRedirect");
		} catch (Throwable th) {
			followProtocolRedirect = false;
		}

	}

	protected WebReply handleResponseOk(InputStream i, int statusCode, Map<String, List<String>> replyHeader) throws IOException {
		WebReply reply = new WebReply();
		reply.setHttpStatusCode(statusCode);
		reply.setReplyHeader(replyHeader);
		if (webRequest.getTmpFile().first) {
			saveData(i);
			return reply;
		}
		return getData(i, reply);
	}

	/**
	 * Returns the {@link WebRequest} related to this {@link WebClient}
	 * 
	 * @return a {@link WebRequest}
	 */
	public WebRequest getWebRequest() {
		return webRequest;
	}

	private void saveData(InputStream i) throws IOException {
		if (i == null) {
			return;
		}
		TempFile tmp = webRequest.getTmpFile().second;
		FileOutputStream fos = null;
		try {

			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream dis = new DigestInputStream(i, md);

			File file = new File(tmp.getPath());
			LOGGER.debug(file.getAbsolutePath() + "can write: " + file.canWrite());
			if (file.exists() && !tmp.isAppend()) {
				file.delete();
			}
			byte buffer[] = new byte[READ_BUFFER_SIZE];
			fos = new FileOutputStream(file, tmp.isAppend());
			int bytesRead = 0;
			while ((bytesRead = dis.read(buffer)) != -1) {
				if (!webRequest.isCancelled()) {
					fos.write(buffer, 0, bytesRead);
					publishDownloadProgress(bytesRead);
				} else {
					LOGGER.info("Cancelled Download");
					break;
				}
			}
			fos.flush();
			fos.close();

			if (webRequest.isCancelled()) {
				LOGGER.info("delete file due to canclled download: " + file.getName());
				file.delete();
			} else {

				if (tmp.isUseChecksum()) {
					String md5 = new String(Hex.encodeHex(md.digest()));

					LOGGER.debug("md5 check, original: " + tmp.getChecksum() + " file: " + md5);

					if (!md5.equalsIgnoreCase(tmp.getChecksum())) {
						throw new IOException("Error while downloading File.\nOriginal Checksum: " + tmp.getChecksum() + "\nChecksum: "
								+ md5);
					}
				}
			}

		} catch (Exception e) {
			if (fos != null && tmp.isAppend()) {
				fos.flush();
				fos.close();
			}
			LOGGER.error("Failed download", e);
			// Please do not do that - that hides the original error!
			throw new IOException(e.getMessage());

			// Who ever changed this... new IOException(e) is API level 9!!! and
			// gives a nice NoSuchMethodException :)
			// throw new IOException(e);
		}
	}

	private WebReply getData(InputStream i, WebReply reply) throws IOException {
		if (i == null) {
			return reply;
		}

		if (webRequest.isGetStream()) {
			if (isGzipEncoded(reply)) {
				reply.setInputStream(new GZIPInputStream(i));
			} else {
				reply.setInputStream(i);
			}
			return reply;
		}

		byte buffer[] = new byte[READ_BUFFER_SIZE];

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		InputStream toRead;
		if (isGzipEncoded(reply)) {
			LOGGER.info("Reply is gzip encoded! " + reply);
			try {
				toRead = new GZIPInputStream(i);
			} catch (Throwable tr) {
				LOGGER.warn(
						"Problem with GZIP reply, using normal input stream! This issue can be caused by an empty body (i.e. HEAD request)",
						tr);
				toRead = i;
			}
		} else {
			toRead = i;
		}

		int bytesRead = 0;
		while ((bytesRead = toRead.read(buffer)) != -1) {
			if (!webRequest.isCancelled()) {
				baos.write(buffer, 0, bytesRead);
				publishDownloadProgress(bytesRead);
			} else {
				break;
			}
		}
		if (BuildConfig.DEBUG) {
			byte[] array = baos.toByteArray();
			LOGGER.error("md5: " + Utils.getMD5Hash(array) + " " + webRequest.getUrl());
		}
		reply.setData(baos.toByteArray());
		try {
			baos.close();
		} catch (Exception e) {
		}

		try {
			toRead.close();
		} catch (Exception e) {
		}
		return reply;
	}

	private boolean isGzipEncoded(WebReply reply) {
		if (!reply.getReplyHeader().containsKey("Content-Encoding")) {
			return false;
		}
		List<String> encodings = reply.getReplyHeader().get("Content-Encoding");
		for (String encoding : encodings) {
			LOGGER.debug("Encoding: " + encoding);
			if (encoding.contains("gzip")) {
				return true;
			}
		}
		return false;
	}

	protected void publishFileSize(long size) {
		if (downloadProgressListener != null) {
			downloadProgressListener.downloadSize(size);
		}
	}

	protected void publishDownloadProgress(long progress) {
		if (downloadProgressListener != null) {
			downloadProgressListener.downloadProgress(progress);
		}
	}

	protected WebReply handleResponseNotModified(int statusCode, Map<String, List<String>> replyHeader) {
		WebReply reply = new WebReply();
		reply.setHttpStatusCode(statusCode);
		reply.setReplyHeader(replyHeader);
		return reply;
	}

	protected WebReply handleResponseNotOk(InputStream i, int statusCode, Map<String, List<String>> replyHeader) {
		WebReply reply = new WebReply();
		reply.setHttpStatusCode(statusCode);

		if (i == null) {
			reply.setData(null);
		} else {
			try {
				getData(i, reply);
			} catch (Exception e) {
				reply.setData(null);
			}

		}

		reply.setReplyHeader(replyHeader);
		return reply;
	}

	protected ReplyAdapter createListenerReply(WebRequest request, WebReply reply, Throwable throwable, Status replyStatus) {
		ReplyAdapter listenerReply = new ReplyAdapter();
		listenerReply.setRequest(request);
		listenerReply.setReply(reply);
		listenerReply.setThrowable(throwable);
		listenerReply.setStatus(replyStatus);
		return listenerReply;
	}

	@SuppressWarnings("javadoc")
	public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
		this.downloadProgressListener = downloadProgressListener;
	}

	@SuppressWarnings("javadoc")
	public void setListener(WebClientReplyListener listener) {
		this.webClientReplyListener = listener;
	}

	@SuppressWarnings("javadoc")
	public void setWebRequest(WebRequest webRequest) {
		this.webRequest = webRequest;
	}

	/**
	 * Interface that needs to be implemented by every class that wished to
	 * receive {@link WebReply} notifications
	 */
	public interface WebClientReplyListener {
		/**
		 * Called by {@link WebClient} once a {@link WebReply} has been received
		 * 
		 * @param webClient
		 *            the client that called
		 *            {@link WebClientReplyListener#onWebReply(WebClient, ReplyAdapter)}
		 * @param reply
		 *            the {@link ReplyAdapter} created by the {@link WebClient}
		 */
		public void onWebReply(WebClient webClient, ReplyAdapter reply);
	}

	/**
	 * Interface that needs to be implemented by every class that wishes to
	 * receive download progress updates
	 */
	public interface DownloadProgressListener {
		/**
		 * Informs listeners about the content length (if available)
		 * 
		 * @param size
		 *            the size in byte
		 */
		public void downloadSize(long size);

		/**
		 * Informs listeners about the bytes read
		 * 
		 * @param progress
		 *            bytes read
		 */
		public void downloadProgress(long progress);
	}

	/**
	 * Copy of HttpURLConnection, needed since we are working with multiple http
	 * implementations and we want to have a common class with constants.
	 */
	public static final class HTTPStatus {
		@SuppressWarnings("javadoc")
		public static final int HTTP_OK = 200;
		@SuppressWarnings("javadoc")
		public static final int HTTP_CREATED = 201;
		@SuppressWarnings("javadoc")
		public static final int HTTP_ACCEPTED = 202;
		@SuppressWarnings("javadoc")
		public static final int HTTP_NOT_AUTHORITATIVE = 203;
		@SuppressWarnings("javadoc")
		public static final int HTTP_NO_CONTENT = 204;
		@SuppressWarnings("javadoc")
		public static final int HTTP_RESET = 205;
		@SuppressWarnings("javadoc")
		public static final int HTTP_PARTIAL = 206;
		@SuppressWarnings("javadoc")
		public static final int HTTP_MULT_CHOICE = 300;
		@SuppressWarnings("javadoc")
		public static final int HTTP_MOVED_PERM = 301;
		@SuppressWarnings("javadoc")
		public static final int HTTP_MOVED_TEMP = 302;
		@SuppressWarnings("javadoc")
		public static final int HTTP_SEE_OTHER = 303;
		@SuppressWarnings("javadoc")
		public static final int HTTP_NOT_MODIFIED = 304;
		@SuppressWarnings("javadoc")
		public static final int HTTP_USE_PROXY = 305;
		@SuppressWarnings("javadoc")
		public static final int HTTP_BAD_REQUEST = 400;
		@SuppressWarnings("javadoc")
		public static final int HTTP_UNAUTHORIZED = 401;
		@SuppressWarnings("javadoc")
		public static final int HTTP_PAYMENT_REQUIRED = 402;
		@SuppressWarnings("javadoc")
		public static final int HTTP_FORBIDDEN = 403;
		@SuppressWarnings("javadoc")
		public static final int HTTP_NOT_FOUND = 404;
		@SuppressWarnings("javadoc")
		public static final int HTTP_BAD_METHOD = 405;
		@SuppressWarnings("javadoc")
		public static final int HTTP_NOT_ACCEPTABLE = 406;
		@SuppressWarnings("javadoc")
		public static final int HTTP_PROXY_AUTH = 407;
		@SuppressWarnings("javadoc")
		public static final int HTTP_CLIENT_TIMEOUT = 408;
		@SuppressWarnings("javadoc")
		public static final int HTTP_CONFLICT = 409;
		@SuppressWarnings("javadoc")
		public static final int HTTP_GONE = 410;
		@SuppressWarnings("javadoc")
		public static final int HTTP_LENGTH_REQUIRED = 411;
		@SuppressWarnings("javadoc")
		public static final int HTTP_PRECON_FAILED = 412;
		@SuppressWarnings("javadoc")
		public static final int HTTP_ENTITY_TOO_LARGE = 413;
		@SuppressWarnings("javadoc")
		public static final int HTTP_REQ_TOO_LONG = 414;
		@SuppressWarnings("javadoc")
		public static final int HTTP_UNSUPPORTED_TYPE = 415;
		@SuppressWarnings("javadoc")
		public static final int HTTP_INTERNAL_ERROR = 500;
		@SuppressWarnings("javadoc")
		public static final int HTTP_NOT_IMPLEMENTED = 501;
		@SuppressWarnings("javadoc")
		public static final int HTTP_BAD_GATEWAY = 502;
		@SuppressWarnings("javadoc")
		public static final int HTTP_UNAVAILABLE = 503;
		@SuppressWarnings("javadoc")
		public static final int HTTP_GATEWAY_TIMEOUT = 504;
		@SuppressWarnings("javadoc")
		public static final int HTTP_VERSION = 505;
	}
}
