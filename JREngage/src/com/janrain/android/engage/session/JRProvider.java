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

import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.prefs.Prefs;
import com.janrain.android.engage.types.JRDictionary;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO:DOC
 */
public class JRProvider implements Serializable {
	
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	public static final String KEY_NAME = "name";
	public static final String KEY_FRIENDLY_NAME = "friendly_name";
	public static final String KEY_PLACEHOLDER_TEXT = "placeholder_text";
	public static final String KEY_SHORT_TEXT = "short_text";
	public static final String KEY_INPUT_PROMPT = "input_prompt";
	public static final String KEY_OPENID_IDENTIFIER = "openid_identifier";
	public static final String KEY_URL = "url";
	public static final String KEY_REQUIRES_INPUT = "requires_input";


    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private String mName;
    private String mFriendlyName;
    private String mPlaceholderText;
    private String mShortText;
    private boolean mRequiresInput;
    private String mOpenIdentifier;
    private String mStartAuthenticationUrl;
    private boolean mForceReauth;   // <- these three user parameters get preserved
    private String mUserInput;      // <- across cached provider reloads
    private String mWelcomeString;  // <-
    private JRDictionary mSocialSharingProperties;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public JRProvider(String name, JRDictionary dictionary) {
    	if ((!TextUtils.isEmpty(name)) && (dictionary != null)) {
    		mName = name;
    		mFriendlyName = dictionary.getAsString(KEY_FRIENDLY_NAME);
    		mPlaceholderText = dictionary.getAsString(KEY_INPUT_PROMPT);
    		mOpenIdentifier = dictionary.getAsString(KEY_OPENID_IDENTIFIER);
    		mStartAuthenticationUrl = dictionary.getAsString(KEY_URL);
    		mRequiresInput = dictionary.getAsBoolean(KEY_REQUIRES_INPUT);
    		if (mRequiresInput) {
    			String[] arr = mPlaceholderText.split(" ");
    			ArrayList<String> shortList = new ArrayList<String>();
    			for (int i = 2; i < (arr.length - 2); i++) {
    				shortList.add(arr[i]);
    			}
    			mShortText = TextUtils.join(" ", shortList);
    		} else {
    			mShortText = "";
    		}

    		loadDynamicVariables();

            mSocialSharingProperties = dictionary.getAsDictionary("social_sharing_properties");
    	}
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    public String getName() {  /* (readonly) */
        return mName;
    }

    public String getFriendlyName() {  /* (readonly) */
        return mFriendlyName;
    }

    public String getPlaceholderText() {  /* (readonly) */
        return mPlaceholderText;
    }

    public String getShortText() {  /* (readonly) */
        return mShortText;
    }

    public boolean requiresInput() {  /* (readonly) */
        return mRequiresInput;
    }

    public String getOpenIdentifier() { /* (readonly) */
        return mOpenIdentifier;
    }

    public String getStartAuthenticationUrl() { /* (readonly) */
        return mStartAuthenticationUrl;
    }

    public JRDictionary getSocialSharingProperties() { /* (readonly) */
        return mSocialSharingProperties;
    }

    public boolean getForceReauth() {
        return mForceReauth;
    }

    public void setForceReauth(boolean forceReauth) {
        this.mForceReauth = forceReauth;

        Prefs.putBoolean(Prefs.KEY_JR_FORCE_REAUTH + this.mName, this.mForceReauth);
    }

    public String getUserInput() {
        return mUserInput;
    }

    public void setUserInput(String userInput) {
        if (Config.LOGD) {
            Log.d("JRProvider", "[prov] user input: [" + Prefs.KEY_JR_USER_INPUT + mName + "]");
            }


        this.mUserInput = userInput;

        Prefs.putString(Prefs.KEY_JR_USER_INPUT + this.mName, this.mUserInput);
    }

    public String getWelcomeString() {
        return mWelcomeString;
    }

    public void setWelcomeString(String welcomeString) {
        this.mWelcomeString = welcomeString;

        Prefs.putString(Prefs.KEY_JR_WELCOME_STRING + this.mName, this.mWelcomeString);
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

//	public void initWithCoder(JRDictionary coder) {
//		mName = coder.getAsString(KEY_NAME);
//		mFriendlyName = coder.getAsString(KEY_FRIENDLY_NAME);
//		mPlaceholderText = coder.getAsString(KEY_PLACEHOLDER_TEXT);
//		mShortText = coder.getAsString(KEY_SHORT_TEXT);
//		mOpenIdentifier = coder.getAsString(KEY_OPENID_IDENTIFIER);
//		mStartAuthenticationUrl = coder.getAsString(KEY_URL);
//		mRequiresInput = coder.getAsBoolean(KEY_REQUIRES_INPUT);
//
//        this.loadDynamicVariables();
//    }
//
//    public void encodeWithCoder(JRDictionary coder) {
//		coder.put(KEY_NAME, mName);
//		coder.put(KEY_FRIENDLY_NAME, mFriendlyName);
//		coder.put(KEY_PLACEHOLDER_TEXT, mPlaceholderText);
//		coder.put(KEY_SHORT_TEXT, mShortText);
//		coder.put(KEY_OPENID_IDENTIFIER, mOpenIdentifier);
//		coder.put(KEY_URL, mStartAuthenticationUrl);
//		coder.put(KEY_REQUIRES_INPUT, mRequiresInput);
//    }
//
//    public boolean isEqualToProvider(JRProvider provider) {
//    	boolean isEqual = false;
//    	if (provider != null) {
//    		isEqual = ((!TextUtils.isEmpty(mName)) && (mName.equals(provider.getName())));
//    	}
//    	return isEqual;
//    }

    public boolean isEqualToReturningProvider(String returningProvider) {
    	return ((!TextUtils.isEmpty(mName)) && (mName.equals(returningProvider)));
    }

    public void loadDynamicVariables() {
        if (Config.LOGD) {
            Log.d("JRProvider", "[prov] user input: [" + Prefs.KEY_JR_USER_INPUT + mName + "]");
        }

    	mUserInput = Prefs.getAsString(Prefs.KEY_JR_USER_INPUT + mName, "");
    	mWelcomeString = Prefs.getAsString(Prefs.KEY_JR_WELCOME_STRING + mName, "");
    	mForceReauth = Prefs.getAsBoolean(Prefs.KEY_JR_FORCE_REAUTH + mName, false);
    }

}

