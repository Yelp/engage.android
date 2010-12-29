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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;

import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.session.JRSessionDelegate;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

/**
 * TODO:DOC
 *
 */
public class JREngage implements JRSessionDelegate {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
	
	// Tag used for logging
	private static final String TAG = JREngage.class.getSimpleName();
	
	private static JREngage sInstance;
	private static Context sContext;
	
    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	/**
	 * Returns singleton instance, provided it has been initialized.
	 * 
	 * @return
	 * 		JREngage instance if properly initialized, null otherwise.
	 */
	public static JREngage getInstance() {
		return sInstance;
	}
	
	/**
	 * Initializes and returns instance of JREngage.
	 * 
	 * @param context
	 * 		Application context used for access to application and information (e.g. global 
	 * 		preferences).  This value cannot be null.
	 * 
	 * @param appId
	 * 		This is your 20-character application ID.  You can find this on your application's 
	 * 		Dashboard on <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value cannot be 
	 * 		null.
	 * 
	 * @param tokenUrl
	 * 		The url on your server where you wish to complete authentication.  If provided, 
	 *   	the JREngage library will post the user's authentication token to this url where it can
	 *   	used for further authentication and processing.  When complete, the library will pass 
	 *   	the server's response back to the your application.

	 * @param delegate
	 * 		The delegate object that implements the JREngageDelegate protocol.
	 * 
	 * @return
	 * 		The shared instance of the \c JREngage object initialized with the given
	 *   	<code>appId</code>, <code>tokenUrl</code>, and <code>delegate</code>.  If the given 
	 *   	<code>appId</code> is <code>null</code>, returns <code>null</code>.
	 */
	public static JREngage initialize(Context context, String appId, String tokenUrl, 
			JREngageDelegate delegate) {
		if (sInstance != null) {
			return sInstance;
		}
		
		if (context == null) {
			Log.e(TAG, "[initialize] context parameter cannot be null.");
			return null;
		}
		
		if (TextUtils.isEmpty(appId)) {
			Log.e(TAG, "[initialize] appId parameter cannot be null.");
			return null;
		}
		
		return initHelper(context, appId, tokenUrl, delegate);
	}
	
	/**
	 * Returns the application context used to initialize the library.
	 * 
	 * @return
	 * 		Context object used to initialize this library.
	 */
	public static Context getContext() {
		return sContext;
	}
	
	/*
	 * Helper method for initialization.
	 */
	private static synchronized JREngage initHelper(Context context, String appId, String tokenUrl, 
			JREngageDelegate delegate) {

		Log.i(TAG, "[initialize] JREngage library initialing.");
		if (Config.LOGD) {
			Log.d(TAG, "[initialize] appId: " + appId + " | tokenUrl: " + tokenUrl);
		}
		
		if (sInstance == null) {
			sInstance = new JREngage();
		}
		
		sContext = context;
		sInstance.mDelegates.add(delegate);
		
		return sInstance;
	}

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

	// Holds configuration and state for the JREngage library
	private JRSessionData mSessionData;
	
	// Delegates (listeners) array
	private ArrayList<JREngageDelegate> mDelegates;
	
	// TODO:  Need to implement UI classes
	// private JRUserInterfaceMaestro mInterfaceMaestro;
	
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private JREngage() {
		//mSessionData = new JRSessionData();
		mDelegates = new ArrayList<JREngageDelegate>();
	}
	
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // DELEGATE METHODS
    // ------------------------------------------------------------------------

    public void authenticationDidRestart() {
        if (Config.LOGD) {
            Log.d(TAG, "[authenticationDidRestart]");
        }
        // TODO:  implement UI stuff
        // interfaceMaestro.authenticationRestarted();
    }

    public void authenticationDidCancel() {
        if (Config.LOGD) {
            Log.d(TAG, "[authenticationDidCancel]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrAuthenticationDidNotComplete();
        }

        // TODO:  implement UI stuff
        // interfaceMaestro.authenticationCanceled();
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

        // TODO:  implement UI stuff
        // interfaceMaestro.authenticationCompleted();
    }

    public void authenticationDidFail(JREngageError error, String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[authenticationDidFail]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrAuthenticationDidFailWithError(error, provider);
        }

        // TODO:  implement UI stuff
        // interfaceMaestro.authenticationFailed();
    }

