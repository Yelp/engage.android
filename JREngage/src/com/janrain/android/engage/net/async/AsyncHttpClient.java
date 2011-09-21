/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android.engage.net.async;

import android.os.Handler;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.utils.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @internal
 *
 * @class AsyncHttpClient
 * Utility class which performs HTTP operations asynchronously.
 **/
public final class AsyncHttpClient {
    private static final String TAG = AsyncHttpClient.class.getSimpleName();
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Droid Build/FRG22D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    private static final String ACCEPT_ENCODING = "identity";

    private AsyncHttpClient() {}

	private static class HttpSender extends Thread {
		private static final String TAG = HttpSender.class.getSimpleName();

        private String mUrl;
        private List<NameValuePair> mHeaders;
        private byte[] mPostData;
		private Handler mHandler;
		private HttpCallbackWrapper mWrapper;

        public HttpSender(String url, List<NameValuePair> requestHeaders,
                          Handler handler, HttpCallbackWrapper wrapper) {
            mUrl = url;
            mHeaders = requestHeaders;
            mPostData = null;
            mHandler = handler;
            mWrapper = wrapper;
        }

        public HttpSender(String url, byte[] postData,
                          Handler handler, HttpCallbackWrapper wrapper) {
            mUrl = url;
            mHeaders = null;
            mPostData = postData;
            mHandler = handler;
            mWrapper = wrapper;
        }

		public void run() {
			if (Config.LOGD) Log.d(TAG, "[run] BEGIN, url: " + mUrl);

            try {
                HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(params, 10000); // ten second timeout
                HttpConnectionParams.setSoTimeout(params, 10000);

                DefaultHttpClient c = new DefaultHttpClient(params);
                HttpUriRequest request;
                if (mPostData != null) {
                    request = new HttpPost(mUrl);
                    ((HttpPost) request).setEntity(new ByteArrayEntity(mPostData));
                    request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                    request.addHeader("Content-Language", "en-US");
                } else {
                    request = new HttpGet(mUrl);
                }

                request.addHeader("User-Agent", USER_AGENT);
                request.addHeader("Accept-Encoding", ACCEPT_ENCODING);
                if (mHeaders == null) mHeaders = new ArrayList<NameValuePair>();
                for (NameValuePair header : mHeaders) request.addHeader(header.getName(), header.getValue());

                HttpResponse response = c.execute(request);

                HttpResponseHeaders headers = HttpResponseHeaders.fromConnection(response);

                HttpEntity entity = response.getEntity();
                byte[] data = entity == null? new byte[0] : IOUtils.readFromStream(entity.getContent(), true);
                String dataString = new String(data);

                switch (response.getStatusLine().getStatusCode()) {
                case HttpURLConnection.HTTP_OK:
                    if (Config.LOGD) Log.d(TAG, "[run] HTTP_OK");
                    if (Config.LOGD) Log.d(TAG, "[run] headers: " + headers.toString());
                    if (Config.LOGD) Log.d(TAG, "[run] data: " + dataString);
                    mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, headers, data));
                    break;
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    if (Config.LOGD) Log.d(TAG, "[run] HTTP_NOT_MODIFIED");
                    mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, headers, data));
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    // Response from the Engage trail creation and maybe URL shortening calls
                    if (Config.LOGD) Log.d(TAG, "[run] HTTP_CREATED");
                    mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, headers, data));
                    break;
                default:
                    // Maybe this shouldn't be globbed together, but instead be structured
                    // to allow the error handler to make meaningful use of the web
                    // servers response (contained in String r)
                    String message = "[run] Unexpected HTTP response:  [responseCode: "
                            + response.getStatusLine().getStatusCode() + " | responseMessage: "
                            + response.getStatusLine().getReasonPhrase() + " | errorStream: "
                            + dataString;

                    Log.e(TAG, message);

                    mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, new Exception(message)));
                }

                mHandler.post(mWrapper);
            } catch (IOException e) {
                Log.e(TAG, "[run] Problem executing HTTP request.", e);
                Log.e(TAG, this.toString());
                mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, e));
                mHandler.post(mWrapper);
            }
		}

        public String toString() {
            if (mPostData == null) mPostData = new byte[0];
            return TAG + ": {url: " + mUrl + "\nheaders: " + mHeaders + "\npostData: "
                    + new String(mPostData);
        }
	}

	/*
	 * Sends full response (or exception) back to the listener.
	 */
	private static class HttpCallbackWrapper implements Runnable {
		private static final String TAG = HttpCallbackWrapper.class.getSimpleName();

		private AsyncHttpResponseListener mListener;
		private AsyncHttpResponseHolder mResponse;
        private JRConnectionManager.ConnectionData mConnectionData;

		public HttpCallbackWrapper(AsyncHttpResponseListener listener,
                                   JRConnectionManager.ConnectionData cd) {
            mConnectionData = cd;
			mListener = listener;
		}

		public void run() {
			mListener.onResponseReceived(mResponse);
		}

		public void setResponse(AsyncHttpResponseHolder holder) {
			mResponse = holder;
            mResponse.setConnectionData(mConnectionData);
			if (Config.LOGD) Log.d(TAG, "[setResponse] response set.");
		}
	}

	/**
	 * Executes the specified HTTP GET request asynchronously.  The results will be returned to
	 * the specified listener.
	 *
	 * @param cd
     *      The connection 
	 * @param listener
	 * 		The AsyncHttpResponseListener to return the results to.
	 **/
    public static void executeHttpGet(JRConnectionManager.ConnectionData cd,
                                      AsyncHttpResponseListener listener) {
        final String url = cd.getRequestUrl();
        List<NameValuePair> requestHeaders = cd.getRequestHeaders();
        if (Config.LOGD) Log.d(TAG, "[executeHttpGet] invoked");


        (new HttpSender(url, requestHeaders, new Handler(),
                new HttpCallbackWrapper(listener, cd))).start();
    }

    /**
     * Executes the specified HTTP POST request asynchronously.  The results will be returned to
     * the specified listener.
     *
     * @param cd
     * 		The connection data, which must include URL and \c byte[] data.
     * @param listener
     * 		The AsyncHttpResponseListener to return the results to.
     **/
    public static void executeHttpPost(JRConnectionManager.ConnectionData cd,
                                       AsyncHttpResponseListener listener) {
        final String url = cd.getRequestUrl();
        byte[] data = cd.getPostData();

        if (Config.LOGD) Log.d(TAG, "[executeHttpPost] invoked");

        (new HttpSender(url, data, new Handler(),
                new HttpCallbackWrapper(listener, cd))).start();

    }
}
