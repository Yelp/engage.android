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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Wraps the possible data returned from a basic asynchronous HTTP request.  This object will 
 * contain either a HttpResponse object (if successful) or an Exception object (if failed).
 */
public final class AsyncHttpBasicResponseHolder {

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

	private HttpUriRequest mRequest;
	private HttpResponse mResponse;
	private Exception mException;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	/**
	 * Creates "success" instance of this object.
	 * 
	 * @param request
	 * 		The HttpUriRequest object that this response corresponds to.
	 * @param response
	 * 		The HttpResponse object returned as a result of the asynchronous HTTP request.
	 */
	public AsyncHttpBasicResponseHolder(HttpUriRequest request, HttpResponse response) {
		mRequest = request;
		mResponse = response;
		mException = null;
	}
	
	/**
	 * Creates "failure" instance of this object.
	 * 
	 * @param request
	 * 		The HttpUriRequest object that this response corresponds to.
	 * @param exception
	 * 		The Exception that occurred as a result of the asynchronous HTTP request.
	 */
	public AsyncHttpBasicResponseHolder(HttpUriRequest request, Exception exception) {
		mRequest = request;
		mResponse = null;
		mException = exception;
	}
	
	// ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

	/**
	 * Gets the request object that this response corresponds to.
	 * 
	 * @return
	 * 		The HttpUriRequest object that this response corresponds to.  This will be present in
	 *		both success and failure cases.
	 */
	public HttpUriRequest getRequest() {
		return mRequest;
	}
	
	/**
	 * Gets the response object.  If the operation failed, it will return null.
	 * 
	 * @return
	 * 		The HttpResponse object returned as a result of the asynchronous HTTP request if 
	 * 		the operation was successful, null otherwise.
	 */
	public HttpResponse getResponse() {
		return mResponse;
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
	
    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
	
	/**
	 * Checks to see if the holder contains a valid (non-null) response object.
	 * 
	 * @return
	 * 		<code>true</code> if the response object is valid, <code>false</code> otherwise.
	 */
	public boolean hasResponse() {
		return (mResponse != null);
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
}