    public void authenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, byte[] payload, String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[authenticationDidReachTokenUrl]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, payload, provider);
            delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, response, payload, provider);
        }
    }

    public void authenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[authenticationCallToTokenUrlDidFail]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrAuthenticationCallToTokenUrlDidFail(tokenUrl, error, provider);
        }        
    }

    public void publishingDidRestart() {
        if (Config.LOGD) {
            Log.d(TAG, "[publishingDidRestart]");
        }

        // TODO:  implement UI stuff
        // interfaceMaestro.publishingRestarted();
    }

    public void publishingDidCancel() {
        if (Config.LOGD) {
            Log.d(TAG, "[publishingDidCancel]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrSocialDidNotCompletePublishing();
        }

        // TODO:  implement UI stuff
        // interfaceMaestro.publishingCanceled();
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

    public void publishingActivityDidSucceed(JRActivityObject activity, String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[publishingActivityDidSucceed]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrSocialDidPublishActivity(activity, provider);
        }
    }

    public void publishingActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[publishingActivityDidFail]");
        }

        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrSocialPublisingActivityDidFail(activity, error, provider);
        }
    }

    /*
     * TODO:  See setCustomNavigationController in iPhone code.  Do we need this?
     */


    public JRDictionary getUserForProvider(String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[getUserForProvider]");
        }
        return mSessionData.authenticatedUserForProviderNamed(provider).getAuthInfo();
    }

    public void signoutUserForProvider(String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[signoutUserForProvider]");
        }
        mSessionData.forgetAuthenticatedUserForProvider(provider);
    }

    public void signoutUserForAllProviders() {
        if (Config.LOGD) {
            Log.d(TAG, "[signoutUserForAllProviders]");
        }
        mSessionData.forgetAllAuthenticatedUsers();
    }

    public void signoutUserForSocialProvider(String provider) {
        if (Config.LOGD) {
            Log.d(TAG, "[signoutUserForSocialProvider]");
        }
        mSessionData.forgetAuthenticatedUserForProvider(provider);
    }

    public void signoutUserForAllSocialProviders() {
        if (Config.LOGD) {
            Log.d(TAG, "[signoutUserForAllSocialProviders]");
        }
        mSessionData.forgetAllAuthenticatedUsers();
    }

    public void alwaysForceReauthentication(boolean force) {
        if (Config.LOGD) {
            Log.d(TAG, "[alwaysForceReauthentication]");
        }
        mSessionData.setAlwaysForceReauth(force);
    }

    public void cancelAuthentication() {
        if (Config.LOGD) {
            Log.d(TAG, "[cancelAuthentication]");
        }
        mSessionData.triggerAuthenticationDidCancel();
    }

    public void cancelPublishing() {
        if (Config.LOGD) {
            Log.d(TAG, "[cancelPublishing]");
        }
        mSessionData.triggerPublishingDidCancel();
    }

    public void updateTokenUrl(String newTokenUrl) {
        if (Config.LOGD) {
            Log.d(TAG, "[updateTokenUrl]");
        }
        mSessionData.setTokenUrl(newTokenUrl);
    }


    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
	
	public void addDelegate(JREngageDelegate delegate) {
		if (Config.LOGD) { 
			Log.d(TAG, "[addDelegate]"); 
		}
		mDelegates.add(delegate);
	}
	
	public void removeDelegate(JREngageDelegate delegate) {
		if (Config.LOGD) { 
			Log.d(TAG, "[removeDelegate]"); 
		}
		mDelegates.remove(delegate);
	}

    public void engageDidFailWithError(JREngageError error) {
        for (JREngageDelegate delegate : getDelegatesCopy()) {
            delegate.jrEngageDialogDidFailToShowWithError(error);
        }
    }

	public void showAuthenticationDialog() {
		if (Config.LOGD) { 
			Log.d(TAG, "[showAuthenticationDialog]"); 
		}

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

                return;
            }
        }

        // TODO:  implement UI stuff
        // interfaceMaestro.showAuthenticationDialog();
	}

    public void showSocialPublishingDialogWithActivity(JRActivityObject activity) {
        if (Config.LOGD) {
            Log.d(TAG, "[showSocialPublishingDialogWithActivity]");
        }

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

                return;
            }
        }

        if (activity == null) {
            engageDidFailWithError(new JREngageError(
                    "Activity object cannot be null",
                    JREngageError.SocialPublishingError.ACTIVITY_NIL,
                    JREngageError.ErrorType.PUBLISH_FAILED
            ));
        }

        mSessionData.setActivity(activity);

        // TODO:  implement UI stuff
        // interfaceMaestro.showPublishingDialogWithActivity();
    }
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("JREngage does not permit clone");
	}

    private synchronized List<JREngageDelegate> getDelegatesCopy() {
        return (mDelegates == null)
                ? new ArrayList<JREngageDelegate>()
                : new ArrayList<JREngageDelegate>(mDelegates);
    }

}
