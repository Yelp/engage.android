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
package com.janrain.android.engage;


// package.html type package documentation tag for Doxygen

// removed from the docs until these features are implemented:
//- Customize the login experience by displaying native and social login options on the same screen
//- Match the look and feel of the iPhone app with customizable background colors, images, and navigation bar tints


/**
 * @mainpage Janrain Engage Android
 *
 * <a href="http://rpxnow.com/docs/android">The
 * Janrain Engage for Android SDK</a> makes it easy to include third party authentication and
 * social publishing in your Android app.  This library includes the same key
 * features as Janrain Engage for the web, as well as additional features created specifically for
 * the mobile
 * platform. With as few as three lines of code, you can authenticate your users with their
 * accounts on Google, Yahoo!, Facebook, etc., and they can immediately publish their
 * activities to multiple social networks, including Facebook, Twitter, LinkedIn, MySpace,
 * and Yahoo, through one simple interface.
 *
 * Beyond authentication and social sharing, the latest release of the Engage for iPhone SDK
 * now allows mobile apps to:
 *   - Share content, activities, game scores or invitations via Email or SMS
 *   - Track popularity and click through rates on various links included in the
 *     shared email message with automatic URL shortening for up to 5 URLs
 *   - Provide an additional level of security with forced re-authentication when
 *     users are about to make a purchase or conduct a sensitive transaction
 *   - Configure and maintain separate lists of providers for mobile and web apps
 *
 * Before you begin, you need to have created a <a href="https://rpxnow.com/signup_createapp_plus">
 * Janrain Engage application</a>,
 * which you can do on <a href="http://rpxnow.com">http://rpxnow.com</a>
 *
 * For an overview of how the library works and how you can take advantage of the library's features,
 * please see the <a href="http://rpxnow.com/docs/android#user_experience">"Overview"</a> section of our documentation.
 *
 * To begin using the SDK, please see the <a href="http://rpxnow.com/docs/iphone#quick">"Quick Start Guide"</a>.
 *
 * For more detailed documentation of the library's API, you can use
 * the <a href="http://rpxnow.com/docs/android_api/annotated.html">"JREngage API"</a> documentation.
 **/

/**
 * @page Providers
 * @section basicProviders List of Providers
 *
 * Here is a list of possible strings that the argument provider can be
 * when used in the authentication methods:
 *   - "aol"
 *   - "blogger"
 *   - "facebook"
 *   - "flickr"
 *   - "google"
 *   - "hyves"
 *   - "linkedin"
 *   - "live_id"
 *   - "livejournal"
 *   - "myopenid"
 *   - "myspace"
 *   - "netlog"
 *   - "openid"
 *   - "paypal"
 *   - "twitter"
 *   - "verisign"
 *   \if wordpress_is_working
 *   - "wordpress"
 *   \endif
 *   - "yahoo"
 *
 * @note As your Engage application is limited by the number of providers it may use,
 * you may only see a subset of this list.
 *
 * @section socialProviders List of Social Providers
 *
 * Here is a list of possible strings that the argument provider can be
 * when used in the social publishing methods:
 *   - "facebook"
 *   - "linkedin"
 *   - "myspace"
 *   - "twitter"
 *   - "yahoo"
 *
 * @note As your Engage application is limited by the number of providers it may use,
 * you may only see a subset of this list.
 **/

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;

import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.session.JRAuthenticatedUser;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.session.JRSessionDelegate;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.ui.JRUserInterfaceMaestro;


/**
 * @brief
 * The JREngage class provides methods for configuring the Engage for Android library and
 * initiating authentication and social sharing.
 *
 * Prior to using the Engage for Android
 * library, you must already have an application on <a href="http://rpxnow.com">http://rpxnow.com</a>.
 * This is all that is required for basic authentication, although some providers require extra
 * configuration (which can be done through your application's <a href="http://rpxnow.com/relying_parties">Dashboard</a>).
 * For social publishing, you will need to configure your Engage application with the desired providers.
 *
 * You may optionally implement server-side authentication.
 * When configured, the Engage for Android library can post the user's authentication token to a URL on your server:
 * the token URL.  Your server can complete authentication, access more of the Engage web API, log the authentication, etc.
 * and the server's response will be passed back through to your Android application.
 *
 * To use JREngage:
 *  - Call JREngage.initInstance
 *  - Save the returned JREngage object
 *  - Invoke showAuthenticationDialog() or showSocialPublishingDialog(JRActivityObject activity)
 *  - You may implement JREngageDelegate to receive responses
 *
 **/
public class JREngage {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
	
	// Tag used for logging
	private static final String TAG = JREngage.class.getSimpleName();

