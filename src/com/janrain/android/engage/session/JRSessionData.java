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
import java.util.List;

import android.content.Context;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREnvironment;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.prefs.Prefs;
import com.janrain.android.engage.types.JRProviderList;
import com.janrain.android.engage.utils.Archiver;
import com.janrain.android.engage.utils.StringUtils;

import android.text.TextUtils;

import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.JREngageError.ConfigurationError;
import com.janrain.android.engage.JREngageError.ErrorType;
import com.janrain.android.engage.JREngageError.SocialPublishingError;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import org.apache.http.util.EncodingUtils;

public class JRSessionData implements JRConnectionManagerDelegate {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRSessionData.class.getSimpleName();
    
	private static JRSessionData sInstance;

    private static final JREnvironment ENVIRONMENT = JREnvironment.LOCAL;

    private static final String ARCHIVE_USERS = "users";
    private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
    private static final String ARCHIVE_BASIC_PROVIDERS = "basicProviders";
    private static final String ARCHIVE_SOCIAL_PROVIDERS = "socialProviders";
    private static final String ARCHIVE_ICONS_STILL_NEEDED = "iconsStillNeeded";
    private static final String ARCHIVE_PROVIDERS_WITH_ICONS = "providersWithIcons";
    private static final String ARCHIVE_BASE_URL = "baseUrl";
    private static final String ARCHIVE_HIDE_POWERED_BY = "hidePoweredBy";
    private static final String ARCHIVE_LAST_USED_SOCIAL = "lastUsedSocialProvider";
    private static final String ARCHIVE_LAST_USED_BASIC = "lastUsedBasicProvider";

