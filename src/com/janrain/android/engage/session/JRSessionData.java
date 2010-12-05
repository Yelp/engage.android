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
package com.janrain.android.engage.session;

import java.util.ArrayList;

import org.apache.http.HttpRequest;

import android.text.TextUtils;

import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

public class JRSessionData implements JRConnectionManagerDelegate {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	private static JRSessionData sInstance;
	
    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	public static JRSessionData getInstance() {
		return sInstance;
	}
	
	public static JRSessionData getInstance(String appId, String tokenUrl, 
			JRSessionDelegate delegate) {
		
		if (sInstance != null) {
			return sInstance;
		}
		
		if (TextUtils.isEmpty(appId)) {
			return null;
		}
		
		sInstance = new JRSessionData(appId, tokenUrl, delegate);
		return sInstance;
	}
	
    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

	private ArrayList<JRSessionDelegate> mDelegates;
	
	private JRProvider mCurrentProvider;
	private String mReturningBasicProvider;
	private String mReturningSocialProvider;
	
	private JRDictionary mAllProviders;
	private ArrayList<JRProvider> mBasicProviders;
	private ArrayList<JRProvider> mSocialProviders;
	private JRDictionary mAuthenticatedUsersByProvider;
	
	private String mSavedConfigurationBlock;
	private String mNewEtag;
	
	private JRActivityObject mActivity;
	
	private String mTokenUrl;
	private String mBaseUrl;
	private String mAppId;
	
	private boolean mAlwaysForceReauth;
	private boolean mForceReauth;
	private boolean mHidePoweredBy;
	private boolean mSocial;
	
	private boolean mDialogIsShowing;
	private JREngageError mError;
	
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private JRSessionData(String appId, String tokenUrl, JRSessionDelegate delegate) {
		mDelegates = new ArrayList<JRSessionDelegate>();
		mDelegates.add(delegate);
		
		mAppId = appId;
		mTokenUrl = tokenUrl;
	}
	
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // DELEGATE METHODS
    // ------------------------------------------------------------------------

	@Override
	public void connectionDidFail(JREngageError error, HttpRequest request,
			Object userdata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionDidFinishLoading(String payload, HttpRequest request,
			Object userdata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionDidFinishLoading(HttpResponseHeaders fullResponse,
			byte[] payload, HttpRequest request, Object userdata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionWasStopped(Object userdata) {
		// TODO Auto-generated method stub
		
	}

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
}