    // Singleton instance of this class
	private static JREngage sInstance;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Initializes and returns the singleton instance of JREngage.
     *
     * @param context
     * 		the Android application Context, used for access to system resources (e.g. global
     * 		preferences).  This value cannot be null.
     *
     * @param appId
     * 		your 20-character application ID.  You can find this on your application's
     * 		Engage Dashboard at <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value
     * 		cannot be null.
     *
     * @param tokenUrl
     * 		the url on your server where you wish to complete authentication, or null.  If provided,
     *   	the JREngage library will post the user's authentication token to this url where it can
     *   	used for further authentication and processing.  When complete, the library will pass
     *   	the server's response back to the your application.

     * @param delegate
     * 		the delegate object that implements the JREngageDelegate protocol.
     *
     * @return
     * 		the shared instance of the \c JREngage object initialized with the given
     *   	<code>appId</code>, <code>tokenUrl</code>, and <code>delegate</code>.  If the given
     *   	<code>appId</code> is <code>null</code>, returns <code>null</code>.
     */
    public static JREngage initInstance(Context context,
                                        String appId,
                                        String tokenUrl,
                                        JREngageDelegate delegate) {
        // todo Display a helpful dialog if appId is null? To point devs in the right direction?

        if (context == null) {
            Log.e(TAG, "[initialize] context parameter cannot be null.");
            throw new IllegalArgumentException("context parameter cannot be null.");
        }

        if (TextUtils.isEmpty(appId)) {
            Log.e(TAG, "[initialize] appId parameter cannot be null.");
            throw new IllegalArgumentException("appId parameter cannot be null.");
        }

        if (sInstance == null) sInstance = new JREngage();
        sInstance.initialize(context, appId, tokenUrl, delegate);

        return sInstance;
    }

	/**
	 * Returns the singleton instance, provided it has been initialized.
	 * 
	 * @return
	 * 		the JREngage instance if properly initialized, null otherwise.
	 */
	public static JREngage getInstance() {
		return sInstance;
	}
	
	/**
     * \internal
     * @hide
     * @exclude
	 * Returns the application context used to initialize the library.
	 * 
	 * @return
	 * 		the Context object used to initialize this library.
	 */
	public static Context getContext() {
        return (sInstance == null) ? null : sInstance.mContext;
	}

    public static void setContext(Context c) {
        sInstance.mContext = c;
    }

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // Application context
    private Context mContext;

	// Holds configuration and state for the JREngage library
	private JRSessionData mSessionData;
	
	// Delegates (listeners) array
	private ArrayList<JREngageDelegate> mDelegates;
	
	private JRUserInterfaceMaestro mInterfaceMaestro;
	
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	/*
	 * Initializing constructor.
	 */
//	private JREngage(Context context, String appId, String tokenUrl, JREngageDelegate delegate) {
//        mContext = context;
//        mAppId = appId;
//        mTokenUrl = tokenUrl;
//        mDelegates = new ArrayList<JREngageDelegate>();
//        if (delegate != null) {
//            mDelegates.add(delegate);
//        }
//        mSessionData = JRSessionData.getInstance(mAppId, mTokenUrl, this);
//        mInterfaceMaestro = JRUserInterfaceMaestro.getInstance();
//	}

    /*
     * Hide default constructor (singleton pattern).
     */
	private JREngage() {
	}


