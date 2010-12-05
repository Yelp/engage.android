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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import android.util.Log;

/**
 * Encapsulates the various header and metadata returned when making a HTTP call. It combines data
 * available from both the HttpResponse and HttpEntity objects.
 */
public class HttpResponseHeaders {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	private static final String TAG = HttpResponseHeaders.class.getSimpleName();
	
    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	/**
	 * Creates and populates an instance of this class by retrieving the available values from
	 * the HttpResponse object (and the underlying HttpEntity).
	 * 
	 * @param response
	 * 		The HttpResponse object to gather data from.
	 * 
	 * @return 
	 * 		A valid/populated HttpResponseHeaders object.
	 */
	public static HttpResponseHeaders fromHttpResponse(HttpResponse response) {
		if (response == null) {
			Log.w(TAG, "[fromHttpResponse] null response parameter.");
			return null;
		}
		
		HttpResponseHeaders headers = new HttpResponseHeaders();
		
		StatusLine statusLine = response.getStatusLine();
		if (statusLine != null) {
			headers.mProtocolVersion = statusLine.getProtocolVersion().toString();
			headers.mStatusCode = statusLine.getStatusCode();
			headers.mReasonPhrase = statusLine.getReasonPhrase();
		}
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			Header h = entity.getContentEncoding();
			if (h != null) {
				headers.mContentEncoding = h.getValue();
			}
			h = entity.getContentType();
			if (h != null) {
				headers.mContentType = h.getValue();
			}
			headers.mContentLength = entity.getContentLength();
		}
		
		return headers;
	}
	
    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

	private String mProtocolVersion;
	private int mStatusCode;
	private String mReasonPhrase;
	private String mContentEncoding;
	private String mContentType;
	private long mContentLength;
	
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

	public String getProtocolVersion() {
		return mProtocolVersion;
	}
	
	public int getStatusCode() {
		return mStatusCode;
	}
	
	public String getReasonPhrase() {
		return mReasonPhrase;
	}
	
	public String getContentEncoding() {
		return mContentEncoding;
	}
	
	public String getContentType() {
		return mContentType;
	}
	
	public long getContentLength() {
		return mContentLength;
	}
}
