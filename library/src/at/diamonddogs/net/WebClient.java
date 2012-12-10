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

public abstract class WebClient implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebClient.class);

	protected WebRequest webRequest = null;

	protected WebClientReplyListener webClientReplyListener;

	private DownloadProgressListener downloadProgressListener;

	private static final int READ_BUFFER_SIZE = 4096;

	protected abstract void buildHeader();

	protected boolean followProtocolRedirect;

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

	public WebRequest getWebRequest() {
		return webRequest;
	}

	private void saveData(InputStream i) throws IOException {
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
		byte buffer[] = new byte[READ_BUFFER_SIZE];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int bytesRead = 0;
		while ((bytesRead = i.read(buffer)) != -1) {
			if (!webRequest.isCancelled()) {
				baos.write(buffer, 0, bytesRead);
				publishDownloadProgress(bytesRead);
			} else {
				break;
			}
		}
		reply.setData(baos.toByteArray());

		return reply;
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

	public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
		this.downloadProgressListener = downloadProgressListener;
	}

	public void setListener(WebClientReplyListener listener) {
		this.webClientReplyListener = listener;
	}

	public void setWebRequest(WebRequest webRequest) {
		this.webRequest = webRequest;
	}

	public interface WebClientReplyListener {
		public void onWebReply(WebClient webClient, ReplyAdapter reply);
	}

	public interface DownloadProgressListener {
		public void downloadSize(long size);

		public void downloadProgress(long progress);
	}

	/**
	 * Copy of HttpURLConnection, needed since we are working with multiple http
	 * implementations and we want to have a common class with constants.
	 */
	public static final class HTTPStatus {
		public static final int HTTP_OK = 200;
		public static final int HTTP_CREATED = 201;
		public static final int HTTP_ACCEPTED = 202;
		public static final int HTTP_NOT_AUTHORITATIVE = 203;
		public static final int HTTP_NO_CONTENT = 204;
		public static final int HTTP_RESET = 205;
		public static final int HTTP_PARTIAL = 206;
		public static final int HTTP_MULT_CHOICE = 300;
		public static final int HTTP_MOVED_PERM = 301;
		public static final int HTTP_MOVED_TEMP = 302;
		public static final int HTTP_SEE_OTHER = 303;
		public static final int HTTP_NOT_MODIFIED = 304;
		public static final int HTTP_USE_PROXY = 305;
		public static final int HTTP_BAD_REQUEST = 400;
		public static final int HTTP_UNAUTHORIZED = 401;
		public static final int HTTP_PAYMENT_REQUIRED = 402;
		public static final int HTTP_FORBIDDEN = 403;
		public static final int HTTP_NOT_FOUND = 404;
		public static final int HTTP_BAD_METHOD = 405;
		public static final int HTTP_NOT_ACCEPTABLE = 406;
		public static final int HTTP_PROXY_AUTH = 407;
		public static final int HTTP_CLIENT_TIMEOUT = 408;
		public static final int HTTP_CONFLICT = 409;
		public static final int HTTP_GONE = 410;
		public static final int HTTP_LENGTH_REQUIRED = 411;
		public static final int HTTP_PRECON_FAILED = 412;
		public static final int HTTP_ENTITY_TOO_LARGE = 413;
		public static final int HTTP_REQ_TOO_LONG = 414;
		public static final int HTTP_UNSUPPORTED_TYPE = 415;
		public static final int HTTP_INTERNAL_ERROR = 500;
		public static final int HTTP_NOT_IMPLEMENTED = 501;
		public static final int HTTP_BAD_GATEWAY = 502;
		public static final int HTTP_UNAVAILABLE = 503;
		public static final int HTTP_GATEWAY_TIMEOUT = 504;
		public static final int HTTP_VERSION = 505;
	}
}