	/*
	 * Initializer.
	 */
	private void initialize(Context context,
                            String appId,
                            String tokenUrl,
                            JREngageDelegate delegate) {
        mContext = context;
        mDelegates = new ArrayList<JREngageDelegate>();
        if (delegate != null) {
            mDelegates.add(delegate);
        }

        mSessionData = JRSessionData.getInstance(appId, tokenUrl, mJRSD);
        mInterfaceMaestro = JRUserInterfaceMaestro.getInstance();
	}

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    private JRSessionDelegate mJRSD = new JRSessionDelegate() {
        // ------------------------------------------------------------------------
        // DELEGATE METHODS
        // moved into a private inner class to keep the internal interface hidden
        // ------------------------------------------------------------------------

        public void authenticationDidRestart() {
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationDidRestart]");
            }
            mInterfaceMaestro.authenticationRestarted();
        }

        public void authenticationDidCancel() {
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationDidCancel]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrAuthenticationDidNotComplete();
            }

            mInterfaceMaestro.authenticationCanceled();
        }

        public void authenticationDidComplete(String token, String provider) {
            //
            // NOTE:  METHOD COMMENTED OUT IN IPHONE VERSION
            //
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationDidComplete]");
            }
        }

        public void authenticationDidComplete(JRDictionary profile, String provider) {
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationDidComplete]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrAuthenticationDidSucceedForUser(profile, provider);
            }

            mInterfaceMaestro.authenticationCompleted();
        }

        public void authenticationDidFail(JREngageError error, String provider) {
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationDidFail]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrAuthenticationDidFailWithError(error, provider);
            }

            mInterfaceMaestro.authenticationFailed();
        }

        public void authenticationDidReachTokenUrl(String tokenUrl,
                                                   HttpResponseHeaders headers,
                                                   String payload,
                                                   String provider) {
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationDidReachTokenUrl]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, payload, provider);
                delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, headers, payload, provider);
            }
        }

        public void authenticationCallToTokenUrlDidFail(String tokenUrl,
                                                        JREngageError error,
                                                        String provider) {
            if (Config.LOGD) {
                Log.d(TAG, "[authenticationCallToTokenUrlDidFail]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrAuthenticationCallToTokenUrlDidFail(tokenUrl, error, provider);
            }
        }

//        public void publishingDidRestart() {
//            if (Config.LOGD) {
//                Log.d(TAG, "[publishingDidRestart]");
//            }
//
//            // TODO:  implement UI stuff
//            // interfaceMaestro.publishingRestarted();
//        }

        public void publishingDidCancel() {
            if (Config.LOGD) {
                Log.d(TAG, "[publishingDidCancel]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrSocialDidNotCompletePublishing();
            }

            mInterfaceMaestro.publishingCanceled();
        }

        public void publishingDidComplete() {
            if (Config.LOGD) {
                Log.d(TAG, "[publishingDidComplete]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrSocialDidCompletePublishing();
            }

            // TODO:  implement UI stuff
            // interfaceMaestro.publishingCompleted();
        }

        public void publishingDialogDidFail(JREngageError err) {
            mInterfaceMaestro.publishingDialogFailed();
            engageDidFailWithError(err);
        }

        public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
            if (Config.LOGD) {
                Log.d(TAG, "[publishingJRActivityDidSucceed]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrSocialDidPublishJRActivity(activity, provider);
            }
        }

        public void publishingJRActivityDidFail(JRActivityObject activity,
                                                JREngageError error,
                                                String provider) {
            if (Config.LOGD) {
                Log.d(TAG, "[publishingJRActivityDidFail]");
            }

            for (JREngageDelegate delegate : getDelegatesCopy()) {
                delegate.jrSocialPublishJRActivityDidFail(activity, error, provider);
            }

            mInterfaceMaestro.publishingJRActivityFailed();
        }

        public void mobileConfigDidFinish() {}
    };

/**
 * @name Manage Authenticated Users
 * Methods that manage authenticated users remembered by the library
 **/
    
/*@{*/

    /**
     * @anchor signoutUserForProvider
     *
     * Remove the user's credentials for the given provider from the library.
     *
     * @param provider
     *   the name of the provider on which the user authenticated.  For a list of possible strings,
     *   please see the \ref basicProviders "List of Providers"
     **/    
    public void signoutUserForProvider(String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[signoutUserForProvider]");
        }
        mSessionData.forgetAuthenticatedUserForProvider(provider);
    }

    /**
     * @anchor signoutUserForAllProviders
     *
     * Remove the user's credentials for all providers from the library.
     **/
    public void signoutUserForAllProviders() {
        if (Config.LOGD) {
            Log.d(TAG, "[signoutUserForAllProviders]");
        }
        mSessionData.forgetAllAuthenticatedUsers();
    }

    /**
     * Specify whether the Engage for Android library will require the user to reauthenticate.
     * Reauthentication will require the user to re-enter their password.
     *
     * @param force
     *   true if the library should force reauthentication for all providers or false if the
     *   library should allow cached credentials to authenticate the user
     **/
    public void setAlwaysForceReauthentication(boolean force) {
        if (Config.LOGD) {
            Log.d(TAG, "[setAlwaysForceReauthentication]");
        }
        mSessionData.setAlwaysForceReauth(force);
    }
/*@}*/

/**
 * @name Cancel the JREngage Dialogs
 * Methods to cancel authentication and social publishing
 **/
/*@{*/
    /**
     * Stops the authentication flow.  This finishes all Engage for Android activities and returns
     * the calling Activity to the top of the application's activity stack.
     **/
    public void cancelAuthentication() {
        if (Config.LOGD) {
            Log.d(TAG, "[cancelAuthentication]");
        }
        mSessionData.triggerAuthenticationDidCancel();
    }

    /**
     * Stops the publishing flow.  This finishes all Engage for Android activities and returns
     * the calling Activity to the top of the application's activity stack.
     **/
    public void cancelPublishing() {
        if (Config.LOGD) {
            Log.d(TAG, "[cancelPublishing]");
        }
        mSessionData.triggerPublishingDidCancel();
    }
/*@}*/

/**
 * @name Server-side Authentication
 * Methods to configure server-side authentication
 **/
/*@{*/

    /**
     * @anchor setTokenUrl
     *
     * Specify a token URL (potentially a different token URL than the one the library was
     * initialized with).
     * 
     * @param newTokenUrl
     *   the new token URL you wish authentications to post the Engage auth_info token to
     **/
    public void setTokenUrl(String newTokenUrl) {
        if (Config.LOGD) {
            Log.d(TAG, "[setTokenUrl]");
        }
        mSessionData.setTokenUrl(newTokenUrl);
    }
/*@}*/

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
	

/**
 * @name Manage the JREngage Delegates
 * Add/remove delegates that implement the JREngageDelegate protocol
 **/
/*@{*/

    /**
     * Add a JREngageDelegate to the library
     *
     * @param delegate
     *   the object that implements the JREngageDelegate protocol
     **/
    public void addDelegate(JREngageDelegate delegate) {
		if (Config.LOGD) { 
			Log.d(TAG, "[addDelegate]"); 
		}
		mDelegates.add(delegate);
	}

    /**
     * Remove a JREngageDelegate from the library
     *
     * @param delegate
     *   the object that implements the JREngageDelegate protocol
     **/	
	public void removeDelegate(JREngageDelegate delegate) {
		if (Config.LOGD) { 
			Log.d(TAG, "[removeDelegate]"); 
		}
		mDelegates.remove(delegate);
	}
/*@}*/
    
    private void engageDidFailWithError(JREngageError error) {
        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrEngageDialogDidFailToShowWithError(error);
        }
    }

    private boolean checkSessionDataError() {
        /* If there was error configuring the library, sessionData.error will not be null. */
        JREngageError error = mSessionData.getError();
        if (error != null) {
            /* If there was an error, send a message to the delegates, release the error, then
          attempt to restart the configuration.  If, for example, the error was temporary
          (network issues, etc.) reattempting to configure the library could end successfully.
          Since configuration may happen before the user attempts to use the library, if the
          user attempts to use the library at all, we only try to reconfigure when the library
          is needed. */
            if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                engageDidFailWithError(error);
                mSessionData.tryToReconfigureLibrary();

                return true;
            }
        }
        return false;
    }


