package at.diamonddogs.net;

import android.content.Context;
import android.os.Environment;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.ResponseBody;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.exception.WebClientException;
import at.diamonddogs.net.ssl.SSLHelper;

public class WebClientOkHttpClient extends WebClient implements HttpRequestRetryHandler, RedirectHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientOkHttpClient.class.getSimpleName());
    private int retryCount = 0;

    private OkUrlFactory urlFactory;
    private HttpURLConnection connection;
    private OkHttpClient httpClient;
    private Request request;
    private Response response;
    /**
     * Constructs a {@link at.diamonddogs.net.WebClient}
     *
     * @param context a context
     */
    public WebClientOkHttpClient(Context context) {
        super(context);
        httpClient = new OkHttpClient();
        urlFactory = new OkUrlFactory(httpClient);
    }

    @Override
    public ReplyAdapter call() {
        ReplyAdapter listenerReply;
        if(webRequest == null){
            throw new WebClientException("WebRequest must not be null!");
        }
        retryCount = webRequest.getNumberOfRetries();
        do {
            try {
                retryCount--;
                WebReply reply;

                Request.Builder requestBuilder = new Request.Builder()
                        .url(webRequest.getUrl());

                configureConnection(requestBuilder);

                request = requestBuilder.build();
                response = httpClient.newCall(request).execute();
                reply = runRequest();

                LOGGER.info("Running RequestBase: " + request);
                LOGGER.error(request.urlString());

                if(needsFollowRedirect(reply)){
                    String url = getRedirectUrl(reply);
                    LOGGER.debug("following redirect manually to new url: " + url);
                    configureConnection(requestBuilder);

                    request = requestBuilder.url(new URL(url)).build();
                    response = httpClient.newCall(request).execute();
                    reply = runRequest();
                }

                listenerReply = createListenerReply(webRequest, reply, null, ReplyAdapter.Status.OK);
                int status = ((WebReply) listenerReply.getReply()).getHttpStatusCode();
                if (!(status == -1)) {
                    retryCount = -1;
                }
            } catch (Throwable tr){
                if(retryCount != 0){
                    try {
                        Thread.sleep(webRequest.getRetryInterval());
                    }catch (InterruptedException e){
                        LOGGER.error("Error in WebRequest: " + webRequest,e);
                    }
                }
                listenerReply = createListenerReply(webRequest, null, tr, ReplyAdapter.Status.FAILED);
                LOGGER.info("Error running webrequest: " + webRequest.getUrl(), tr);
            }
        } while ( retryCount >= 0);

        if(webClientReplyListener != null){
            webClientReplyListener.onWebReply(this, listenerReply);
        }
        return listenerReply;
    }

    private String getRedirectUrl(WebReply wr) {
        return wr.getReplyHeader().get("location").get(0);
    }

    private boolean needsFollowRedirect(WebReply wr) {
        if (!followProtocolRedirect || !webRequest.isFollowRedirects()) {
            return false;
        }
        if (wr.getHttpStatusCode() == HTTPStatus.HTTP_MOVED_TEMP
                || wr.getHttpStatusCode() == HTTPStatus.HTTP_MOVED_PERM) {
            return true;
        }
        return false;
    }

    private void configureConnection(Request.Builder requestBuilder) throws IOException {
        httpClient.setConnectTimeout(webRequest.getConnectionTimeout(), TimeUnit.MILLISECONDS);
        httpClient.setReadTimeout(webRequest.getReadTimeout(), TimeUnit.MILLISECONDS);
        httpClient.setFollowRedirects(webRequest.isFollowRedirects());

        setSslFactory();
        setRequestType(requestBuilder);
        buildHeader(requestBuilder);
    }

    private void setSslFactory() {
        SSLSocketFactory sslSocketFactory = SSLHelper.getInstance().SSL_FACTORY_JAVA;
        httpClient.setSslSocketFactory(sslSocketFactory);
    }

    private void setRequestType(Request.Builder requestBuilder) throws IOException {
        switch (webRequest.getRequestType()) {
            case POST:
                requestBuilder.method("POST", getRequestBody());
                break;
            case GET:
                requestBuilder.method("GET", null);
                break;
            case HEAD:
                requestBuilder.method("HEAD", null);
                break;
        }
        request = requestBuilder.build();
    }

    private RequestBody getRequestBody() throws IOException {
        HttpEntity he = webRequest.getHttpEntity();
        InputStream content;
        int bytesRead;
        byte buffer[] = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if(he == null){
            return null;
        }
        else{
            content = he.getContent();
            while ((bytesRead = content.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return RequestBody.create(MediaType.parse("text/xml"), baos.toByteArray());
        }
    }

    @Override
    protected void buildHeader(Request.Builder requestBuilder) {
        Map<String, String> header = webRequest.getHeader();
        if (header != null) {
            for (String field : header.keySet()) {
                if (webRequest.isAppendHeader()) {
                    //request.newBuilder().addHeader(field, header.get(field));
                    //connection.addRequestProperty(field, header.get(field));
                    requestBuilder.addHeader(field, header.get(field)).build();
                } else {
                    //response.request().newBuilder().header(field, header.get(field));
                    requestBuilder.header(field, header.get(field)).build();
                    //response.request().newBuilder().addHeader(field, header.get(field));
                    //connection.setRequestProperty(field, header.get(field));
                }
            }
        }
    }

    private WebReply runRequest() throws IOException {
        //int statusCode = response.getStatusLine().getStatusCode();
        int statusCode = response.code();
        WebReply reply = null;

        switch (statusCode) {
            case HttpStatus.SC_PARTIAL_CONTENT: //206
            case HttpStatus.SC_OK: //200LOGGER.debug("WebRequest OK: " + webRequest);
                publishFileSize(request.headers().size());
                //publishFileSize(getRequestBody().contentLength());
                //publishFileSize(request.body().contentLength());
                reply = handleResponseOk(response.body().byteStream(), statusCode, convertHeaders(response.headers()));
                break;
            case HttpStatus.SC_NOT_MODIFIED: //304
                LOGGER.debug("WebRequest Not modified: " + webRequest);
                reply = handleResponseNotModified(statusCode, convertHeaders(response.headers()));
                break;
            case HttpStatus.SC_NO_CONTENT: //204
                reply = handleResponseOk(null, statusCode, convertHeaders(response.headers()));
            default:
                LOGGER.debug("WebRequest DEFAULT: " + webRequest);
                ResponseBody rBody = response.body();
                if (rBody != null) {
                    InputStream content = rBody.byteStream();
                    writeErrorLog(content);
                    reply = handleResponseNotOk(content, statusCode, convertHeaders(response.headers()));
                } else {
                    reply = handleResponseNotOk(null, statusCode, convertHeaders(response.headers()));
                }
                break;
        }
        return reply;
    }

    private Map<String, List<String>> convertHeaders(Headers headers) {
        HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
/*        //for (Headers h : headers) {
            String key = headers.names().toString();
            //String value = headers.values();
            if (ret.containsKey(key)) {
                //ret.get(key).add(value);
            } else {
                List<String> values = new ArrayList<String>(10);
                //values.add(value);
                ret.put(key, values);
            }
        //}*/
        for(int i=0; i< headers.size();i++){
            String key = headers.name(i).toString();
            String value = headers.value(i).toString();
            //String value = headers.values();
            if (ret.containsKey(key)) {
                ret.get(key).add(value);
            } else {
                List<String> values = new ArrayList<>(10);
                values.add(value);
                ret.put(key, values);
            }
        }
        return ret;
    }

    private void writeErrorLog(InputStream content) {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), "errorlog.txt");
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = content.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
        } catch (Exception e) {
            LOGGER.error("error writing log", e);
        }

    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        LOGGER.error("executionCount:" + executionCount + " NumberOfRetries: " + webRequest.getNumberOfRetries() + " exception: "
                + exception.toString());
        if (executionCount >= webRequest.getNumberOfRetries()) {
            return false;
        }
        // @formatter:off
        if ((exception instanceof NoHttpResponseException) || (exception instanceof ConnectTimeoutException)
                || (exception instanceof ConnectionPoolTimeoutException) || (exception instanceof SocketTimeoutException)) {
            return true;
        }
        // @formatter:on
        return false;
    }

    @Override
    public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
        if (!webRequest.isFollowRedirects()) {
            return false;
        }
        int status = response.getStatusLine().getStatusCode();
        LOGGER.debug("isRedirectRequested: " + status);
        if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
            return true;
        }
        return false;
    }

    @Override
    public URI getLocationURI(HttpResponse response, HttpContext context) throws org.apache.http.ProtocolException {
        Header[] headers = response.getHeaders("location");
        if (headers[0] != null) {
            try {
                LOGGER.error("getLocationURI: " + headers[0].getValue());
                return new URI(headers[0].getValue());
            } catch (URISyntaxException e) {
                LOGGER.error("error parsing url", e);
                return null;
            }
        }
        return null;
    }
}
