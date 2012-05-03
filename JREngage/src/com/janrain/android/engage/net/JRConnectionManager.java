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
package com.janrain.android.engage.net;

import android.os.Handler;
import android.os.Looper;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.utils.AndroidUtils;
import com.janrain.android.engage.utils.ThreadUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @internal
 *
 * @class JRConnectionManager
 **/
public class JRConnectionManager {
    private static final String TAG = JRConnectionManager.class.getSimpleName();
	private static JRConnectionManager sInstance;

    private JRConnectionManager() {}

    public static synchronized JRConnectionManager getInstance() {
		if (sInstance == null) sInstance = new JRConnectionManager();
		return sInstance;
	}

    /**
     * Creates a managed HTTP GET connection.
     *
     * @param requestUrl
     *      The URL to be executed. May not be null.
     *
     * @param delegate
     *      The delegate (listener) class instance. May be null.
     * @param tag
     *      Optional tag for the connection, later passed to the delegate for the purpose of distinguishing
     *      multiple connections handled by a single delegate.
     */
	public static void createConnection(String requestUrl,
                                        JRConnectionManagerDelegate delegate,
                                        Object tag) {
        createConnection(requestUrl, delegate, tag, null);
	}

    /**
     * Creates a managed HTTP GET connection.
     *
     * @param requestUrl
     *      The URL to be executed. May not be null.
     * @param delegate
     *      The delegate (listener) class instance. May be null. Callback methods will be invoked on the UI
     *      thread.
     * @param tag
     *      Optional tag for the connection, later passed to the delegate for the purpose of distinguishing
     *      multiple connections handled by a single delegate.
     * @param requestHeaders
     *      Optional list of HTTP headers to add to the request.
     *
     */
    public static void createConnection(String requestUrl,
                                        JRConnectionManagerDelegate delegate,
                                        Object tag,
                                        List<NameValuePair> requestHeaders) {
        if (requestHeaders == null) requestHeaders = new ArrayList<NameValuePair>();

        ConnectionData connectionData = new ConnectionData(delegate, tag);
        connectionData.mRequestUrl = requestUrl;
        connectionData.mRequestHeaders = requestHeaders;

        trackAndStartConnection(delegate, connectionData);
    }

    private static void trackAndStartConnection(JRConnectionManagerDelegate delegate,
                                                ConnectionData connectionData) {
        HttpUriRequest request;
        if (connectionData.mPostData != null) {
            request = new HttpPost(connectionData.mRequestUrl);
            ((HttpPost) request).setEntity(new ByteArrayEntity(connectionData.mPostData));
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            request.addHeader("Content-Language", "en-US");
        } else {
            request = new HttpGet(connectionData.mRequestUrl);
        }

        connectionData.mHttpRequest = request;

        synchronized (sDelegateConnections) {
            Set<ConnectionData> s = sDelegateConnections.get(delegate);
            if (s == null) {
                s = new AndroidUtils.SetFromMap<ConnectionData>(new WeakHashMap<ConnectionData, Boolean>());
                sDelegateConnections.put(delegate, s);
            }
            s.add(connectionData);
        }

        if (Looper.myLooper() != null) {
            // operate asynchronously, post a message back to the thread later
            ThreadUtils.mExecutor.execute(new AsyncHttpClient.HttpExecutor(new Handler(), connectionData));
        } else {
            // operate synchronously
            new AsyncHttpClient.HttpExecutor(null, connectionData).run();
        }

        JREngage.logd(TAG, "[executeHttpRequest] invoked");
    }

    // I think this map can only be operated on by the UI thread but I'm not sure :(
    // ... so I've wrapped it in Collections.synchronizedMap
    private static final Map<JRConnectionManagerDelegate, Set<ConnectionData>> sDelegateConnections =
            Collections.synchronizedMap(new WeakHashMap<JRConnectionManagerDelegate, Set<ConnectionData>>());
    
    public static void stopConnectionsForDelegate(JRConnectionManagerDelegate delegate) {
        synchronized (sDelegateConnections) {
            Set<ConnectionData> s = sDelegateConnections.get(delegate);
            if (s != null) {
                for (ConnectionData c : s) {
                    c.mDelegate = null;
//                    c.mHttpRequest.abort();
                }
            }
        }
    }

    /**
     * Creates a managed HTTP POST connection.
     *
     * @param requestUrl
     *      The URL to be executed. May not be null.
     * @param delegate
     *      The delegate (listener) class instance. May be null. Callback methods will be invoked on the UI
     *      thread.
     * @param tag
     *      Optional tag for the connection, later passed to the delegate for the purpose of distinguishing
     *      multiple connections handled by a single delegate.
     * @param requestHeaders
     *      Optional list of HTTP headers to add to the request.
     * @param postData
     *      Optional data to post.
     */
    public static void createConnection(String requestUrl,
                                        JRConnectionManagerDelegate delegate,
                                        Object tag,
                                        List<NameValuePair> requestHeaders,
                                        byte[] postData) {
        if (postData == null) postData = new byte[0];
        if (requestHeaders == null) requestHeaders = new ArrayList<NameValuePair>();

        ConnectionData connectionData = new ConnectionData(delegate, tag);
        connectionData.mRequestUrl = requestUrl;
        connectionData.mPostData = postData;
        connectionData.mRequestHeaders = requestHeaders;

        trackAndStartConnection(delegate, connectionData);
    }

    public static class ConnectionData {
        private HttpUriRequest mHttpRequest;
        private Object mTag;
        private JRConnectionManagerDelegate mDelegate;
        private String mRequestUrl;
        private byte[] mPostData;
        private List<NameValuePair> mRequestHeaders;
        private AsyncHttpClient.AsyncHttpResponse mResponse;

        public ConnectionData(JRConnectionManagerDelegate delegate,
                              Object tag) {
            mDelegate = delegate;
            mTag = tag;
        }

        public String getRequestUrl() {
            return mRequestUrl;
        }

        public byte[] getPostData() {
            return mPostData;
        }

        public List<NameValuePair> getRequestHeaders() {
            return mRequestHeaders;
        }

        public HttpUriRequest getHttpRequest() {
            return mHttpRequest;
        }

        public void setResponse(AsyncHttpClient.AsyncHttpResponse response) {
            mResponse = response;
        }
    }

    public static class HttpCallbackWrapper implements Runnable {
        private ConnectionData mConnectionData;

        public HttpCallbackWrapper(ConnectionData cd) {
            mConnectionData = cd;
        }

        public void run() {
            AsyncHttpClient.AsyncHttpResponse response = mConnectionData.mResponse;
            String requestUrl = response.getUrl();
            JRConnectionManagerDelegate delegate = mConnectionData.mDelegate;

            if (delegate == null || mConnectionData.mHttpRequest.isAborted()) return;

            if (response.hasException()) {
                delegate.connectionDidFail(response.getException(), requestUrl, mConnectionData.mTag);
            } else {
                delegate.connectionDidFinishLoading(
                        response.getHeaders(),
                        response.getPayload(),
                        requestUrl,
                        mConnectionData.mTag);
            }
        }
    }
}