/** @anchor showMethods **/
/**
 * @name Show the JREngage Dialogs
 * Methods that display the Engage for Android dialogs which initiate authentication and
 * social publishing
 **/
/*@{*/

/**
 * @anchor showAuthDialog
 *
 * Begins authentication.  The library will
 * start a new Android Activity and take the user through the sign-in process.
 **/
    public void showAuthenticationDialog() {
        if (Config.LOGD) {
            Log.d(TAG, "[showProviderSelectionDialog]");
        }

        if (checkSessionDataError()) return;

        mInterfaceMaestro.showProviderSelectionDialog();
    }

//    public void showAuthenticationDialog(boolean forceReauth) {
//        if (Config.LOGD) {
//            Log.d(TAG, "[showAuthenticationDialog(boolean forceReath)]");
//        }
//
//        if (checkSessionDataError()) return;
//
//        mInterfaceMaestro.showProviderSelectionDialog(forceReauth);
//
//    }


    /**
     * @anchor showPubDialog
     *
     * Begin social publishing.  The library will
     * start a new Android Activity enabling the user to publish a social share.  The user will also
     * be taken through the sign-in process, if necessary.
     *
     * @sa \c JRActivityObject
     *
     * @param activity
     *   the activity you wish to share
     **/
    public void showSocialPublishingDialog(JRActivityObject activity) {
        if (Config.LOGD) {
            Log.d(TAG, "[showSocialPublishingDialog]");
        }

        /* If there was error configuring the library, sessionData.error will not be null. */
        if (checkSessionDataError()) return;


        if (activity == null) {
            engageDidFailWithError(new JREngageError(
                    "Activity object cannot be null",
                    JREngageError.SocialPublishingError.ACTIVITY_NIL,
                    JREngageError.ErrorType.PUBLISH_FAILED
            ));
        }

        mSessionData.setJRActivity(activity);

        mInterfaceMaestro.showPublishingDialogWithActivity();

    }
/*@}*/

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

    private synchronized List<JREngageDelegate> getDelegatesCopy() {
        return (mDelegates == null)
                ? new ArrayList<JREngageDelegate>()
                : new ArrayList<JREngageDelegate>(mDelegates);
    }
}
