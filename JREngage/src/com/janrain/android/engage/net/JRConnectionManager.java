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
package com.janrain.android.engage.net;

import com.janrain.android.engage.net.async.AsyncHttpClient;
import com.janrain.android.engage.net.async.AsyncHttpResponseHolder;
import com.janrain.android.engage.net.async.AsyncHttpResponseListener;
import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @internal
 *
 * @class JRConnectionManager
 **/
public class JRConnectionManager implements AsyncHttpResponseListener {
    /**
     * Connection data for managed connections.
     */
	public static class ConnectionData {
		private boolean mShouldReturnFullResponse;
		private Object mTag;
		private JRConnectionManagerDelegate mDelegate;
        private String mRequestUrl;
        private byte[] mPostData;

        public String getRequestUrl() {
            return mRequestUrl;
        }

        public byte[] getPostData() {
            return mPostData;
        }

        public List<NameValuePair> getRequestHeaders() {
            return mRequestHeaders;
        }

        private List<NameValuePair> mRequestHeaders;

        public ConnectionData(JRConnectionManagerDelegate delegate,
				boolean shouldReturnFullResponse, Object userdata) {
			mShouldReturnFullResponse = shouldReturnFullResponse;
			mDelegate = delegate;
			mTag = userdata;
		}

        public void setRequestUrl(String r) {
            mRequestUrl = r;
        }

        public void setPostData(byte[] d) {
            mPostData = d;
        }

        public void setRequestHeaders(List<NameValuePair> requestHeaders) {
            mRequestHeaders = requestHeaders;
        }
    }

	private static JRConnectionManager sInstance;

    private JRConnectionManager() {
    }

	public static synchronized JRConnectionManager getInstance() {
		if (sInstance == null) {
			sInstance = new JRConnectionManager();
		}
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
     *
     * @param shouldReturnFullResponse
     *      <code>true</code> to call the "full response" connectionDidFinishLoading delegate method --
     *      connectionDidFinishLoading(HttpResponseHeaders, byte[], String,  Object) --  which
     *      includes the HTTP headers, or <code>false</code> to call
     *      connectionDidFinishLoading(String, String, Object)
     *
     * @param userdata
     *      The tag object associated with the connection. May be null.
     */
	public static void createConnection(String requestUrl,
                                           JRConnectionManagerDelegate delegate,
                                           boolean shouldReturnFullResponse,
                                           Object userdata) {

        createConnection(requestUrl, delegate, shouldReturnFullResponse,
                new ArrayList<NameValuePair>(), userdata);
	}

    /**
     * Creates a managed HTTP GET connection.
     *
     * @param requestUrl
     *      The URL to be executed. May not be null.
     *
     * @param delegate
     *      The delegate (listener) class instance. May be null.
     *
     * @param shouldReturnFullResponse
     *      <code>true</code> to call the "full response" connectionDidFinishLoading delegate method --
     *      connectionDidFinishLoading(HttpResponseHeaders, byte[], String,  Object) --  which
     *      includes the HTTP headers, or <code>false</code> to call
     *      connectionDidFinishLoading(String, String, Object)
     *
     * @param userdata
     *      The tag object associated with the connection. May be null.
     *
     * @param requestHeaders
     *      Any additional headers to be sent with the connection.
     *
     */
    public static void createConnection(String requestUrl,
                                           JRConnectionManagerDelegate delegate,
                                           boolean shouldReturnFullResponse,
                                           List<NameValuePair> requestHeaders,
                                           Object userdata) {

		// get singleton instance (lazy init)
		JRConnectionManager instance = getInstance();

        ConnectionData cd = new ConnectionData(delegate, shouldReturnFullResponse, userdata);
        cd.setRequestUrl(requestUrl);
        cd.setRequestHeaders(requestHeaders);

		// create/store connection data
		instance.mConnectionBuffers.add(cd);

		// execute HTTP GET request
		AsyncHttpClient.executeHttpGet(cd, instance);
    }

    /**
     *  Creates a managed HTTP POST connection.
     *
     * @param requestUrl
     *      The URL to be executed. May not be null.
     *
     * @param postData
     *      The data to be sent to the server via POST. May be null.
     *
     * @param delegate
     *      The delegate (listener) class instance. May be null.
     *
     * @param shouldReturnFullResponse
     *      <code>true</code> to call the "full response" connectionDidFinishLoading delegate method --
     *      connectionDidFinishLoading(HttpResponseHeaders, byte[], String,  Object) --  which
     *      includes the HTTP headers, or <code>false</code> to call
     *      connectionDidFinishLoading(String, String, Object)
     *
     * @param userdata
     *      The tag object associated with the connection. May be null.
     */
    public static void createConnection(String requestUrl,
                                           byte[] postData,
                                           JRConnectionManagerDelegate delegate,
                                           boolean shouldReturnFullResponse,
                                           Object userdata) {
        if (postData == null) postData = new byte[0];
		JRConnectionManager instance = getInstance();
        ConnectionData cd = new ConnectionData(delegate, shouldReturnFullResponse, userdata);
        cd.setRequestUrl(requestUrl);
        cd.setPostData(postData);

		// create/store connection data
		instance.mConnectionBuffers.add(cd);

		// execute HTTP POST request
		AsyncHttpClient.executeHttpPost(cd, instance);
    }

    /* Map of managed connections, where connection data is mapped to URL. */
	//private HashMap<String, ConnectionData> mConnectionBuffers;
    private HashSet<ConnectionData> mConnectionBuffers = new HashSet<ConnectionData>();

	public void onResponseReceived(AsyncHttpResponseHolder response) {
        String requestUrl = response.getUrl();
        ConnectionData connectionData = response.getConnectionData();

        mConnectionBuffers.remove(connectionData);
        JRConnectionManagerDelegate delegate = connectionData.mDelegate;

        if (delegate == null) return;

        if (response.hasException()) {
            delegate.connectionDidFail(response.getException(), requestUrl, connectionData.mTag);
        } else {
            if (connectionData.mShouldReturnFullResponse && response.hasHeaders()) {
                // full response
                delegate.connectionDidFinishLoading(
                        response.getHeaders(),
                        response.getPayload(),
                        requestUrl,
                        connectionData.mTag);
            } else {
                // non-full response
                byte[] payload = response.getPayload();
                if (payload == null) payload = new byte[0];
                delegate.connectionDidFinishLoading(
                        new String(payload),
                        requestUrl,
                        connectionData.mTag);
            }

        }
	}
}