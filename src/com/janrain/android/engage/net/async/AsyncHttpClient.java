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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
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
	 * Sends HTTP request in background and returns the response via a handler.
	 */
	private static class BasicSender extends Thread {

		private static final String TAG = BasicSender.class.getSimpleName();
		private static final DefaultHttpClient sHttpClient = new DefaultHttpClient();
		
		private HttpUriRequest mRequest;
		private Handler mHandler;
		private BasicCallbackWrapper mWrapper;
		
		public BasicSender(HttpUriRequest request, Handler handler, BasicCallbackWrapper wrapper) {
			mRequest = request;
			mHandler = handler;
			mWrapper = wrapper;
		}
		
		public void run() {
			if (Config.LOGD) { Log.d(TAG, "[run] BEGIN."); }
			try {
				final HttpResponse response;
				synchronized (sHttpClient) {
					response = sHttpClient.execute(mRequest);
				}
				mWrapper.setResponse(new AsyncHttpBasicResponseHolder(mRequest, response));
				mHandler.post(mWrapper);
				if (Config.LOGD) { Log.d(TAG, "[run] SUCCESS."); }
			} catch (ClientProtocolException e) {
				Log.e(TAG, "Problem executing request." + e.getMessage(), e);
				mWrapper.setResponse(new AsyncHttpBasicResponseHolder(mRequest, e));
			} catch (IOException e) {
				Log.e(TAG, "Problem executing request." + e.getMessage(), e);
				mWrapper.setResponse(new AsyncHttpBasicResponseHolder(mRequest, e));
			}
		}
	}
	
	/*
	 * Sends HTTP request in background, loads header and response, and returns via handler.
	 */
	private static class FullSender extends Thread {
		
		private static final String TAG = FullSender.class.getSimpleName();
		private static final DefaultHttpClient sHttpClient = new DefaultHttpClient();
		
		private HttpUriRequest mRequest;
		private Handler mHandler;
		private FullCallbackWrapper mWrapper;
		
		public FullSender(HttpUriRequest request, Handler handler, FullCallbackWrapper wrapper) {
			mRequest = request;
			mHandler = handler;
			mWrapper = wrapper;
		}
		
		public void run() {
			if (Config.LOGD) { Log.d(TAG, "[run] BEGIN."); }
			try {
				final HttpResponse response;
				synchronized (sHttpClient) {
					response = sHttpClient.execute(mRequest);
				}
				mWrapper.setResponse(new AsyncHttpFullResponseHolder(
						mRequest, 
						HttpResponseHeaders.fromHttpResponse(response),
						loadResponseData(response)));
				mHandler.post(mWrapper);
				if (Config.LOGD) { Log.d(TAG, "[run] SUCCESS."); }
			} catch (ClientProtocolException e) {
				Log.e(TAG, "Problem executing request." + e.getMessage(), e);
				mWrapper.setResponse(new AsyncHttpFullResponseHolder(mRequest, e));
			} catch (IOException e) {
				Log.e(TAG, "Problem executing request." + e.getMessage(), e);
				mWrapper.setResponse(new AsyncHttpFullResponseHolder(mRequest, e));
			}
		}
		
		/*
		 * Loads and returns binary array of data from the HttpResponse object.
		 */
		private byte[] loadResponseData(HttpResponse response) throws IOException {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			HttpEntity entity = response.getEntity();
			entity.writeTo(buffer);  // should we do this manually and control buffer size?
			return buffer.toByteArray();
		}
	}
	
	/*
	 * Sends basic response (or exception) back to the listener.
	 */
	private static class BasicCallbackWrapper implements Runnable {

		private static final String TAG = BasicCallbackWrapper.class.getSimpleName();
		
		private AsyncHttpBasicResponseListener mListener;
		private AsyncHttpBasicResponseHolder mHolder;
		
		public BasicCallbackWrapper(AsyncHttpBasicResponseListener listener) {
			mListener = listener;
		}
		
		public void run() {
			if (mListener == null) {
				Log.w(TAG, "[run] listener instance is null, no one to send response to!");
				// don't want to leave connection open if no one is listening...
				closeConnection();
			} else {
				mListener.onResponseReceived(mHolder);
				if (Config.LOGD) { Log.d(TAG, "[run] listener invoked."); }
			}
		}

		public void setResponse(AsyncHttpBasicResponseHolder holder) {
			mHolder = holder;
			Log.d(TAG, "[setResponse] response set.");
		}
		
		private void closeConnection() {
			if (mHolder != null && mHolder.hasResponse()) {
				try {
					mHolder.getResponse().getEntity().getContent().close();
					if (Config.LOGD) { Log.d(TAG, "[run] connection closed."); }
				} catch (Exception e) {
					Log.w(TAG, "Problem closing connection", e);
				}
			}
		}
	}
	
	/*
	 * Sends full response (or exception) back to the listener.
	 */
	private static class FullCallbackWrapper implements Runnable {

		private static final String TAG = FullCallbackWrapper.class.getSimpleName();
		
		private AsyncHttpFullResponseListener mListener;
		private AsyncHttpFullResponseHolder mHolder;
		
		public FullCallbackWrapper(AsyncHttpFullResponseListener listener) {
			mListener = listener;
		}
		
		public void run() {
			mListener.onFullResponseReceived(mHolder);
		}
		
		public void setResponse(AsyncHttpFullResponseHolder holder) {
			mHolder = holder;
			Log.d(TAG, "[setResponse] response set.");
		}
	}

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	private static final String TAG = AsyncHttpClient.class.getSimpleName();
	
    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	/**
	 * Executes the specified "basic" HTTP request asynchronously.  The results will be returned to 
	 * the specified listener.
	 * 
	 * A "basic" request is one in which the caller will receive a HttpResponse object and are 
	 * themselves responsible for reading the data and managing/closing the connection.
	 * 
	 * @param request
	 * 		The HTTP request to be executed asynchronously.
	 * @param listener
	 * 		The AsyncHttpBasicResponseListener to return the results to.
	 */
	public static void executeBasicRequest(final HttpUriRequest request, 
			AsyncHttpBasicResponseListener listener) {
		
		if (Config.LOGD) {
			Log.d(TAG, "[executeBasicRequest] invoked");
		}
		
		(new BasicSender(request, new Handler(), new BasicCallbackWrapper(listener))).start();
	}

	/**
	 * Executes the specified "full" HTTP request asynchronously.  The results will be returned to
	 * the specified listener.
	 * 
	 * A "full" request is on in which the caller will receive the synthesized header information 
	 * as well as a buffer (byte array) containing the entire returned payload.  The connection
	 * will be managed for them.
	 * 
	 * @param request
	 * 		The HTTP request to be executed asynchronously.
	 * @param listener
	 * 		The AsyncHttpFullResponseListener to return the results to. 
	 */
	public static void executeFullRequest(final HttpUriRequest request, 
			AsyncHttpFullResponseListener listener) {

		if (Config.LOGD) {
			Log.d(TAG, "[executeFullRequest] invoked");
		}
		
		(new FullSender(request, new Handler(), new FullCallbackWrapper(listener))).start();
	}

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private AsyncHttpClient() {
		/* no instance */
	}
	
}
