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

import android.content.Context;
import android.graphics.Bitmap;
import com.janrain.android.engage.types.JRDictionary;

import android.text.TextUtils;

import java.awt.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * TODO:DOC
 */
public class JRAuthenticatedUser implements Serializable {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    public static final String KEY_AUTH_INFO = "auth_info";
	public static final String KEY_DEVICE_TOKEN = "device_token";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_PREFERRED_USERNAME = "preferred_username";
    //TODO remove authinfo field
    //TODO use keychain or something to store device token

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private String mPhoto;
    private String mPreferredUsername;
    private String mDeviceToken;
    private JRDictionary mAuthInfo;
    private String mProviderName;
    transient private Bitmap mCachedProfilePic;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private JRAuthenticatedUser() {
    }
    
    public JRAuthenticatedUser(JRDictionary mobileEndPointResponse, String providerName) {
        mProviderName = providerName;
        mDeviceToken = mobileEndPointResponse.getAsString(KEY_DEVICE_TOKEN);
        mPhoto = mobileEndPointResponse.getAsString(KEY_PHOTO);
        mPreferredUsername = mobileEndPointResponse.getAsString(KEY_PREFERRED_USERNAME);
        mAuthInfo = mobileEndPointResponse.getAsDictionary(KEY_AUTH_INFO);
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    public String getPhoto() {              /* (readonly) */
        return mPhoto;
    }

    public String getPreferredUsername() {  /* (readonly) */
        return mPreferredUsername;
    }

    public String getDeviceToken() {        /* (readonly) */
        return mDeviceToken;
    }

//    public JRDictionary getAuthInfo() {     /* (readonly) */
//        return mAuthInfo;
//    }

    public String getProviderName() {       /* (readonly) */
        return mProviderName;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

//    public void encodeWithCoder(JRDictionary coder) {
//    	coder.put(KEY_PROVIDER_NAME, mProviderName);
//    	coder.put(KEY_PHOTO, mPhoto);
//    	coder.put(KEY_PREFERRED_USERNAME, mPreferredUsername);
//    	coder.put(KEY_DEVICE_TOKEN, mDeviceToken);
//        coder.put(KEY_AUTH_INFO, mAuthInfo);
//    }
//
//    public void initWithCoder(JRDictionary coder) {
//    	mProviderName = coder.getAsString(KEY_PROVIDER_NAME);
//    	mPhoto = coder.getAsString(KEY_PHOTO);
//    	mPreferredUsername = coder.getAsString(KEY_PREFERRED_USERNAME);
//    	mDeviceToken = coder.getAsString(KEY_DEVICE_TOKEN);
//        mAuthInfo = coder.getAsDictionary(KEY_AUTH_INFO);
//    }

//    public Bitmap getCachedProfilePic() {
//        return mCachedProfilePic;  //To change body of created methods use File | Settings | File Templates.
//        if (mCachedProfilePic != null) return mCachedProfilePic;
//        else {
//            if (
//        }
//    }

//    public void setCachedProfilePic(Bitmap b) {
//        //todo implement the caching
//        mCachedProfilePic = b;
//    }

    public String getCachedProfilePicKey() {
        try {
            return URLEncoder.encode(mPhoto, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
