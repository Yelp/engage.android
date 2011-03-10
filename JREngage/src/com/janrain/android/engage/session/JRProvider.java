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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.R;
import com.janrain.android.engage.prefs.Prefs;
import com.janrain.android.engage.types.JRDictionary;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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

//	public static final String KEY_NAME = "name";
	public static final String KEY_FRIENDLY_NAME = "friendly_name";
//	public static final String KEY_PLACEHOLDER_TEXT = "placeholder_text";
//	public static final String KEY_SHORT_TEXT = "short_text";
	public static final String KEY_INPUT_PROMPT = "input_prompt";
	public static final String KEY_OPENID_IDENTIFIER = "openid_identifier";
	public static final String KEY_URL = "url";
	public static final String KEY_REQUIRES_INPUT = "requires_input";

    private static final String TAG = JRProvider.class.getSimpleName();

    private static HashMap<String, Drawable> tab_spec_drawables = new HashMap<String, Drawable>();

    private final static HashMap<String, Integer> tab_spec_resources = new HashMap<String, Integer>(){
        {
            //put("facebook", getResources().getDrawable(R.drawable.ic_facebook_tab));
            put("linkedin", R.drawable.ic_linkedin_tab);
            put("myspace", R.drawable.ic_myspace_tab);
            put("twitter", R.drawable.ic_twitter_tab);
            put("yahoo", R.drawable.ic_yahoo_tab);
        }
    };
    
    private static HashMap<String, Drawable> provider_list_icon_drawables = new HashMap<String, Drawable>();

    private final static HashMap<String, Integer> provider_list_icon_resources = new HashMap<String, Integer>(){
        {
            put("aol", R.drawable.icon_aol_30x30);
            put("blogger", R.drawable.icon_blogger_30x30);
//            put("facebook", R.drawable.icon_facebook_30x30);
            put("flickr", R.drawable.icon_flickr_30x30);
            put("google", R.drawable.icon_google_30x30);
            put("hyves", R.drawable.icon_hyves_30x30);
            put("linkedin", R.drawable.icon_linkedin_30x30);
            put("live_id", R.drawable.icon_live_id_30x30);
            put("livejournal", R.drawable.icon_livejournal_30x30);
            put("myopenid", R.drawable.icon_myopenid_30x30);
            put("myspace", R.drawable.icon_myspace_30x30);
            put("netlog", R.drawable.icon_netlog_30x30);
            put("openid", R.drawable.icon_openid_30x30);
            put("paypal", R.drawable.icon_paypal_30x30);
            put("twitter", R.drawable.icon_twitter_30x30);
            put("verisign", R.drawable.icon_verisign_30x30);
            put("wordpress", R.drawable.icon_wordpress_30x30);
            put("yahoo", R.drawable.icon_yahoo_30x30);
        }
    };

    private static HashMap<String, Drawable> provider_logo_drawables = new HashMap<String, Drawable>();

    private final static HashMap<String, Integer> provider_logo_resources = new HashMap<String, Integer>(){
        {
            put("aol", R.drawable.logo_aol_280x65);
            put("blogger", R.drawable.logo_blogger_280x65);
//            put("facebook", R.drawable.logo_facebook_280x65);
            put("flickr", R.drawable.logo_flickr_280x65);
            put("google", R.drawable.logo_google_280x65);
            put("hyves", R.drawable.logo_hyves_280x65);
            put("linkedin", R.drawable.logo_linkedin_280x65);
            put("live_id", R.drawable.logo_live_id_280x65);
            put("livejournal", R.drawable.logo_livejournal_280x65);
            put("myopenid", R.drawable.logo_myopenid_280x65);
            put("myspace", R.drawable.logo_myspace_280x65);
            put("netlog", R.drawable.logo_netlog_280x65);
            put("openid", R.drawable.logo_openid_280x65);
            put("paypal", R.drawable.logo_paypal_280x65);
            put("twitter", R.drawable.logo_twitter_280x65);
            put("verisign", R.drawable.logo_verisign_280x65);
            put("yahoo", R.drawable.logo_yahoo_280x65);
        }
    };


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
    private transient boolean mForceReauth;   // <- these three user parameters get preserved
    private transient String mUserInput;      // <- across cached provider reloads
    private transient String mWelcomeString;  // <-
    private JRDictionary mSocialSharingProperties;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private JRProvider() {}

    public JRProvider(String name, JRDictionary dictionary) {
        mName = name;
        mFriendlyName = dictionary.getAsString(KEY_FRIENDLY_NAME);
        mPlaceholderText = dictionary.getAsString(KEY_INPUT_PROMPT);
        mOpenIdentifier = dictionary.getAsString(KEY_OPENID_IDENTIFIER);
        mStartAuthenticationUrl = dictionary.getAsString(KEY_URL);
        mRequiresInput = dictionary.getAsBoolean(KEY_REQUIRES_INPUT);
        mSocialSharingProperties = dictionary.getAsDictionary("social_sharing_properties");

        loadDynamicVariables();

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

        //XXX shouldn't we clear the users cookie too?

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

    public Drawable getProviderListIconDrawable(Context c) {
        if (provider_list_icon_drawables.containsKey(mName)) return provider_list_icon_drawables.get(mName);

        if (provider_list_icon_resources.containsKey(mName)) {
            //that static hash is actually by friendly name not by name <- legacy artifact
            Drawable r = c.getResources().getDrawable(provider_list_icon_resources.get(mName));
            provider_list_icon_drawables.put(mName, r);
            return r;
        }

        try {
            String iconFileName = "providericon~" + "icon_" + mName + "_30x30.png";

            Bitmap icon = BitmapFactory.decodeStream(c.openFileInput(iconFileName));
            if (icon == null) c.deleteFile(iconFileName);

            return new BitmapDrawable(icon);
        } catch (FileNotFoundException e) {
            downloadIcons(c);

            return new BitmapDrawable(BitmapFactory.decodeResource(c.getResources(), R.drawable.icon_unknown));
        }
    }

    public Drawable getProviderLogo(Context c) {
        if (provider_logo_drawables.containsKey(mName)) return provider_logo_drawables.get(mName);

        if (provider_logo_resources.containsKey(mName)) {
            //that static hash is actually by friendly name not by name <- legacy artifact
            Drawable r = c.getResources().getDrawable(provider_logo_resources.get(mName));
            provider_logo_drawables.put(mName, r);
            return r;
        }

        try {
            String iconFileName = "providericon~" + "logo_" + mName + "_280x65.png";

            Bitmap icon = BitmapFactory.decodeStream(c.openFileInput(iconFileName));
            if (icon == null) c.deleteFile(iconFileName);

            return new BitmapDrawable(icon);
        } catch (FileNotFoundException e) {
            downloadIcons(c);

            return new BitmapDrawable(BitmapFactory.decodeResource(c.getResources(), R.drawable.icon_unknown));
        }
    }

    public Drawable getTabSpecIndicatorDrawable(final Context c) {
        if (tab_spec_drawables.containsKey(mName)) return tab_spec_drawables.get(mName);

        if (tab_spec_resources.containsKey(mName)) {
            Drawable r = c.getResources().getDrawable(tab_spec_resources.get(mName));
            tab_spec_drawables.put(mName, r);
            return r;
        }

        StateListDrawable sld = new StateListDrawable();
        tab_spec_drawables.put(mName, sld);
        
        try{
            String colorIconFileName = "providericon~" + "icon_" + mName + "_30x30.png";
            String bwIconFileName = "providericon~" + "icon_bw_" + mName + "_30x30.png";

            Bitmap colorIcon = BitmapFactory.decodeStream(c.openFileInput(colorIconFileName));
            if (colorIcon == null) c.deleteFile(colorIconFileName);

            Bitmap bwIcon = BitmapFactory.decodeStream(c.openFileInput(bwIconFileName));
            if (bwIcon == null) c.deleteFile(bwIconFileName);

            sld.addState(new int[]{android.R.attr.state_selected}, new BitmapDrawable(colorIcon));
            sld.addState(new int[]{}, new BitmapDrawable(bwIcon));

            return sld;
        } catch (FileNotFoundException e) {
            Drawable missingIconIcon = c.getResources().getDrawable(R.drawable.icon_unknown);
            sld.addState(new int[]{android.R.attr.state_selected}, missingIconIcon);
            sld.addState(new int[]{}, missingIconIcon);

            downloadIcons(c);

            return sld;
        }
    }

    private void downloadIcons(final Context c) {
        final String[] iconFileNames = {
            "icon_" + mName + "_30x30.png",
            "icon_" + mName + "_30x30@2x.png",
            "logo_" + mName + "_280x65.png",
            "logo_" + mName + "_280x65@2x.png",
            "icon_bw_" + mName + "_30x30.png",
            "icon_bw_" + mName + "_30x30@2x.png",
            "button_" + mName + "_135x40.png",
            "button_" + mName + "_135x40@2x.png",
            "button_" + mName + "_280x40.png",
            "button_" + mName + "_280x40@2x.png"
        };

        new AsyncTask<Void, Void, Void>(){
            public Void doInBackground(Void... s) {
                for (String iconFileName : iconFileNames) {
                    try {
                        if (Arrays.asList(c.fileList()).contains("providericon~" + iconFileName)) continue;

                        //todo fixme to use the library compile time configured baseurl
                        URL url = new URL("http://10.0.0.115:8080/cdn/images/mobile_icons/android/" + iconFileName);
                        InputStream is = url.openStream();
                        FileOutputStream fos = c.openFileOutput("providericon~" + iconFileName, Context.MODE_PRIVATE);
                        while (is.available() > 0) fos.write(is.read());
                        fos.close();
                    } catch (MalformedURLException e) {
                        Log.d(TAG, e.toString());
                    } catch (IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
                return null;
            }
        }.execute();
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

//    public boolean isEqualToReturningProvider(String returningProvider) {
//    	return ((!TextUtils.isEmpty(mName)) && (mName.equals(returningProvider)));
//    }

    public void loadDynamicVariables() {
        if (Config.LOGD) {
            Log.d("JRProvider", "[prov] user input: [" + Prefs.KEY_JR_USER_INPUT + mName + "]");
        }

    	mUserInput = Prefs.getAsString(Prefs.KEY_JR_USER_INPUT + mName, "");
    	mWelcomeString = Prefs.getAsString(Prefs.KEY_JR_WELCOME_STRING + mName, "");
    	mForceReauth = Prefs.getAsBoolean(Prefs.KEY_JR_FORCE_REAUTH + mName, false);
    }
}

