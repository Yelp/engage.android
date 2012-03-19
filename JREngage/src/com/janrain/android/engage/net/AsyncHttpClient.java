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

 /*
  * The class InflatingEntity and the method setupGoogleInflater are:
  * Copyright 2011 Google Inc.
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

package com.janrain.android.engage.net;

import android.os.Handler;
import android.util.Log;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.utils.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @internal
 *
 * @class AsyncHttpClient
 * Utility class which performs HTTP operations asynchronously.
 **/
public final class AsyncHttpClient {
    private static final String TAG = AsyncHttpClient.class.getSimpleName();
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Droid Build/FRG22D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private AsyncHttpClient() {}

	public static class HttpSender implements Runnable {
		private static final String TAG = HttpSender.class.getSimpleName();

        private String mUrl;
        private List<NameValuePair> mHeaders;
        private byte[] mPostData;
		private Handler mHandler;
        private DefaultHttpClient mHttpClient;
        private HttpUriRequest mRequest;
        private JRConnectionManager.ConnectionData mConnectionData;

        private HttpSender() {}

        public HttpSender(Handler handler, JRConnectionManager.ConnectionData connectionData) {
            mConnectionData = connectionData;
            mUrl = connectionData.getRequestUrl();
            mHeaders = connectionData.getRequestHeaders();
            mPostData = connectionData.getPostData();
            mHandler = handler;
            mRequest = connectionData.getHttpRequest();
        }

        private void setupHttpClient() {
            HttpParams connectionParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(connectionParams, 30000); // thirty seconds
            HttpConnectionParams.setSoTimeout(connectionParams, 30000);
            setupGoogleInflater(connectionParams);
        }

