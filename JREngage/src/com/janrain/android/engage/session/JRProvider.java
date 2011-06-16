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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.text.AndroidCharacter;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.View;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.R;
import com.janrain.android.engage.prefs.Prefs;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.ui.ColorButton;
import com.janrain.android.engage.utils.Android;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @internal
 *
 * @class JRProvider
 **/
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
    public static final String KEY_SOCIAL_SHARING_PROPERTIES = "social_sharing_properties";
    public static final String KEY_COOKIE_DOMAINS = "cookie_domains";
    public static final String KEY_ANDROID_WEBVIEW_OPTIONS = "android_webview_options";

    private static final String TAG = JRProvider.class.getSimpleName();

    private static HashMap<String, Drawable> provider_list_icon_drawables =
            new HashMap<String, Drawable>();


    // Suppressed because this class is not expected to be serialized
    @SuppressWarnings("serial")
	private final static HashMap<String, Integer> provider_list_icon_resources =
            new HashMap<String, Integer>(){
                    {
                        put("icon_bw_facebook", R.drawable.jr_icon_bw_facebook);
                        put("icon_bw_linkedin", R.drawable.jr_icon_bw_linkedin);
                        put("icon_bw_myspace", R.drawable.jr_icon_bw_myspace);
                        put("icon_bw_twitter", R.drawable.jr_icon_bw_twitter);
                        put("icon_bw_yahoo", R.drawable.jr_icon_bw_yahoo);

                        put("icon_aol", R.drawable.jr_icon_aol);
                        put("icon_blogger", R.drawable.jr_icon_blogger);
                        put("icon_facebook", R.drawable.jr_icon_facebook);
                        put("icon_flickr", R.drawable.jr_icon_flickr);
                        //put("icon_foursquare", R.drawable.jr_icon_foursquare);
                        put("icon_google", R.drawable.jr_icon_google);
                        put("icon_hyves", R.drawable.jr_icon_hyves);
                        put("icon_linkedin", R.drawable.jr_icon_linkedin);
                        put("icon_live_id", R.drawable.jr_icon_live_id);
                        put("icon_livejournal", R.drawable.jr_icon_livejournal);
                        put("icon_myopenid", R.drawable.jr_icon_myopenid);
                        put("icon_myspace", R.drawable.jr_icon_myspace);
                        put("icon_netlog", R.drawable.jr_icon_netlog);
                        put("icon_openid", R.drawable.jr_icon_openid);
                        put("icon_orkut", R.drawable.jr_icon_orkut);
                        put("icon_paypal", R.drawable.jr_icon_paypal);
                        put("icon_salesforce", R.drawable.jr_icon_salesforce);
                        put("icon_twitter", R.drawable.jr_icon_twitter);
                        put("icon_verisign", R.drawable.jr_icon_verisign);
                        put("icon_vzn", R.drawable.jr_icon_vzn);
                        put("icon_wordpress", R.drawable.jr_icon_wordpress);
                        put("icon_yahoo", R.drawable.jr_icon_yahoo);
                    }
            };

    private static HashMap<String, Drawable> provider_logo_drawables =
            new HashMap<String, Drawable>();

    // Suppressed because this class is not expected to be serialized
    @SuppressWarnings("serial")
	private final static HashMap<String, Integer> provider_logo_resources =
            new HashMap<String, Integer>(){
                    {
                        put("logo_aol", R.drawable.jr_logo_aol);
                        put("logo_blogger", R.drawable.jr_logo_blogger);
                        put("logo_facebook", R.drawable.jr_logo_facebook);
                        put("logo_flickr", R.drawable.jr_logo_flickr);
                        put("logo_foursquare", R.drawable.jr_logo_foursquare);
                        put("logo_google", R.drawable.jr_logo_google);
                        put("logo_hyves", R.drawable.jr_logo_hyves);
                        put("logo_linkedin", R.drawable.jr_logo_linkedin);
                        put("logo_live_id", R.drawable.jr_logo_live_id);
                        put("logo_livejournal", R.drawable.jr_logo_livejournal);
                        put("logo_myopenid", R.drawable.jr_logo_myopenid);
                        put("logo_myspace", R.drawable.jr_logo_myspace);
                        put("logo_netlog", R.drawable.jr_logo_netlog);
                        put("logo_openid", R.drawable.jr_logo_openid);
                        put("logo_orkut", R.drawable.jr_logo_orkut);
                        put("logo_paypal", R.drawable.jr_logo_paypal);
                        put("logo_salesforce", R.drawable.jr_logo_salesforce);
                        put("logo_twitter", R.drawable.jr_logo_twitter);
                        put("logo_verisign", R.drawable.jr_logo_verisign);
                        put("logo_vzn", R.drawable.jr_logo_vzn);
                        put("logo_yahoo", R.drawable.jr_logo_yahoo);
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
    private List<String> mCookieDomains;
    private JRDictionary mSocialSharingProperties;
    private JRDictionary mWebViewOptions;

    private transient boolean mForceReauth;   // <- these three user parameters get preserved
    private transient String mUserInput;      // <- across cached provider reloads
    private transient String mWelcomeString;  // <-
    private transient boolean mCurrentlyDownloading;
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // We don't need a private default constructor so long as we have another constructor as we 
    // do, found immediately below.
    // Declaring this raises an irresolvable compiler warning that requires a suppression
    // annotation.
    //private JRProvider() {}

    public JRProvider(String name, JRDictionary dictionary) {
        mName = name;
        mFriendlyName = dictionary.getAsString(KEY_FRIENDLY_NAME);
        mPlaceholderText = dictionary.getAsString(KEY_INPUT_PROMPT);
        mOpenIdentifier = dictionary.getAsString(KEY_OPENID_IDENTIFIER);
        mStartAuthenticationUrl = dictionary.getAsString(KEY_URL);
        mRequiresInput = dictionary.getAsBoolean(KEY_REQUIRES_INPUT);
        mCookieDomains = dictionary.getAsListOfStrings(KEY_COOKIE_DOMAINS, true);
        mSocialSharingProperties = dictionary.getAsDictionary(KEY_SOCIAL_SHARING_PROPERTIES);
        mWebViewOptions = dictionary.getAsDictionary(KEY_ANDROID_WEBVIEW_OPTIONS, true);

//        if (mCookieDomains.size() == 0) {
//            mCookieDomains.add(mName + ".com");
//            mCookieDomains.add("www." + mName + ".com");
//        }

        loadDynamicVariables();

        if (mRequiresInput) {
            String[] arr = mPlaceholderText.split(" ");
            ArrayList<String> shortList = new ArrayList<String>();
            for (int i = (arr.length - 2); i < arr.length; i++) {
                shortList.add(arr[i]);
            }
            mShortText = TextUtils.join(" ", shortList);
        } else {
            mShortText = "";
        }

        /* We call this function in the constructor, simply to preemptively download the icons
         if they aren't already there. */
        getProviderLogo(JREngage.getContext());
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    public List<String> getCookieDomains () { /* (readonly) */
        return mCookieDomains;
    }

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

    public JRDictionary getWebViewOptions() {
        return mWebViewOptions;
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

    private Drawable getDrawable(Context c,
                                 String drawableName,
                                 HashMap<String, Drawable> drawableMap,
                                 HashMap<String, Integer> resourceMap) {
        if (drawableMap.containsKey(drawableName)) return drawableMap.get(drawableName);

        if (resourceMap.containsKey(drawableName)) {
            Drawable r = c.getResources().getDrawable(resourceMap.get(drawableName));
            drawableMap.put(drawableName, r);
            return r;
        }

        if (Android.isCupcake()) {
            // 1.5 can't handle our programmatic XHDPI resource instantiation
            return c.getResources().getDrawable(R.drawable.jr_icon_unknown);
        }


        try {
            String iconFileName = "providericon~" + drawableName + ".png";

            Bitmap icon = BitmapFactory.decodeStream(c.openFileInput(iconFileName));
            if (icon != null) {
                //Our downloaded icons are all at xhdpi, but Android 2.1 doesn't have the
                //DENSITY_XHIGH constant defined yet.  Fortunately it does the right thing
                //if you pass in the DPI as an int

                //icon.setDensity(320);
                try {
                    Method setDensity = icon.getClass().getDeclaredMethod("setDensity", int.class);
                    setDensity.invoke(icon, 320);
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "Unexpected: " + e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Unexpected: " + e);
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "Unexpected: " + e);
                }
            }
            else {
                c.deleteFile(iconFileName);
                downloadIcons(c);
                return c.getResources().getDrawable(R.drawable.jr_icon_unknown);
            }

            //return new BitmapDrawable(c.getResources(), icon);
            try {
                Class bitmapDrawableClass = Class.forName("android.graphics.drawable.BitmapDrawable");
                Constructor newBitmapDrawable =
                        bitmapDrawableClass.getDeclaredConstructor(Resources.class, Bitmap.class);
                return (BitmapDrawable) newBitmapDrawable.newInstance(c.getResources(), icon);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        catch (FileNotFoundException e) {
            downloadIcons(c);

            return c.getResources().getDrawable(R.drawable.jr_icon_unknown);
        }
    }


    public Drawable getProviderIcon(Context c) {
        return getDrawable(c,
                "icon_" + mName,
                provider_list_icon_drawables,
                provider_list_icon_resources);
    }

    public Drawable getProviderLogo(Context c) {
        return getDrawable(c, "logo_" + mName, provider_logo_drawables, provider_logo_resources);

    }

    public Drawable getTabSpecIndicatorDrawable(Context c) {
        Drawable colorIcon = getDrawable(c,
                "icon_" + mName,
                provider_list_icon_drawables,
                provider_list_icon_resources);

        Drawable bwIcon = getDrawable(c,
                "icon_bw_" + mName,
                provider_list_icon_drawables,
                provider_list_icon_resources);

        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_selected}, colorIcon);
        sld.addState(new int[]{}, bwIcon);

        return sld;
    }

    private void downloadIcons(final Context c) {
        Log.d(TAG, "downloadIcons: " + mName);

        synchronized (this) {
            if (mCurrentlyDownloading) return;
            else mCurrentlyDownloading = true;
        }

        final String[] iconFileNames = {
            "icon_" + mName + ".png",
            "icon_bw_" + mName + ".png",
            "logo_" + mName + ".png"
        };

        new AsyncTask<Void, Void, Void>(){
            public Void doInBackground(Void... s) {
                for (String iconFileName : iconFileNames) {
                    try {
                        if (Arrays.asList(c.fileList()).contains("providericon~" + iconFileName))
                            continue;

                        Log.d(TAG, "Downloading icon: " + iconFileName);
                        URL url = new URL(JRSessionData.getEnvironment().getServerUrl()
                                + "/cdn/images/mobile_icons/android/" + iconFileName);
                        InputStream is = url.openStream();
                        FileOutputStream fos = c.openFileOutput("providericon~" + iconFileName,
                                Context.MODE_PRIVATE);

                        byte buffer[] = new byte[1000];
                        int code;
                        while ((code = is.read(buffer, 0, buffer.length)) > 0) fos.write(buffer, 0, code);
                        //while (is.available() > 0) fos.write(is.read());

                        fos.close();
                    } catch (MalformedURLException e) {
                        Log.d(TAG, e.toString());
                    } catch (IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
                mCurrentlyDownloading = false;
                return null;
            }
        }.execute();
    }

    public void loadDynamicVariables() {
        if (Config.LOGD) {
            Log.d("JRProvider", "[prov] user input: [" + Prefs.KEY_JR_USER_INPUT + mName + "]");
        }

    	mUserInput = Prefs.getAsString(Prefs.KEY_JR_USER_INPUT + mName, "");
    	mWelcomeString = Prefs.getAsString(Prefs.KEY_JR_WELCOME_STRING + mName, "");
    	mForceReauth = Prefs.getAsBoolean(Prefs.KEY_JR_FORCE_REAUTH + mName, false);
    }

    public String toString() {
        return mName;
    }
}