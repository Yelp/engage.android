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

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.JREngageError.AuthenticationError;
import com.janrain.android.engage.JREngageError.ConfigurationError;
import com.janrain.android.engage.JREngageError.ErrorType;
import com.janrain.android.engage.JREngageError.SocialPublishingError;
import com.janrain.android.engage.JREnvironment;
import com.janrain.android.engage.R;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.prefs.Prefs;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.AndroidUtils;
import com.janrain.android.engage.utils.Archiver;
import com.janrain.android.engage.utils.ListUtils;
import com.janrain.android.engage.utils.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JRSessionData implements JRConnectionManagerDelegate {
    private static final String TAG = JRSessionData.class.getSimpleName();

    private static final JREnvironment ENVIRONMENT = JREnvironment.PRODUCTION;
    //private static final JREnvironment ENVIRONMENT = JREnvironment.STAGING;
    //private static final JREnvironment ENVIRONMENT = JREnvironment.LILLI;
    //private static final JREnvironment ENVIRONMENT = JREnvironment.NATHAN;

    private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
    private static final String ARCHIVE_BASIC_PROVIDERS = "basicProviders";
    private static final String ARCHIVE_SOCIAL_PROVIDERS = "socialProviders";
    private static final String ARCHIVE_AUTH_USERS_BY_PROVIDER = "jrAuthenticatedUsersByProvider";

    private static final String FMT_CONFIG_URL =
            "%s/openid/mobile_config_and_baseurl?appId=%s&device=android&app_name=%s&version=%s";
    private static final String TAG_GET_CONFIGURATION = "getConfiguration";

    private static JRSessionData sInstance;
    public static final String USERDATA_ACTION_KEY = "action";
    public static final String USERDATA_ACTION_CALL_TOKEN_URL = "callTokenUrl";
    public static final String USERDATA_TOKEN_URL_KEY = "tokenUrl";
    public static final String USERDATA_PROVIDER_NAME_KEY = "providerName";
    public static final String USERDATA_ACTIVITY_KEY = "activity";
    public static final String USERDATA_ACTION_SHARE_ACTIVITY = "shareActivity";

    private ArrayList<JRSessionDelegate> mDelegates;

	private JRProvider mCurrentlyAuthenticatingProvider;
    private JRProvider mCurrentlyPublishingProvider;

    private String mReturningBasicProvider;
	private String mReturningSocialProvider;

	private JRDictionary mAllProviders;
	private ArrayList<String> mBasicProviders;
	private ArrayList<String> mSocialProviders;
	private JRDictionary mAuthenticatedUsersByProvider;

	private String mSavedConfigurationBlock = "";
    private String mSavedEtag;
	private String mNewEtag;

	private JRActivityObject mActivity;

	private String mTokenUrl;
	private String mBaseUrl;
	private String mAppId;

    private boolean mGetConfigDone = false;
    private String mOldEtag;
    private String mUrlEncodedAppName;
    private String mLibraryVersion;

    private boolean mHidePoweredBy;
	private boolean mAlwaysForceReauth;
    private boolean mSkipLandingPage;
    private int mUiShowingCount = 0;

    private JREngageError mError;

    public static JRSessionData getInstance() {
        return sInstance;
	}

	public static JRSessionData getInstance(String appId,
                                            String tokenUrl,
                                            JRSessionDelegate delegate) {
        if (sInstance != null) {
            if (Config.LOGD) Log.w(TAG, "[getInstance] reinitializing existing instance");
            //sInstance.initialize(appId, tokenUrl, delegate);
        } else {
            if (Config.LOGD) Log.d(TAG, "[getInstance] returning new instance.");
            sInstance = new JRSessionData(appId, tokenUrl, delegate);
        }

		return sInstance;
	}

	private JRSessionData(String appId, String tokenUrl, JRSessionDelegate delegate) {
        initialize(appId, tokenUrl, delegate);
    }

    /* We runtime type check the deserialized generics so we can safely ignore these unchecked
     * assignment warnings. */
    @SuppressWarnings("unchecked")
    private void initialize(String appId, String tokenUrl, JRSessionDelegate delegate) {
        if (Config.LOGD) Log.d(TAG, "[ctor] initializing instance.");

		mDelegates = new ArrayList<JRSessionDelegate>();
		mDelegates.add(delegate);

		mAppId = appId;
		mTokenUrl = tokenUrl;

        ApplicationInfo ai = AndroidUtils.getApplicationInfo();
        String appName = JREngage.getContext().getPackageManager().getApplicationLabel(ai).toString();
        mUrlEncodedAppName = AndroidUtils.urlEncode(appName);

        mLibraryVersion = JREngage.getContext().getString(R.string.jr_engage_version);
        String diskVersion = Prefs.getAsString(Prefs.KEY_JR_ENGAGE_LIBRARY_VERSION, "");
        if (diskVersion.equals(mLibraryVersion)
                ) {
                //&& false) {  // todo
            /* Load dictionary of authenticated users.  If the dictionary is not found, the
             * archiver will return a new (empty) list.  This will throw and catch an exception if it isn't
             * found, so if it's empty, we save the empty dictionary to stop this exception in the future. */
            mAuthenticatedUsersByProvider = JRDictionary.unarchive(ARCHIVE_AUTH_USERS_BY_PROVIDER);
            if (mAuthenticatedUsersByProvider.isEmpty())
                JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);

            /* Load the list of all providers */
            mAllProviders = JRDictionary.unarchive(ARCHIVE_ALL_PROVIDERS);

            for (Object provider : mAllProviders.values()) ((JRProvider)provider).loadDynamicVariables();

            /* Load the list of basic providers */
            mBasicProviders = (ArrayList<String>)Archiver.load(ARCHIVE_BASIC_PROVIDERS);
            for (Object v : mBasicProviders) assert v instanceof String;
            if (Config.LOGD) {
                if (ListUtils.isEmpty(mBasicProviders)) {
                    if (Config.LOGD) Log.d(TAG, "[ctor] basic providers is empty");
                } else {
                    if (Config.LOGD) {
                        Log.d(TAG, "[ctor] basic providers: [" + TextUtils.join(",", mBasicProviders) + "]");
                    }
                }
            }

            /* Load the list of social providers */
            mSocialProviders = (ArrayList<String>)Archiver.load(ARCHIVE_SOCIAL_PROVIDERS);
            for (Object v : mSocialProviders) assert v instanceof String;
            if (Config.LOGD) {
                if (ListUtils.isEmpty(mSocialProviders)) {
                    if (Config.LOGD) Log.d(TAG, "[ctor] social providers is empty");
                } else {
                    if (Config.LOGD) {
                        Log.d(TAG, "[ctor] social providers: [" + TextUtils.join(",", mSocialProviders)
                                + "]");
                    }
                }
            }

            /* Load the base url */
            mBaseUrl = Prefs.getAsString(Prefs.KEY_JR_BASE_URL, "");

            /* Figure out of we're suppose to hide the powered by line */
            mHidePoweredBy = Prefs.getAsBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, false);

            /* And load the last used basic and social providers */
            mReturningSocialProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, "");
            mReturningBasicProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, "");

            /* If the configuration for this rp has changed, the etag will have changed, and we need
             * to update our current configuration information. */
            mOldEtag = Prefs.getAsString(Prefs.KEY_JR_CONFIGURATION_ETAG, "");
        } else {
            mAuthenticatedUsersByProvider = new JRDictionary();
            mAllProviders = new JRDictionary();
            mBasicProviders = new ArrayList<String>();
            mSocialProviders = new ArrayList<String>();
            mBaseUrl = "";
            mHidePoweredBy = true;
            mReturningSocialProvider = "";
            mReturningBasicProvider = "";
            mOldEtag = "";
        }

        mError = startGetConfiguration();
	}

    public JREngageError getError() {
        return mError;
    }

    public JRActivityObject getJRActivity() {
        return mActivity;
    }

    public void setJRActivity(JRActivityObject activity) {
        mActivity = activity;
    }

    public void setSkipLandingPage(boolean skipLandingPage) {
        mSkipLandingPage = skipLandingPage;
    }

    public boolean getAlwaysForceReauth() {
        return mAlwaysForceReauth;
    }

    public void setAlwaysForceReauth(boolean force) {
        mAlwaysForceReauth = force;
    }

    public String getTokenUrl() {
        return mTokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        mTokenUrl = tokenUrl;
    }

    public JRProvider getCurrentlyAuthenticatingProvider() {
        return mCurrentlyAuthenticatingProvider;
    }

    public void setCurrentlyAuthenticatingProvider(JRProvider provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[setCurrentlyAuthenticatingProvider] to " +
                    (provider != null ? provider.getName() : null));
        }

        mCurrentlyAuthenticatingProvider = provider;
    }

    public ArrayList<JRProvider> getBasicProviders() {
        ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
        if ((mBasicProviders != null) && (mBasicProviders.size() > 0)) {
            for (String name : mBasicProviders) {
                JRProvider provider = mAllProviders.getAsProvider(name);
                providerList.add(provider);
            }
        }
        return providerList;

        // Empty return for testing:
        //return new ArrayList<JRProvider>();
    }

    public ArrayList<JRProvider> getSocialProviders() {
        ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();

        if ((mSocialProviders != null) && (mSocialProviders.size() > 0)) {
            for (String name : mSocialProviders)
                providerList.add(mAllProviders.getAsProvider(name));
        }

        return providerList;
    }

    public String getReturningBasicProvider() {
        /* This is here so that when a calling application sets mSkipLandingPage, the dialog always opens
         * to the providers list. (See JRProviderListFragment.onCreate for an explanation of the flow control
         * when there's a "returning provider.") */
        if (mSkipLandingPage)
            return null;

        return mReturningBasicProvider;
    }

    public void setReturningBasicProvider(String returningBasicProvider) {
        if (TextUtils.isEmpty(returningBasicProvider)) returningBasicProvider = ""; // nulls -> ""s
        if (!mBasicProviders.contains(returningBasicProvider)) returningBasicProvider = "";

        mReturningBasicProvider = returningBasicProvider;
        Prefs.putString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, returningBasicProvider);
    }

    public String getReturningSocialProvider() {
        return mReturningSocialProvider;
    }

    public void setReturningSocialProvider(String returningSocialProvider) {
        if (TextUtils.isEmpty(returningSocialProvider)) returningSocialProvider = ""; // nulls -> ""s
        if (!mSocialProviders.contains(returningSocialProvider)) returningSocialProvider = "";

        mReturningSocialProvider = returningSocialProvider;
        Prefs.putString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, returningSocialProvider);
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public boolean getHidePoweredBy() {
        return mHidePoweredBy;
    }

    public void setUiIsShowing(boolean uiIsShowing) {
        if (uiIsShowing) {
            mUiShowingCount++;
        } else {
            mUiShowingCount--;
        }

        if (mUiShowingCount == 0 && !mSavedConfigurationBlock.equals("")) {
            String s = mSavedConfigurationBlock;
            mSavedConfigurationBlock = "";
            mNewEtag = mSavedEtag;
            mError = finishGetConfiguration(s);
        }
    }

    public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
        Log.e(TAG, "[connectionDidFail]");

        if (userdata == null) {
            Log.e(TAG, "[connectionDidFail] unexpected null userdata");
        } else if (userdata instanceof String) {
            if (userdata.equals(TAG_GET_CONFIGURATION)) {
                Log.e(TAG, "[connectionDidFail] for getConfiguration");
                mError = new JREngageError(
                        "There was a problem communicating with the Janrain server while configuring authentication.",
                        ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                        JREngageError.ErrorType.CONFIGURATION_FAILED);
                mGetConfigDone = true;
                triggerMobileConfigDidFinish();
            } else {
                Log.e(TAG, "[connectionDidFail] unrecognized ConnectionManager tag: "
                        + userdata + " with Exception: " + ex);
            }
        } else if (userdata instanceof JRDictionary) {
            JRDictionary dictionary = (JRDictionary) userdata;
            if (dictionary.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_CALL_TOKEN_URL)) {
                Log.e(TAG, "[connectionDidFail] call to token url failed: " + ex);
                JREngageError error = new JREngageError(
                        "Session error",
                        JREngageError.CODE_UNKNOWN,
                        "",
                        ex);
                for (JRSessionDelegate delegate : getDelegatesCopy()) {
                    delegate.authenticationCallToTokenUrlDidFail(
                            dictionary.getAsString(USERDATA_TOKEN_URL_KEY),
                            error,
                            dictionary.getAsString(USERDATA_PROVIDER_NAME_KEY));
                }
            } else if (dictionary.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_SHARE_ACTIVITY)) {
                Log.e(TAG, "[connectionDidFail] sharing activity failed: " + ex);
                List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                JREngageError error = new JREngageError(
                        "Session error",
                        JREngageError.CODE_UNKNOWN,
                        "",
                        ex);
                for (JRSessionDelegate delegate : delegatesCopy) {
                    delegate.publishingJRActivityDidFail(
                            (JRActivityObject) dictionary.get(USERDATA_ACTIVITY_KEY),
                            error,
                            dictionary.getAsString(USERDATA_PROVIDER_NAME_KEY));
                }
            }
        }
	}

    public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
        if (Config.LOGD) Log.d(TAG, "[connectionDidFinishLoading] payload: " + payload);

        if (userdata instanceof String) {
            Log.e(TAG, "[connectionDidFinishLoading] unrecognized ConnectionManager tag: " + userdata);
        } else if (userdata instanceof JRDictionary) {
            JRDictionary dictionary = (JRDictionary) userdata;

            if (dictionary.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_SHARE_ACTIVITY))
                processShareActivityResponse(payload, dictionary);
            else {
                Log.e(TAG, "[connectionDidFinishLoading] unexpected JRDictionary: " + userdata);
            }
        } else if (userdata == null) {
            /* Unrecognized response */
            Log.e(TAG, "[connectionDidFinishLoading] unexpected null userdata");
        }
	}

    private void processShareActivityResponse(String payload, JRDictionary userDataTag) {
        String providerName = userDataTag.getAsString(USERDATA_PROVIDER_NAME_KEY);

        JRDictionary responseDict = JRDictionary.fromJSON(payload);

        if (responseDict == null) {
            setCurrentlyPublishingProvider(null);
            for (JRSessionDelegate delegate : getDelegatesCopy()) {
                delegate.publishingJRActivityDidFail(
                        (JRActivityObject) userDataTag.get(USERDATA_ACTIVITY_KEY),
                        new JREngageError(payload,
                                SocialPublishingError.FAILED,
                                ErrorType.PUBLISH_FAILED),
                                providerName);
            }

        } else if (responseDict.containsKey("stat") && ("ok".equals(responseDict.get("stat")))) {
            setReturningSocialProvider(getCurrentlyPublishingProvider().getName());
            setCurrentlyPublishingProvider(null);
            for (JRSessionDelegate delegate : getDelegatesCopy()) {
                delegate.publishingJRActivityDidSucceed(mActivity, providerName);
            }
        } else {
            setCurrentlyPublishingProvider(null);
            JRDictionary errorDict = responseDict.getAsDictionary("err");
            JREngageError publishError;

            if (errorDict == null) {
                publishError = new JREngageError(
                        "There was a problem publishing with this activity",
                        SocialPublishingError.FAILED,
                        ErrorType.PUBLISH_FAILED);
            } else {
                int code = (errorDict.containsKey("code"))
                        ? errorDict.getAsInt("code") : 1000;

                String msg = errorDict.getAsString("msg", "");

                switch (code) {
                    case 0: /* "Missing parameter: apiKey" */
                        publishError = new JREngageError(
                                msg,
                                SocialPublishingError.MISSING_API_KEY,
                                ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                        break;
                    case 4: /* "Facebook Error: Invalid OAuth 2.0 Access Token" */
                        if (msg.matches(".*nvalid ..uth.*")) {
                            publishError = new JREngageError(
                                    msg,
                                    SocialPublishingError.INVALID_OAUTH_TOKEN,
                                    ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                        } else if (msg.matches(".*eed action request limit.*")) {
                            publishError = new JREngageError(
                                    msg,
                                    SocialPublishingError.FEED_ACTION_REQUEST_LIMIT,
                                    ErrorType.PUBLISH_FAILED);
                        } else {
                            publishError = new JREngageError(
                                    msg,
                                    SocialPublishingError.FAILED,
                                    ErrorType.PUBLISH_FAILED);
                        }
                        break;
                    case 100: // TODO LinkedIn character limit error
                        publishError = new JREngageError(
                                msg,
                                SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED,
                                ErrorType.PUBLISH_INVALID_ACTIVITY);
                        break;
                    case 6:
                        if (msg.matches(".*witter.*")) {
                            publishError = new JREngageError(
                                    msg,
                                    SocialPublishingError.DUPLICATE_TWITTER,
                                    ErrorType.PUBLISH_INVALID_ACTIVITY);
                        } else {
                            publishError = new JREngageError(
                                    msg,
                                    JREngageError.SocialPublishingError.FAILED,
                                    ErrorType.PUBLISH_INVALID_ACTIVITY);
                        }
                        break;
                    case 1000: /* Extracting code failed; Fall through. */
                    default: // TODO Other errors (find them)
                        publishError = new JREngageError(
                                "There was a problem publishing this activity",
                                SocialPublishingError.FAILED,
                                ErrorType.PUBLISH_FAILED);
                        break;
                }
            }

            triggerPublishingJRActivityDidFail(publishError,
                    (JRActivityObject) userDataTag.get(USERDATA_ACTIVITY_KEY),
                    providerName);
        }
    }

    public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload,
                                           String requestUrl, Object userdata) {
        Log.i(TAG, "[connectionDidFinishLoading-full]");

        if (userdata instanceof JRDictionary) {
            JRDictionary dictionary = (JRDictionary) userdata;
            if (dictionary.containsKey(USERDATA_TOKEN_URL_KEY)) {
                for (JRSessionDelegate delegate : getDelegatesCopy()) {
                    delegate.authenticationDidReachTokenUrl(
                            dictionary.getAsString(USERDATA_TOKEN_URL_KEY),
                            headers,
                            new String(payload),
                            dictionary.getAsString(USERDATA_PROVIDER_NAME_KEY));
                }
            } else Log.e(TAG, "unexpected userdata found in ConnectionDidFinishLoading full");
        } else if (userdata instanceof String) {
            String s = (String)userdata;

            if (s.equals("getConfiguration")) {
                if (headers.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    /* If the ETag matched, we're done. */
                    if (Config.LOGD) {
                        Log.d(TAG, "[connectionDidFinishLoading] found HTTP_NOT_MODIFIED -> matched ETag");
                    }

                    mGetConfigDone = true;
                    return;
                }

                String payloadString = new String(payload);
                if (Config.LOGD) {
                    Log.d(TAG, "[connectionDidFinishLoading-full] payload string: " + payloadString);
                }

                if (payloadString.contains("\"provider_info\":{")) {
                    mError = finishGetConfiguration(payloadString, headers.getETag());
                } else {
                    Log.e(TAG, "failed to parse response for getConfiguration");
                    mError = new JREngageError(
                            "There was a problem communicating with the Janrain server while configuring authentication.",
                            ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                            ErrorType.CONFIGURATION_FAILED);
                }
            } else {
                Log.e(TAG, "unexpected userdata found in ConnectionDidFinishLoading full");
            }
        }
	}

    public void addDelegate(JRSessionDelegate delegate) {
        if (Config.LOGD) Log.d(TAG, "[addDelegate]");

        mDelegates.add(delegate);
    }

    public void removeDelegate(JRSessionDelegate delegate) {
        if (Config.LOGD) Log.d(TAG, "[removeDelegate]");

        mDelegates.remove(delegate);
    }

    public void tryToReconfigureLibrary() {
        if (Config.LOGD) Log.d(TAG, "[tryToReconfigureLibrary]");

        mError = null;
        mError = startGetConfiguration();
    }

    private JREngageError startGetConfiguration() {
        String urlString = String.format(FMT_CONFIG_URL,
                ENVIRONMENT.getServerUrl(),
                mAppId,
                mUrlEncodedAppName,
                mLibraryVersion);
        if (Config.LOGD) Log.d(TAG, "[startGetConfiguration] url: " + urlString);

        BasicNameValuePair etagHeader = new BasicNameValuePair("If-None-Match", mOldEtag);
        List<NameValuePair> headerList = new ArrayList<NameValuePair>();
        headerList.add(etagHeader);

        String tag = TAG_GET_CONFIGURATION;
        if (!JRConnectionManager.createConnection(urlString, this, true, headerList, tag)) {
            Log.w(TAG, "[startGetConfiguration] createConnection failed.");
            return new JREngageError(
                    "There was a problem connecting to the Janrain server while configuring authentication.",
                    ConfigurationError.URL_ERROR,
                    JREngageError.ErrorType.CONFIGURATION_FAILED);
        }

        return null;
    }

    private JREngageError finishGetConfiguration(String dataStr) {
        if (Config.LOGD) Log.d(TAG, "[finishGetConfiguration]");

        /* Attempt to parse the return string as JSON.*/
        JRDictionary jsonDict = null;
        Exception jsonEx = null;
        try {
            jsonDict = JRDictionary.fromJSON(dataStr);
        } catch (Exception e) {
            Log.e(TAG, "[finishGetConfiguration] json error: ", e);
            jsonEx = e;
        }

        /* If the parsed dictionary object is null or an exception has occurred, return */
        if ((jsonDict == null) || (jsonEx != null)) {
            Log.e(TAG, "[finishGetConfiguration] failed.");
            return new JREngageError(
                    "There was a problem communicating with the Janrain server while configuring authentication.",
                    ConfigurationError.JSON_ERROR,
                    ErrorType.CONFIGURATION_FAILED,
                    jsonEx);
        }

        /* Check to see if the base URL has changed */
        String baseUrl = jsonDict.getAsString("baseurl", "");
        if (!baseUrl.equals(mBaseUrl)) {
            /* Save the new base url */
            mBaseUrl = StringUtils.chomp(baseUrl, "/");
            Prefs.putString(Prefs.KEY_JR_BASE_URL, mBaseUrl);
        }

        /* Get the providers out of the provider_info section.  These are likely to have changed. */
        JRDictionary providerInfo = jsonDict.getAsDictionary("provider_info");
        mAllProviders = new JRDictionary();

        /* For each provider */
        for (String name : providerInfo.keySet()) {
            /* Get its dictionary */
            JRDictionary dictionary = providerInfo.getAsDictionary(name);
            /* Use this to create a provider object */
            JRProvider provider = new JRProvider(name, dictionary);
            /* and finally add the object to our dictionary of providers */
            mAllProviders.put(name, provider);
        }

        /* Get the ordered list of basic providers */
        mBasicProviders = jsonDict.getAsListOfStrings("enabled_providers");
        /* Get the ordered list of social providers */
        mSocialProviders = jsonDict.getAsListOfStrings("social_providers");

        /* By redundantly calling these setters it is ensured that the returning basic and social providers,
         * if set, are members of the configured set of providers. */
        setReturningBasicProvider(mReturningBasicProvider);
        setReturningSocialProvider(mReturningSocialProvider);

        /* Done! */

        /* Save data to local store */
        JRDictionary.archive(ARCHIVE_ALL_PROVIDERS, mAllProviders);
        Archiver.save(ARCHIVE_BASIC_PROVIDERS, mBasicProviders);
        Archiver.save(ARCHIVE_SOCIAL_PROVIDERS, mSocialProviders);

        /* Figure out of whether to hide the "powered by" line */
        mHidePoweredBy = jsonDict.getAsBoolean("hide_tagline", false);
        Prefs.putBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, mHidePoweredBy);

        /* Once we know everything is parsed and saved, save the new etag */
        Prefs.putString(Prefs.KEY_JR_CONFIGURATION_ETAG, mNewEtag);

        /* 'git-tag'-like library version tag to prevent reloading stale data from disk */
        Prefs.putString(Prefs.KEY_JR_ENGAGE_LIBRARY_VERSION, mLibraryVersion);

        mGetConfigDone = true;
        triggerMobileConfigDidFinish();

        return null;
    }

    private JREngageError finishGetConfiguration(String dataStr, String etag) {
        if (Config.LOGD) Log.d(TAG, "[finishGetConfiguration-etag]");

        if (!mOldEtag.equals(etag)) {

            /* We can only update all of our data if the UI isn't currently using that
             * information.  Otherwise, the library may crash/behave inconsistently.  If a
             * dialog isn't showing, go ahead and update that information.  Or, in the case
             * where a dialog is showing but there isn't any data that it could be using (that
             * is, the lists of basic and social providers are nil), go ahead and update it too.
             * The dialogs won't try and do anything until we're done updating the lists. */
            if (!isUiShowing() ||
                    (ListUtils.isEmpty(mBasicProviders) && ListUtils.isEmpty(mSocialProviders))) {
                mNewEtag = etag;
                return finishGetConfiguration(dataStr);
            }

            /* Otherwise, we have to save all this information for later.  The
             * UserInterfaceMaestro sends a signal to sessionData when the dialog closes (by
             * setting the boolean dialogIsShowing to false. In the setter function, sessionData
             * checks to see if there's anything stored in the savedConfigurationBlock, and
             * updates it then. */
            mSavedConfigurationBlock = dataStr;
            mSavedEtag = etag;
        }

        mGetConfigDone = true;
        triggerMobileConfigDidFinish();

        return null;
    }

    private boolean isUiShowing() {
        return mUiShowingCount == 0;
    }

    private String getWelcomeMessageFromCookieString() {
        if (Config.LOGD) Log.d(TAG, "[getWelcomeMessageFromCookieString]");

        String cookies = CookieManager.getInstance().getCookie(getBaseUrl());
        String cookieString = cookies.replaceAll(".*welcome_info=([^;]*).*", "$1");

        if (!TextUtils.isEmpty(cookieString)) {
            String[] parts = cookieString.split("%22");
            if (parts.length > 5) return "Sign in as " + AndroidUtils.urlDecode(parts[5]);
        }

        return null;
    }

    public void saveLastUsedBasicProvider() {
        if (Config.LOGD) Log.d(TAG, "[saveLastUsedBasicProvider]");

        setReturningBasicProvider(mCurrentlyAuthenticatingProvider.getName());
    }

    public URL startUrlForCurrentlyAuthenticatingProvider() {
        if (Config.LOGD) Log.d(TAG, "[startUrlForCurrentlyAuthenticatingProvider]");

        // What's this for?
        if (mCurrentlyAuthenticatingProvider == null) return null;

        String oid; /* open identifier */

        if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getOpenIdentifier())) {
            oid = String.format("openid_identifier=%s&", mCurrentlyAuthenticatingProvider.getOpenIdentifier());
            if (mCurrentlyAuthenticatingProvider.requiresInput()) {
                oid = oid.replaceAll("%@", mCurrentlyAuthenticatingProvider.getUserInput());
            } else {
                oid = oid.replaceAll("%@", "");
            }
        } else {
            oid = "";
        }

        String fullStartUrl;

        boolean forceReauth = mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth();
        if (getAuthenticatedUserForProvider(mCurrentlyAuthenticatingProvider) == null) forceReauth = true;
        if (forceReauth) {
            deleteWebViewCookiesForDomains(mCurrentlyAuthenticatingProvider.getCookieDomains());
        }

        fullStartUrl = String.format("%s%s?%s%sdevice=android&extended=true",
                mBaseUrl,
                mCurrentlyAuthenticatingProvider.getStartAuthenticationUrl(),
                oid,
                (forceReauth ? "force_reauth=true&" : "")
        );

        if (Config.LOGD) {
            Log.d(TAG, "[startUrlForCurrentlyAuthenticatingProvider] startUrl: " + fullStartUrl);
        }

        URL url = null;
        try {
            url = new URL(fullStartUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "[startUrlForCurrentlyAuthenticatingProvider] URL create failed for string: "
                    + fullStartUrl);
        }
        return url;
    }

    public JRAuthenticatedUser getAuthenticatedUserForProvider(JRProvider provider) {
        if (Config.LOGD) Log.d(TAG, "[getAuthenticatedUserForProvider]");

        return (JRAuthenticatedUser) mAuthenticatedUsersByProvider.get(provider.getName());
    }

    public void forgetAuthenticatedUserForProvider(String providerName) {
        if (Config.LOGD) Log.d(TAG, "[forgetAuthenticatedUserForProvider]");

        JRProvider provider = mAllProviders.getAsProvider(providerName);
        if (provider == null) {
            Log.e(TAG, "[forgetAuthenticatedUserForProvider] provider not found: " + providerName);
        } else {
            provider.setForceReauth(true);
            mAuthenticatedUsersByProvider.remove(provider.getName());

            JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
        }
    }

    private void deleteWebViewCookiesForDomains(Collection<String> domains) {
        for (String d : domains) {
            deleteWebViewCookiesForDomain(d, false);
            deleteWebViewCookiesForDomain(d, true);
        }
    }

    private void deleteWebViewCookiesForDomain(String domain, boolean secure) {
        CookieSyncManager csm = CookieSyncManager.createInstance(JREngage.getContext());
        CookieManager cm = CookieManager.getInstance();

        /* http://code.google.com/p/android/issues/detail?id=19294 */
        if (AndroidUtils.SDKINT >= 11) {
            // don't trim leading .s
        } else {
            /* Trim leading .s */
            if (domain.startsWith(".")) domain = domain.substring(1);
        }

        /* Cookies are stored by domain, and are not different for different schemes (i.e. http vs
         * https) (although they do have an optional 'secure' flag.) */
        domain = "http" + (secure ? "s" : "") + "://" + domain;
        String cookieGlob = cm.getCookie(domain);
        if (cookieGlob != null) {
            String[] cookies = cookieGlob.split(";");
            for (String cookieTuple : cookies) {
                String[] cookieParts = cookieTuple.split("=");

                /* setCookie has changed a lot between different versions of Android with respect to
                 * how it handles cookies like these, which are set in order to clear an existing
                 * cookie.  This way of invoking it seems to work on all versions. */
                cm.setCookie(domain, cookieParts[0] + "=;");

                /* These calls have worked for some subset of the the set of all versions of
                 * Android:
                 * cm.setCookie(domain, cookieParts[0] + "=");
                 * cm.setCookie(domain, cookieParts[0]); */
            }
            csm.sync();
        }
    }

    public void forgetAllAuthenticatedUsers() {
        if (Config.LOGD) Log.d(TAG, "[forgetAllAuthenticatedUsers]");

        for (String providerName : mAllProviders.keySet()) forgetAuthenticatedUserForProvider(providerName);
    }

    public JRProvider getProviderByName(String name) {
        return mAllProviders.getAsProvider(name);
    }

    public void notifyEmailSmsShare(String method) {
        StringBuilder body = new StringBuilder();
        body.append("method=").append(method);
        body.append("&device=").append("android");
        body.append("&appId=").append(mAppId);

        String url = ENVIRONMENT.getServerUrl() + "/social/record_activity";

        if (Config.LOGD) Log.d(TAG, "[notifyEmailSmsShare]: " + url + " data: " + body.toString());

        JRConnectionManagerDelegate jrcmd = new SimpleJRConnectionManagerDelegate() {
            @Override
            public void connectionDidFinishLoading(String payload,
                                                   String requestUrl,
                                                   Object userdata) {
                if (Config.LOGD) Log.d(TAG, "[notifyEmailSmsShare]: success");
            }

            @Override
            public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
                Log.e(TAG, "[notifyEmailSmsShare]: failure", ex);
            }
        };

        JRConnectionManager.createConnection(url, body.toString().getBytes(), jrcmd, false, null);
    }

    public void shareActivityForUser(JRAuthenticatedUser user) {
        if (Config.LOGD) Log.d(TAG, "[shareActivityForUser]");

        setCurrentlyPublishingProvider(user.getProviderName());

        StringBuilder body = new StringBuilder();
        String deviceToken = user.getDeviceToken();

        String activityContent;
        JRDictionary activityDictionary = mActivity.toJRDictionary();
        String activityJSON = activityDictionary.toJSON();
        activityContent = AndroidUtils.urlEncode(activityJSON);

        body.append("activity=").append(activityContent);

        /* These are [undocumented] parameters available to the mobile library for the purpose of
         * updating the social sharing UI. */
        body.append("&device_token=").append(deviceToken);
        body.append("&url_shortening=true");
        body.append("&provider=").append(user.getProviderName());
        body.append("&device=android");
        body.append("&app_name=").append(mUrlEncodedAppName);

        String url = ENVIRONMENT.getServerUrl() + "/api/v2/activity";

        if (Config.LOGD) Log.d(TAG, "[shareActivityForUser]: " + url + " data: " + body.toString());

        JRDictionary tag = new JRDictionary();
        tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_SHARE_ACTIVITY);
        tag.put(USERDATA_ACTIVITY_KEY, mActivity);
        tag.put(USERDATA_PROVIDER_NAME_KEY, mCurrentlyPublishingProvider.getName());
        JRConnectionManager.createConnection(url, body.toString().getBytes(), this, false, tag);

        if (Config.LOGD) Log.d(TAG, "[shareActivityForUser] connection started for url: " + url);
    }

    public void setStatusForUser(JRAuthenticatedUser user) {
        if (Config.LOGD) Log.d(TAG, "[shareActivityForUser]");

        setCurrentlyPublishingProvider(user.getProviderName());

        String deviceToken = user.getDeviceToken();
        String status = mActivity.getUserGeneratedContent();
        // TODO: this should also include other pieces of the activity

        StringBuilder body = new StringBuilder();
        // TODO: include truncate parameter here?
        body.append("status=").append(status);

        /* These are [undocumented] parameters available for use by the mobile library when
         * making calls to the Engage API. */
        body.append("&device_token=").append(deviceToken);
        body.append("&device=android");
        body.append("&app_name=").append(mUrlEncodedAppName);

        String url = ENVIRONMENT.getServerUrl() + "/api/v2/set_status";

        if (Config.LOGD)
            Log.d(TAG, "[setStatusForUser]: " + url + " data: " + body.toString());

        // TODO: same callback handler for status as activity?
        JRDictionary tag = new JRDictionary();
        tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_SHARE_ACTIVITY);
        tag.put(USERDATA_ACTIVITY_KEY, mActivity);
        tag.put(USERDATA_PROVIDER_NAME_KEY, mCurrentlyPublishingProvider.getName());
        JRConnectionManager.createConnection(url, body.toString().getBytes(), this, false, tag);

        if (Config.LOGD) Log.d(TAG, "[setStatusForUser] connection started for url: " + url);
    }

    private void makeCallToTokenUrl(String tokenUrl, String token, String providerName) {
        if (Config.LOGD) {
            Log.d(TAG, "[makeCallToTokenUrl] token: " + token);
            Log.d(TAG, "[makeCallToTokenUrl] tokenUrl: " + tokenUrl);
        }

        String body = "token=" + token;
        byte[] postData = body.getBytes();

        JRDictionary tag = new JRDictionary();
        tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_CALL_TOKEN_URL);
        tag.put(USERDATA_TOKEN_URL_KEY, tokenUrl);
        tag.put(USERDATA_PROVIDER_NAME_KEY, providerName);

        if (!JRConnectionManager.createConnection(tokenUrl, postData, this, true, tag)) {
            JREngageError error = new JREngageError(
                    "Problem initializing the connection to the token url",
                    AuthenticationError.AUTHENTICATION_FAILED,
                    ErrorType.AUTHENTICATION_FAILED);

            for (JRSessionDelegate delegate : getDelegatesCopy()) {
                delegate.authenticationCallToTokenUrlDidFail(tokenUrl, error, providerName);
            }
        }
    }

    public void triggerAuthenticationDidCompleteWithPayload(JRDictionary rpx_result) {
        if (Config.LOGD) Log.d(TAG, "[triggerAuthenticationDidCompleteWithPayload]");

        /* Instantiate a user object, keep track of it. */
        JRAuthenticatedUser user = new JRAuthenticatedUser(
                rpx_result,
                mCurrentlyAuthenticatingProvider.getName(),
                getWelcomeMessageFromCookieString());
        mAuthenticatedUsersByProvider.put(mCurrentlyAuthenticatingProvider.getName(), user);
        JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);

        for (JRSessionDelegate delegate : getDelegatesCopy()) {
            delegate.authenticationDidComplete(
                    rpx_result.getAsDictionary("auth_info"),
                    mCurrentlyAuthenticatingProvider.getName());
        }

        String auth_info_token = rpx_result.getAsString("token");
        if (!TextUtils.isEmpty(mTokenUrl)) {
            makeCallToTokenUrl(mTokenUrl,
                               auth_info_token,
                               mCurrentlyAuthenticatingProvider.getName());
        }

        mCurrentlyAuthenticatingProvider.setForceReauth(false);
        setCurrentlyAuthenticatingProvider(null);
    }

    public void triggerAuthenticationDidFail(JREngageError error) {
        if (Config.LOGD) Log.d(TAG, "[triggerAuthenticationDidFailWithError]");

        String providerName = mCurrentlyAuthenticatingProvider.getName();
        mCurrentlyAuthenticatingProvider.setForceReauth(true);

        setCurrentlyAuthenticatingProvider(null);
        mReturningBasicProvider = null;
        mReturningSocialProvider = null;

        for (JRSessionDelegate delegate : getDelegatesCopy()) {
            delegate.authenticationDidFail(error, providerName);
        }
    }

    public void triggerAuthenticationDidCancel() {
        if (Config.LOGD) Log.d(TAG, "[triggerAuthenticationDidCancel]");

        setCurrentlyAuthenticatingProvider(null);
        setReturningBasicProvider(null);

        for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.authenticationDidCancel();
    }

    public void triggerAuthenticationDidRestart() {
        if (Config.LOGD) Log.d(TAG, "[triggerAuthenticationDidRestart]");

        for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.authenticationDidRestart();
    }

    /**
     *
     */
    public void triggerPublishingDidComplete() {
        if (Config.LOGD) Log.d(TAG, "[triggerPublishingDidComplete]");

        for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidComplete();
    }

    public void triggerPublishingJRActivityDidFail(JREngageError error,
                                                   JRActivityObject activity,
                                                   String providerName) {
        if (Config.LOGD) Log.d(TAG, "[triggerPublishingJRActivityDidFail]");

        for (JRSessionDelegate delegate : getDelegatesCopy()) {
            delegate.publishingJRActivityDidFail(activity, error, providerName);
        }
    }

    /**
     * Informs delegates that the publishing dialog failed to display.
     * @param err
     *      The error to send to delegates
     */
    public void triggerPublishingDialogDidFail(JREngageError err) {
       if (Config.LOGD) Log.d(TAG, "[triggerPublishingDialogDidFail]");

        for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDialogDidFail(err);
    }

    /**
     *
     */
    public void triggerPublishingDidCancel() {
        if (Config.LOGD) Log.d(TAG, "[triggerPublishingDidCancel]");

        for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidCancel();
    }

    private void triggerMobileConfigDidFinish() {
        for (JRSessionDelegate d : getDelegatesCopy()) d.mobileConfigDidFinish();
    }

    private synchronized List<JRSessionDelegate> getDelegatesCopy() {
        return (mDelegates == null)
                ? new ArrayList<JRSessionDelegate>()
                : new ArrayList<JRSessionDelegate>(mDelegates);
    }

    public JRProvider getCurrentlyPublishingProvider() {
        return mCurrentlyPublishingProvider;
    }

    public void setCurrentlyPublishingProvider(String provider) {
        if (Config.LOGD) Log.d(TAG, "[setCurrentlyPublishingProvider]: " + provider);

        mCurrentlyPublishingProvider = getProviderByName(provider);
    }

    public boolean isGetMobileConfigDone() {
        return mGetConfigDone;
    }

    public static JREnvironment getEnvironment() {
        return ENVIRONMENT;
    }

    public String getUrlEncodedAppName() {
        return mUrlEncodedAppName;
    }
}