    private static final String FMT_CONFIG_URL =
            "%s/openid/iphone_config_and_baseurl?appId=%s&skipXdReceiver=true";
    

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
            if (Config.LOGD) {
                Log.d(TAG, "[getInstance] returning existing instance.");
            }
			return sInstance;
		}
		
		if (TextUtils.isEmpty(appId)) {
            Log.w(TAG, "[getInstance] null instance w/ null appId specified.");
			return null;
		}

        // TODO:  QUESTIONS FOR LILLI
        //   -- the singleton approach here will limit the instance to a single appId
        //   -- and a single delegate.
        //   -- 1. Is this correct behavior
        //   -- 2. If yes, why do we need a collection of delegates and not just one?

		sInstance = new JRSessionData(appId, tokenUrl, delegate);
        if (Config.LOGD) {
            Log.d(TAG, "[getInstance] returning new instance.");
        }
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
	private JRProviderList mBasicProviders;
	private JRProviderList mSocialProviders;
	private JRDictionary mAuthenticatedUsersByProvider;
	
	private String mSavedConfigurationBlock;
	private String mNewEtag;
    private String mGitCommit;

    private JRProviderList mProvidersWithIcons;
    private JRDictionary mIconsStillNeeded;

	private JRActivityObject mActivity;
	
	private String mTokenUrl;
	private String mBaseUrl;
	private String mAppId;
    private String mDevice;

    private boolean mHidePoweredBy;

	private boolean mAlwaysForceReauth;
	private boolean mForceReauth;

	private boolean mSocialSharing;
	
	private boolean mDialogIsShowing;
	private JREngageError mError;
	
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private JRSessionData(String appId, String tokenUrl, JRSessionDelegate delegate) {
        if (Config.LOGD) {
            Log.d(TAG, "[ctor] creating instance.");
        }

		mDelegates = new ArrayList<JRSessionDelegate>();
		mDelegates.add(delegate);
		
		mAppId = appId;
		mTokenUrl = tokenUrl;

        mAuthenticatedUsersByProvider = new JRDictionary();
        mAllProviders = new JRDictionary();

        // load cached data
        Context ctx = JREngage.getContext();

        // Load dictionary of authenticated users.  If the dictionary is not found, the
        // archiver will return a new (empty) list.
        mAuthenticatedUsersByProvider = JRDictionary.unarchive(ctx, ARCHIVE_USERS);

        // Load the list of all providers
        mAllProviders = JRDictionary.unarchive(ctx, ARCHIVE_ALL_PROVIDERS);

        // Load the list of basic providers
        mBasicProviders = JRProviderList.unarchive(ctx, ARCHIVE_BASIC_PROVIDERS);

        // Load the list of social providers
        mSocialProviders = JRProviderList.unarchive(ctx, ARCHIVE_SOCIAL_PROVIDERS);

        // Load the list of icons that the library should re-attempt to download, in case
        // previous attempts failed for whatever reason
        mIconsStillNeeded = JRDictionary.unarchive(ctx, ARCHIVE_ICONS_STILL_NEEDED);

        // Load the set of providers that already have all of their icons; checking this list
        // is faster than checking for the icons themselves
        mProvidersWithIcons = JRProviderList.unarchive(ctx, ARCHIVE_PROVIDERS_WITH_ICONS);

        // Load the base url and whether or not we need to hide the tagline.
        mBaseUrl = Archiver.loadString(ctx, ARCHIVE_BASE_URL);
        mHidePoweredBy = Archiver.loadBoolean(ctx, ARCHIVE_HIDE_POWERED_BY);

        // And load the last used basic and social providers
        mReturningSocialProvider = Archiver.loadString(ctx, ARCHIVE_LAST_USED_SOCIAL);
        mReturningBasicProvider = Archiver.loadString(ctx, ARCHIVE_LAST_USED_BASIC);

        mError = startGetConfiguration();
	}
	
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // DELEGATE METHODS
    // ------------------------------------------------------------------------

    public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
        Log.i(TAG, "[connectionDidFail]");

        if (userdata == null) {
            // TODO:  this case not handled on iPhone, is it possible?
        } else if (userdata instanceof String) {
            String s = (String)userdata;
            if (s.equals(JREngageError.ErrorType.CONFIGURATION_FAILED)) {
                mError = new JREngageError(
                        "There was a problem communicating with the Janrain server while configuring authentication.",
                        ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                        JREngageError.ErrorType.CONFIGURATION_FAILED);
            } else if (s.equals("shareActivity")) {
                List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                for (JRSessionDelegate delegate : delegatesCopy) {
                    JREngageError error = new JREngageError(
                        "Session error", JREngageError.CODE_UNKNOWN, "", ex
                    );
                    delegate.publishingActivity(mActivity, error, mCurrentProvider.getName());
                }
            }

        } else if (userdata instanceof JRDictionary) {
            JRDictionary dictionary = (JRDictionary) userdata;
            if ((dictionary != null) && (dictionary.containsKey("tokenUrl"))) {
                List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                for (JRSessionDelegate delegate : delegatesCopy) {
                    JREngageError error = new JREngageError(
                        "Session error", JREngageError.CODE_UNKNOWN, "", ex
                    );
                    delegate.authenticationCallToTokenUrl(
                            dictionary.getAsString("tokenUrl"),
                            error,
                            mCurrentProvider.getName());
                }
            }
        }
	}

    public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
        Log.i(TAG, "[connectionDidFinishLoading]");

        if (Config.LOGD) {
            Log.d(TAG, "[connectionDidFinishLoading] payload: " + payload);
        }

        if (userdata == null) {
            // TODO:  this case is not handled on iPhone, is it possible?
        } else if (userdata instanceof String) {
            String tag = (String) userdata;

            if (tag.equals("shareActivity")) {
                JRDictionary responseDict = JRDictionary.fromJSON(payload);
                if (responseDict == null) {
                    List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                    for (JRSessionDelegate delegate : delegatesCopy) {
                        delegate.publishingActivity(
                                mActivity,
                                new JREngageError(payload,
                                        SocialPublishingError.FAILED,
                                        ErrorType.PUBLISH_FAILED),
                                mCurrentProvider.getName());
                    }
                }

                // TODO:  Question for Lilli
                //    Should the if-block above return?  Or should the next block actually be
                //    an else-if?  We're checking responseDict for null above and then assuming it
                //    is not null below.  That is bad.
                // TODO:  Question for Lilli

                
                if (responseDict.containsKey("stat") && ("ok".equals(responseDict.get("stat")))) {
                    saveLastUsedSocialProvider();
                    List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                    for (JRSessionDelegate delegate : delegatesCopy) {
                        delegate.publishingActivityDidSucceed(
                                mActivity,
                                mCurrentProvider.getName()
                        );
                    }
                } else {
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

                        switch (code) {
                            case 0: /* "Missing parameter: apiKey" */
                                publishError = new JREngageError(
                                        errorDict.getAsString("msg"),
                                        SocialPublishingError.MISSING_API_KEY,
                                        ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                                break;
                            case 4: /* "Facebook Error: Invalid OAuth 2.0 Access Token" */
                                publishError = new JREngageError(
                                        errorDict.getAsString("msg"),
                                        SocialPublishingError.INVALID_OAUTH_TOKEN,
                                        ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                                break;
                            case 100: // TODO LinkedIn character limit error
                                publishError = new JREngageError(
                                        errorDict.getAsString("msg"),
                                        SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED,
                                        ErrorType.PUBLISH_INVALID_ACTIVITY);
                                break;
                            case 6: // TODO Twitter duplicate error
                                publishError = new JREngageError(
                                        errorDict.getAsString("msg"),
                                        SocialPublishingError.DUPLICATE_TWITTER,
                                        ErrorType.PUBLISH_INVALID_ACTIVITY);
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

                    List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                    for (JRSessionDelegate delegate : delegatesCopy) {
                        delegate.publishingActivity(mActivity,publishError,
                                mCurrentProvider.getName());
                    }
                }
            }
        }

	}

    public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload,
                                           String requestUrl, Object userdata) {
        Log.i(TAG, "[connectionDidFinishLoading-full]");

        if (userdata == null) {
            // TODO:  this case is not handled on iPhone, is it possible?
        } else if (userdata instanceof JRDictionary) {
            JRDictionary dictionary = (JRDictionary) userdata;
            if ((dictionary != null) && (dictionary.containsKey("tokenUrl"))) {
                List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                for (JRSessionDelegate delegate : delegatesCopy) {
                    delegate.authenticationDidReachTokenUrl(
                            dictionary.getAsString("tokenUrl"),
                            headers,
                            payload,
                            mCurrentProvider.getName());
                }
            }

        } else if (userdata instanceof String) {
            String s = (String)userdata;
            if (TextUtils.isEmpty(s)) {
                // TODO:  tag string is null or empty...not handled on iPhone, is it possible?
            } else if (s.equals("getConfiguration")) {
                String payloadString = EncodingUtils.getAsciiString(payload);
                if (Config.LOGD) {
                    Log.d(TAG, "[connectionDidFinishLoading-full] payload string: "
                            + payloadString);
                }

                if (payloadString.contains("\"provider_info\":{")) {
                    mError = finishGetConfiguration(payloadString, headers.getETag());
                } else {
                    mError = new JREngageError(
                            "There was a problem communicating with the Janrain server while configuring authentication.",
                            ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                            ErrorType.CONFIGURATION_FAILED);
                }

            }
        }
	}

    public void connectionWasStopped(Object userdata) {
        Log.i(TAG, "[connectionWasStopped]");
        // nothing to do here...
	}

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    private JREngageError startGetConfiguration() {
        String urlString = String.format(FMT_CONFIG_URL, ENVIRONMENT.getServerUrl(), mAppId);
        if (Config.LOGD) {
            Log.d(TAG, "[startGetConfiguration] url: " + urlString);
        }

        final String tag = "getConfiguration";

        if (!JRConnectionManager.createConnection(urlString, this, true, tag)) {
            Log.w(TAG, "[startGetConfiguration] createConnection failed.");
            return new JREngageError(
                    "There was a problem connecting to the Janrain server while configuring authentication.",
                    ConfigurationError.URL_ERROR,
                    JREngageError.ErrorType.CONFIGURATION_FAILED);
        }

        return null;
    }

    private JREngageError finishGetConfiguration(String dataStr) {
        if (Config.LOGD) {
            Log.d(TAG, "[finishGetConfiguration]");
        }

        // Attempt to parse the return string as JSON.
        JRDictionary jsonDict = null;
        Exception jsonEx = null;
        try {
            jsonDict = JRDictionary.fromJSON(dataStr);
        } catch (Exception e) {
            jsonEx = e;
        }

        // If the parsed dictionary object is null or an exception has occurred, return.
        if ((jsonDict == null) || (jsonEx != null)) {
            Log.w(TAG, "[finishGetConfiguration] failed.");
            return new JREngageError(
                    "There was a problem communicating with the Janrain server while configuring authentication.",
                    ConfigurationError.JSON_ERROR,
                    ErrorType.CONFIGURATION_FAILED,
                    jsonEx);
        }

        // Check to see if the base url has changed
        String baseUrl = jsonDict.getAsString("baseurl", "");
        if (!baseUrl.equals(mBaseUrl)) {
            // Save the new base url
            mBaseUrl = StringUtils.chomp(baseUrl, "/");
            Prefs.putString("jrBaseUrl", mBaseUrl);
        }

        // Get the providers out of the provider_info section.  These are likely to have changed.
        JRDictionary providerInfo = jsonDict.getAsDictionary("provider_info");
        mAllProviders = new JRDictionary(providerInfo.size());

        // For each provider
        for (String name : providerInfo.keySet()) {
            // Get its dictionary
            JRDictionary dictionary = providerInfo.getAsDictionary(name);
            // Use this to create a provider object
            JRProvider provider = new JRProvider(name, dictionary);
            // and finally add the object to our dictionary of providers
            mAllProviders.put(name, provider);
        }

        // Get the ordered list of basic providers
        mBasicProviders = jsonDict.getAsProviderList("enabled_providers");
        // Get the ordered list of social providers
        mSocialProviders = jsonDict.getAsProviderList("social_providers");

        // Done!

        // Save data to local store
        Context ctx = JREngage.getContext();
        JRDictionary.archive(ctx, ARCHIVE_ALL_PROVIDERS, mAllProviders);
        JRProviderList.archive(ctx, ARCHIVE_BASIC_PROVIDERS, mBasicProviders);
        JRProviderList.archive(ctx, ARCHIVE_SOCIAL_PROVIDERS, mSocialProviders);

        // Figure out of we're suppose to hide the powered by line
        mHidePoweredBy = jsonDict.getAsBoolean("hide_tagline", false);
        Prefs.putBoolean(ctx, "jrHidePoweredBy", mHidePoweredBy);

        // Once we know everything is parsed and saved, save the new etag
        Prefs.putString(ctx, "jrConfigurationEtag", mNewEtag);

        return null;
    }

    private JREngageError finishGetConfiguration(String dataStr, String etag) {
        if (Config.LOGD) {
            Log.d(TAG, "[finishGetConfiguration-etag]");
        }

        /* If the configuration for this rp has changed, the etag will have changed, and we need
         * to update our current configuration information. */
        String oldEtag = Prefs.getAsString(Prefs.KEY_JR_CONFIGURATION_ETAG, "");
        if (!oldEtag.equals(etag)) {
            mNewEtag = etag;

            /* We can only update all of our data if the UI isn't currently using that
             * information.  Otherwise, the library may crash/behave inconsistently.  If a
             * dialog isn't showing, go ahead and update that information.  Or, in the case
             * where a dialog is showing but there isn't any data that it could be using (that
             * is, the lists of basic and social providers are nil), go ahead and update it too.
             * The dialogs won't try and do anything until we're done updating the lists. */
            if (!mDialogIsShowing || (JRProviderList.isEmpty(mBasicProviders)
                    && JRProviderList.isEmpty(mSocialProviders))) {
                return finishGetConfiguration(dataStr);
            }

            /* Otherwise, we have to save all this information for later.  The
             * UserInterfaceMaestro sends a signal to sessionData when the dialog closes (by
             * setting the boolean dialogIsShowing to "NO". In the setter function, sessionData
             * checks to see if there's anything stored in the savedConfigurationBlock, and
             * updates it then. */
            mSavedConfigurationBlock = dataStr;
        }

        return null;
    }

    private String getWelcomeMessageFromCookieString(String cookieString) {
        if (Config.LOGD) {
            Log.d(TAG, "[getWelcomeMessageFromCookieString]");
        }

        String retval = "Welcome, user!";

        if (!TextUtils.isEmpty(cookieString)) {
            String[] parts = cookieString.split("%22");
            if (parts.length > 1) {
                retval = "Sign in as " + parts[5];
                // TODO: remove '+' and percent escapes
            }
        }

        return retval;
    }

    private void loadLastUsedSocialProvider() {
        if (Config.LOGD) {
            Log.d(TAG, "[loadLastUsedSocialProvider]");
        }
        mReturningSocialProvider = Prefs.getAsString("jrLastUsedSocialProvider", "");
    }

    private void loadLastUsedBasicProvider() {
        if (Config.LOGD) {
            Log.d(TAG, "[loadLastUsedBasicProvider]");
        }
        mReturningBasicProvider = Prefs.getAsString("jrLastUsedBasicProvider", "");
    }

    private void saveLastUsedSocialProvider() {
        if (Config.LOGD) {
            Log.d(TAG, "[saveLastUsedSocialProvider]");
        }
        mReturningSocialProvider = mCurrentProvider.getName();
        Prefs.putString("jrLastUsedSocialProvider", mReturningSocialProvider);
    }

    private void saveLastUsedBasicProvider() {
        if (Config.LOGD) {
            Log.d(TAG, "[saveLastUsedBasicProvider]");
        }

        // TODO:  cookie storage

        mReturningBasicProvider = mCurrentProvider.getName();
        Prefs.putString("jrLastUsedBasicProvider", mReturningBasicProvider);
    }


    /*
    TODO:  This method appears to have gone away.

    private String appNameAndVersion() {
        final String FMT = "appName=%s.%s&version=%d_%s";

        Context ctx = JREngage.getContext();
        PackageManager pkgMgr = ctx.getPackageManager();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pkgMgr.getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "[appNameAndVersion] package manager issue -> ", e);
        }

        return (pkgInfo != null)
                ? String.format(FMT,
                    pkgInfo.packageName,
                    pkgInfo.applicationInfo.nonLocalizedLabel,
                    pkgInfo.versionCode,
                    pkgInfo.versionName)
                : "";
    }
     */

    private synchronized List<JRSessionDelegate> getDelegatesCopy() {
        return (mDelegates == null)
                ? new ArrayList<JRSessionDelegate>()
                : new ArrayList<JRSessionDelegate>(mDelegates);
    }
}