        // From the Google IO app:
        private void setupGoogleInflater(HttpParams connectionParams) {
            mHttpClient = new DefaultHttpClient(connectionParams);
            mHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(HttpRequest request, HttpContext context) {
                    // Add header to accept gzip content
                    if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                        request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                    }
                }
            });

            mHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {
                public void process(HttpResponse response, HttpContext context) {
                    // Inflate any responses compressed with gzip
                    final HttpEntity entity = response.getEntity();
                    if (entity == null) return;
                    final Header encoding = entity.getContentEncoding();
                    if (encoding != null) {
                        for (HeaderElement element : encoding.getElements()) {
                            if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                                response.setEntity(new InflatingEntity(response.getEntity()));
                                break;
                            }
                        }
                    }
                }
            });
        }

        /**
         * @internal 
         * From the Google IO app:
         * Simple {@link HttpEntityWrapper} that inflates the wrapped
         * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
         */
        private static class InflatingEntity extends HttpEntityWrapper {
            public InflatingEntity(HttpEntity wrapped) {
                super(wrapped);
            }

            @Override
            public InputStream getContent() throws IOException {
                return new GZIPInputStream(wrappedEntity.getContent());
            }

            @Override
            public long getContentLength() {
                return -1;
            }
        }

		public void run() {
			JREngage.logd(TAG, "[run] BEGIN, URL: " + mUrl);

            setupHttpClient();

            try {
//                if (!mUrl.contains("mobile_config_and_baseurl") && !mUrl.contains("appspot.com")) {
//                    JREngage.getApplicationContext().runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(JREngage.getApplicationContext(), "MEU load started", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//                try { Thread.sleep(3000); } catch (InterruptedException ignore) {}

                InetAddress ia = InetAddress.getByName(mRequest.getURI().getHost());
                JREngage.logd(TAG, "Connecting to: " + ia.getHostAddress());

                mRequest.addHeader("User-Agent", USER_AGENT);
                if (mHeaders == null) mHeaders = new ArrayList<NameValuePair>();
                for (NameValuePair header : mHeaders) mRequest.addHeader(header.getName(), header.getValue());

                HttpResponse response;
                try {
                    response = mHttpClient.execute(mRequest);
                } catch (IOException e) {
                    // XXX Mediocre way to match exceptions from aborted requests:
                    if (mRequest.isAborted() && e.getMessage().contains("abort")) {
                        throw new AbortedRequestException();
                    } else {
                        throw e;
                    }
                }

                if (mRequest.isAborted()) {
                    throw new AbortedRequestException();
                }

                /* Fetching the status code allows the response interceptor to have a chance to un-gzip the
                 * entity before we fetch it. */
                response.getStatusLine().getStatusCode();

                HttpResponseHeaders headers = HttpResponseHeaders.fromResponse(response, mRequest);

                HttpEntity entity = response.getEntity();
                byte[] data = entity == null? new byte[0] : IOUtils.readFromStream(entity.getContent(), true);
                String dataString = new String(data);
                if (entity != null) entity.consumeContent();

                AsyncHttpResponse ahr;
                switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    JREngage.logd(TAG, "[run] HTTP_OK");
                    JREngage.logd(TAG, "[run] headers: " + headers.toString());
                    JREngage.logd(TAG, "[run] data for " + mUrl + ": " +
                            dataString.substring(0, Math.min(dataString.length(), 600)));
                    ahr = new AsyncHttpResponse(mUrl, headers, data);
                    break;
                case HttpStatus.SC_NOT_MODIFIED:
                    JREngage.logd(TAG, "[run] HTTP_NOT_MODIFIED");
                    ahr = new AsyncHttpResponse(mUrl, headers, data);
                    break;
                case HttpStatus.SC_CREATED:
                    // Response from the Engage trail creation and maybe URL shortening calls
                    JREngage.logd(TAG, "[run] HTTP_CREATED");
                    ahr = new AsyncHttpResponse(mUrl, headers, data);
                    break;
                default:
                    // This shouldn't be globbed together, but instead be structured
                    // to allow the error handler to make meaningful use of the web
                    // servers response (contained in String r)
                    String message = "[run] Unexpected HTTP response:  [responseCode: "
                            + response.getStatusLine().getStatusCode() + " | reasonPhrase: "
                            + response.getStatusLine().getReasonPhrase() + " | entity: "
                            + dataString;

                    Log.e(TAG, message);

                    ahr = new AsyncHttpResponse(mUrl, new Exception(message));
                }

                mConnectionData.setResponse(ahr);
                mHandler.post(new JRConnectionManager.HttpCallbackWrapper(mConnectionData));
            } catch (IOException e) {
                Log.e(TAG, this.toString());
                Log.e(TAG, "[run] Problem executing HTTP request. (" + e +")", e);
                mConnectionData.setResponse(new AsyncHttpResponse(mUrl, e));
                mHandler.post(new JRConnectionManager.HttpCallbackWrapper(mConnectionData));
            } catch (AbortedRequestException e) {
                Log.e(TAG, "[run] Aborted request: " + mUrl);
                mConnectionData.setResponse(new AsyncHttpResponse(mUrl, null));
                mHandler.post(new JRConnectionManager.HttpCallbackWrapper(mConnectionData));
            }
		}

        public String toString() {
            if (mPostData == null) mPostData = new byte[0];
            return "url: " + mUrl + "\nheaders: " + mHeaders + "\npostData: " + new String(mPostData);
        }

        private static class AbortedRequestException extends Exception {}
	}

    /**
     * @internal
     *
     * @class AsyncHttpResponseHolder
     * Wraps the possible data returned from a full asynchronous HTTP request.  This object will
     * contain either headers and data (if successful) or an Exception object (if failed).
     */
    public static class AsyncHttpResponse {
        private String mUrl;
        private HttpResponseHeaders mHeaders;
        private byte[] mPayload;
        private Exception mException;
        private JRConnectionManager.ConnectionData mConnectionData;

        /**
         * Creates a "success" instance of this object.
         *
         * @param url
         *      The URL that this response corresponds to.
         * @param headers
         *      The response headers resulting from the HTTP operation.
         * @param payload
         *      The data resulting from the HTTP operation.
         */
        public AsyncHttpResponse(String url, HttpResponseHeaders headers, byte[] payload) {
            mUrl = url;
            mHeaders = headers;
            mPayload = payload;
            mException = null;
        }

        /**
         * Creates a "failure" instance of this object.
         *
         * @param url
         *      The URL that this response corresponds to.
         * @param exception
         *      The exception that occurred during this HTTP operation.
         */
        public AsyncHttpResponse(String url, Exception exception) {
            mUrl = url;
            mHeaders = null;
            mPayload = null;
            mException = exception;
        }

        /**
         * Gets the URL that this HTTP response (or error) corresponds to.
         *
         * @return
         *      The URL that this HTTP response (or error) corresponds to.
         */
        public String getUrl() {
            return mUrl;
        }

        /**
         * Gets the headers object.  If the operation failed, it will return null.
         *
         * @return
         * 		The HttpResponseHeaders object synthesized from the HTTP response if
         * 		the operation was successful, null otherwise.
         */
        public HttpResponseHeaders getHeaders() {
            return mHeaders;
        }

        /**
         * Gets the payload array.  If the operation failed, it will return null.
         *
         * @return
         * 		The byte array containing the data (payload) from the HTTP response if
         * 		the operation was successful, null otherwise.
         */
        public byte[] getPayload() {
            return mPayload;
        }

        /**
         * Gets the exception object.  If the operation succeeded, it will return null.
         *
         * @return
         * 		The Exception that occurred as a result of the asynchronous HTTP request if
         * 		the operation failed, null otherwise.
         */
        public Exception getException() {
            return mException;
        }

        /**
         * Checks to see if the holder contains a valid (non-null) headers object.
         *
         * @return
         * 		<code>true</code> if the headers object is valid, <code>false</code> otherwise.
         */
        public boolean hasHeaders() {
            return (mHeaders != null);
        }

        /**
         * Checks to see if the holder contains a valid (non-null) payload array.
         *
         * @return
         * 		<code>true</code> if the payload array is valid, <code>false</code> otherwise.
         */
        public boolean hasPayload() {
            return (mPayload != null);
        }

        /**
         * Checks to see if the holder contains a valid (non-null) exception object.
         *
         * @return
         * 		<code>true</code> if the exception object is valid, <code>false</code> otherwise.
         */
        public boolean hasException() {
            return (mException != null);
        }

        public JRConnectionManager.ConnectionData getConnectionData() {
            return mConnectionData;
        }

        public void setConnectionData(JRConnectionManager.ConnectionData cd) {
            mConnectionData = cd;
        }
    }
}
