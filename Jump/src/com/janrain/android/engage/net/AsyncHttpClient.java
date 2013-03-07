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
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.utils.IOUtils;
import com.janrain.android.utils.LogUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.zip.GZIPInputStream;

import static com.janrain.android.engage.net.JRConnectionManager.ManagedConnection;
import static com.janrain.android.engage.net.async.HttpResponseHeaders.fromResponse;

/**
 * @internal
 *
 * @class AsyncHttpClient
 * Utility class which performs HTTP operations asynchronously.
 **/
/*package*/ class AsyncHttpClient {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Droid Build/FRG22D) AppleWebKit/533.1 " +
                    "(KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private AsyncHttpClient() {}

    /*package*/ static class HttpExecutor implements Runnable {
		final private Handler mHandler;
        final private ManagedConnection mConn;
        final private DefaultHttpClient mHttpClient;

        /*package*/ HttpExecutor(Handler handler, ManagedConnection managedConnection) {
            mConn = managedConnection;
            mHandler = handler;
            mHttpClient = setupHttpClient();
        }

        private DefaultHttpClient setupHttpClient() {
            HttpParams connectionParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(connectionParams, 30000); // thirty seconds
            HttpConnectionParams.setSoTimeout(connectionParams, 30000);
            DefaultHttpClient client = new DefaultHttpClient(connectionParams);

            evolveGzipEncoding(client);

            if (!mConn.getFollowRedirects()) disableRedirects(client);

            return client;
        }

        private static void evolveGzipEncoding(DefaultHttpClient client) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(HttpRequest request, HttpContext context) {
                    // Add header to accept gzip content
                    if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                        request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                    }
                }
            });

            client.addResponseInterceptor(new HttpResponseInterceptor() {
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

        private static void disableRedirects(DefaultHttpClient client) {
            client.setRedirectHandler(new DefaultRedirectHandler() {
                @Override
                public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                    if (super.isRedirectRequested(response, context)) {
                        LogUtils.loge("error: ignoring redirect");
                    }
                    return false;
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
			LogUtils.logd("[run] BEGIN, URL: " + mConn.getRequestUrl());

            setupHttpClient();

            JRConnectionManager.HttpCallback callBack =
                    new JRConnectionManager.HttpCallback(mConn);
            try {
                InetAddress ia = InetAddress.getByName(mConn.getHttpRequest().getURI().getHost());
                LogUtils.logd("Connecting to: " + ia.getHostAddress());

                mConn.getHttpRequest().addHeader("User-Agent", USER_AGENT);
                for (NameValuePair header : mConn.getRequestHeaders()) {
                    mConn.getHttpRequest().addHeader(header.getName(), header.getValue());
                }

                HttpResponse response;
                try {
                    response = mHttpClient.execute(mConn.getHttpRequest());
                } catch (IOException e) {
                    // XXX Mediocre way to match exceptions from aborted requests:
                    if (mConn.getHttpRequest().isAborted() && e.getMessage().contains("abort")) {
                        throw new AbortedRequestException();
                    } else {
                        throw e;
                    }
                }

                if (mConn.getHttpRequest().isAborted()) throw new AbortedRequestException();

                /* Fetching the status code allows the response interceptor to have a chance to un-gzip the
                 * entity before we fetch it. */
                response.getStatusLine().getStatusCode();

                HttpResponseHeaders headers = fromResponse(response, mConn.getHttpRequest());

                HttpEntity entity = response.getEntity();
                byte[] data = entity == null ?
                        new byte[0] :
                        IOUtils.readFromStream(entity.getContent(), true);
                String dataString = new String(data);
                if (entity != null) entity.consumeContent();

                AsyncHttpResponse ahr;
                switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    LogUtils.logd("[run] HTTP_OK");
                    LogUtils.logd("[run] headers: " + headers.toString());
                    LogUtils.logd("[run] data for " + mConn.getRequestUrl() + ": " +
                            dataString.substring(0, Math.min(dataString.length(), 600)));
                    ahr = new AsyncHttpResponse(mConn, headers, data);
                    break;
                case HttpStatus.SC_NOT_MODIFIED:
                    LogUtils.logd("[run] HTTP_NOT_MODIFIED");
                    ahr = new AsyncHttpResponse(mConn, headers, data);
                    break;
                case HttpStatus.SC_CREATED:
                    // Response from the Engage trail creation and maybe URL shortening calls
                    LogUtils.logd("[run] HTTP_CREATED");
                    ahr = new AsyncHttpResponse(mConn, headers, data);
                    break;
                default:
                    // This shouldn't be globbed together, but instead be structured
                    // to allow the error handler to make meaningful use of the web
                    // servers response (contained in String r)
                    String message = "[run] Unexpected HTTP response for "
                            + mConn.getRequestUrl() + " :  [responseCode: "
                            + response.getStatusLine().getStatusCode() + " | reasonPhrase: "
                            + response.getStatusLine().getReasonPhrase() + " | entity: "
                            + dataString;

                    LogUtils.loge(message);

                    ahr = new AsyncHttpResponse(mConn, new Exception(message));
                }

                mConn.setResponse(ahr);
                invokeCallback(callBack);
            } catch (IOException e) {
                LogUtils.loge(this.toString());
                LogUtils.loge("[run] Problem executing HTTP request. (" + e + ")", e);
                mConn.setResponse(new AsyncHttpResponse(mConn, e));
                invokeCallback(callBack);
            } catch (AbortedRequestException e) {
                LogUtils.loge("[run] Aborted request: " + mConn.getRequestUrl());
                mConn.setResponse(new AsyncHttpResponse(mConn, null));
                invokeCallback(callBack);
            }
		}

        private void invokeCallback(JRConnectionManager.HttpCallback callBack) {
            if (mHandler != null) {
                mHandler.post(callBack);
            } else {
                callBack.run();
            }
        }

        public String toString() {
            String postData = mConn.getPostData() == null ? "null" : new String(mConn.getPostData());
            return "url: " + mConn.getRequestUrl() + "\nheaders: " + mConn.getRequestHeaders()
                    + "\npostData: " + postData;
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
    /*package*/ static class AsyncHttpResponse {
        final private HttpResponseHeaders mHeaders;
        final private byte[] mPayload;
        final private Exception mException;
        final private ManagedConnection mManagedConnection;

        private AsyncHttpResponse(ManagedConnection conn, HttpResponseHeaders headers, byte[] payload) {
            mManagedConnection = conn;
            mHeaders = headers;
            mPayload = payload;
            mException = null;
        }

        private AsyncHttpResponse(ManagedConnection conn, Exception exception) {
            mManagedConnection = conn;
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
        /*package*/ String getUrl() {
            return mManagedConnection.getRequestUrl();
        }

        /**
         * Gets the headers object.  If the operation failed, it will return null.
         *
         * @return
         * 		The HttpResponseHeaders object synthesized from the HTTP response if
         * 		the operation was successful, null otherwise.
         */
        /*package*/ HttpResponseHeaders getHeaders() {
            return mHeaders;
        }

        /**
         * Gets the payload array.  If the operation failed, it will return null.
         *
         * @return
         * 		The byte array containing the data (payload) from the HTTP response if
         * 		the operation was successful, null otherwise.
         */
        /*package*/ byte[] getPayload() {
            return mPayload;
        }

        /**
         * Gets the exception object.  If the operation succeeded, it will return null.
         *
         * @return
         * 		The Exception that occurred as a result of the asynchronous HTTP request if
         * 		the operation failed, null otherwise.
         */
        /*package*/ Exception getException() {
            return mException;
        }

        /**
         * Checks to see if the holder contains a valid (non-null) headers object.
         *
         * @return
         * 		<code>true</code> if the headers object is valid, <code>false</code> otherwise.
         */
        /*package*/ boolean hasHeaders() {
            return (mHeaders != null);
        }

        /**
         * Checks to see if the holder contains a valid (non-null) payload array.
         *
         * @return
         * 		<code>true</code> if the payload array is valid, <code>false</code> otherwise.
         */
        /*package*/ boolean hasPayload() {
            return (mPayload != null);
        }

        /**
         * Checks to see if the holder contains a valid (non-null) exception object.
         *
         * @return
         * 		<code>true</code> if the exception object is valid, <code>false</code> otherwise.
         */
        /*package*/ boolean hasException() {
            return (mException != null);
        }
    }
}
