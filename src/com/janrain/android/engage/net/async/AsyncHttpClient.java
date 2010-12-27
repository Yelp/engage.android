/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 Copyright (c) 2010, Janrain, Inc.
 
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, 
 this list of conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution. 
 * Neither the name of the Janrain, Inc. nor the names of its
 contributors may be used to endorse or promote products derived from this
 software without specific prior written permission.
 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
*/
package com.janrain.android.engage.net.async;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.janrain.android.engage.utils.IOUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.util.Config;
import android.util.Log;

/**
 * Utility class which performs HTTP operations asynchronously.
 */
public final class AsyncHttpClient {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

	/*
	 * Sends HTTP request in background, loads header and response, and returns via handler.
	 */
	private static class HttpSender extends Thread {
		
		private static final String TAG = HttpSender.class.getSimpleName();

        private String mUrl;
        private byte[] mPostData;
		private Handler mHandler;
		private HttpCallbackWrapper mWrapper;
		
        public HttpSender(String url, Handler handler, HttpCallbackWrapper wrapper) {
            mUrl = url;
            mPostData = null;
            mHandler = handler;
            mWrapper = wrapper;
        }

        public HttpSender(String url, byte[] postData, Handler handler, HttpCallbackWrapper wrapper) {
            mUrl = url;
            mPostData = postData;
            mHandler = handler;
            mWrapper = wrapper;
        }

		public void run() {
			if (Config.LOGD) { Log.d(TAG, "[run] BEGIN."); }

            URL url;
            HttpURLConnection connection = null;

            try {
                url = new URL(mUrl);
                connection = (HttpURLConnection) url.openConnection();

                if (mPostData == null) {
                    // HTTP GET OPERATION
                    prepareConnectionForHttpGet(connection);
                    connection.connect();
                } else {
                    // HTTP POST OPERATION
                    prepareConnectionForHttpPost(connection);
                    doHttpPost(connection);
                }


                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    HttpResponseHeaders headers = HttpResponseHeaders.fromConnection(connection);
                    byte[] data = IOUtils.readFromStream(connection.getInputStream(), true);
                    mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, headers, data));
                    mHandler.post(mWrapper);
                } else {
                    String message = "[run] Unexpected HTTP response:  [code: "
                            + connection.getResponseCode() + " | message: "
                            + connection.getResponseMessage()
                            + "]";

                    Log.e(TAG, message);
                    mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, new Exception(message)));
                    mHandler.post(mWrapper);
                }
            } catch (IOException e) {
                Log.e(TAG, "[run] Problem executing HTTP request.", e);
                mWrapper.setResponse(new AsyncHttpResponseHolder(mUrl, e));
                mHandler.post(mWrapper);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }
            }
		}

        private void prepareConnectionForHttpGet(HttpURLConnection connection) throws IOException {
            connection.setRequestMethod(HTTP_METHOD_GET);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
        }

        private void prepareConnectionForHttpPost(HttpURLConnection connection) throws IOException {
            connection.setRequestMethod(HTTP_METHOD_POST);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + mPostData.length);
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
        }

        private void doHttpPost(HttpURLConnection connection) throws IOException {
            DataOutputStream writer = null;
            try {
                writer = new DataOutputStream(connection.getOutputStream());
                writer.write(mPostData);
                writer.flush();
            } catch (IOException e) {
                throw e;
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }

	}
	
	/*
	 * Sends full response (or exception) back to the listener.
	 */
	private static class HttpCallbackWrapper implements Runnable {

		private static final String TAG = HttpCallbackWrapper.class.getSimpleName();
		
		private AsyncHttpResponseListener mListener;
		private AsyncHttpResponseHolder mResponse;
		
		public HttpCallbackWrapper(AsyncHttpResponseListener listener) {
			mListener = listener;
		}
		
		public void run() {
			mListener.onResponseReceived(mResponse);
		}
		
		public void setResponse(AsyncHttpResponseHolder holder) {
			mResponse = holder;
			Log.d(TAG, "[setResponse] response set.");
		}
	}

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	private static final String TAG = AsyncHttpClient.class.getSimpleName();

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String USER_AGENT = "Android Janrain Engage/1.0.0";
    private static final String ACCEPT_ENCODING = "identity";
    private static final int DEFAULT_TIMEOUT = 30000;  // 30 seconds

	
    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	/**
	 * Executes the specified HTTP GET request asynchronously.  The results will be returned to
	 * the specified listener.
	 * 
	 * @param url
	 * 		The URL to be executed asynchronously.
	 * @param listener
	 * 		The AsyncHttpResponseListener to return the results to.
	 */
    public static void executeHttpGet(final String url, AsyncHttpResponseListener listener) {
        if (Config.LOGD) {
            Log.d(TAG, "[executeHttpGet] invoked");
        }

        (new HttpSender(url, new Handler(), new HttpCallbackWrapper(listener))).start();
    }

    /**
     * Executes the specified HTTP POST request asynchronously.  The results will be returned to
     * the specified listener.
     *
     * @param url
     * 		The URL to be executed asynchronously.
     * @param data
     *      The data to be posted (written) to the server.
     * @param listener
     * 		The AsyncHttpResponseListener to return the results to.
     */
    public static void executeHttpPost(final String url, byte[] data, AsyncHttpResponseListener listener) {
        if (Config.LOGD) {
            Log.d(TAG, "[executeHttpPost] invoked");
        }

        (new HttpSender(url, data, new Handler(), new HttpCallbackWrapper(listener))).start();

    }

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private AsyncHttpClient() {
		/* no instance */
	}
	
}
