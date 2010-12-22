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

import java.util.HashMap;

public class JRConnectionManager implements AsyncHttpResponseListener {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

	private static class ConnectionData {
		private boolean mShouldReturnFullReponse;
		private Object mTag;
		private JRConnectionManagerDelegate mDelegate;
		
		public ConnectionData(JRConnectionManagerDelegate delegate, 
				boolean shouldReturnFullResponse, Object userdata) {
			mShouldReturnFullReponse = shouldReturnFullResponse;
			mDelegate = delegate;
			mTag = userdata;
		}
	}
	
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	private static JRConnectionManager sInstance;
	
    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	public static synchronized JRConnectionManager getInstance() {
		if (sInstance == null) {
			sInstance = new JRConnectionManager();
		}
		return sInstance;
	}
	
	public static boolean createConnection(String requestUrl,
                                           JRConnectionManagerDelegate delegate,
                                           Object userdata) {
		return createConnection(requestUrl, delegate, false, userdata);
	}

	public static boolean createConnection(String requestUrl,
                                           JRConnectionManagerDelegate delegate,
                                           boolean shouldReturnFullResponse,
                                           Object userdata) {
		// get singleton instance (lazy init)
		JRConnectionManager instance = getInstance();
		
		// create/store connection data
		instance.mConnectionBuffers.put(requestUrl,
				new ConnectionData(delegate, shouldReturnFullResponse, userdata));

		// execute request
		AsyncHttpClient.executeRequest(requestUrl, instance);
		
		return true;
	}
	
	public static boolean stopConnectionsForDelegate(JRConnectionManagerDelegate delegate) {
		// get singleton instance (lazy init)
		JRConnectionManager instance = getInstance();

		for (String requestUrl : instance.mConnectionBuffers.keySet()) {
			if (instance.mConnectionBuffers.get(requestUrl).mDelegate == delegate) {
				/* TODO:  
				 * need to figure out how to stop connection - not as straightforward w/
				 * the Android/Apache HTTP classes.  For now, just remove from the hash map.
				 */
				instance.mConnectionBuffers.remove(requestUrl);
			}
		}
		return true;
	}
	
	public static int openConnections() {
		return getInstance().mConnectionBuffers.size();
	}

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

	private HashMap<String, ConnectionData> mConnectionBuffers;
	
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private JRConnectionManager() {
		mConnectionBuffers = new HashMap<String, ConnectionData>();
	}
	
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

	public void onResponseReceived(AsyncHttpResponseHolder response) {
        String requestUrl = response.getUrl();
        ConnectionData connectionData = mConnectionBuffers.get(requestUrl);
        JRConnectionManagerDelegate delegate = connectionData.mDelegate;

        if (response.hasException()) {
            delegate.connectionDidFail(response.getException(), requestUrl, connectionData.mTag);
        } else {
            if (connectionData.mShouldReturnFullReponse && response.hasHeaders()) {
                // full response
                delegate.connectionDidFinishLoading(
                        response.getHeaders(),
                        response.getPayload(),
                        requestUrl,
                        connectionData.mTag);
            } else {
                // non-full response
                delegate.connectionDidFinishLoading(
                        new String(response.getPayload()),
                        requestUrl,
                        connectionData.mTag);
            }

        }
	}

	/*
	private boolean thisIsThatStupidWindowsLiveResponse(HttpResponseHeaders headers) {
		// TODO:  need to see how to get the redirect URL (if possible) into the headers.
		return false;
	}
	*/

}
