/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android;

import android.app.Activity;
import android.content.Context;
import com.janrain.android.capture.JRCapture;
import com.janrain.android.capture.JRCaptureRecord;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.types.JRDictionary;


import static com.janrain.android.Jump.SignInResultHandler.FailureReasons;
import static com.janrain.android.capture.JRCapture.SignInResponseHandler;

public class Jump {
    /*package*/ enum State {
        STATE;

        /*package*/ JRCaptureRecord signedInUser;
        /*package*/ JREngage jrEngage;
        /*package*/ String captureDomain;
        /*package*/ String captureClientId;
        /*package*/ SignInResultHandler signInHandler;
        /*package*/ TraditionalSignInType traditionalSignInType;
    }

    /*package*/ static final State state = State.STATE;

    private Jump() {}

    /**
     * Initialize the Jump library with you configuration data
     * @param context A context to perform some disk IO with
     * @param engageAppId The application ID of your Engage app, from the Engage Dashboard
     * @param captureDomain The domain of your Capture app, contact your deployment engineer for this
     * @param captureClientId The Capture API client ID for use with this mobile app.
     *                        Important: You should generate a separate API client for each mobile app.
     *                        DO NOT USE THE OWNER API CLIENT.
     * @param traditionalSignInType The type of traditional sign-in i.e. username or email address based.
     */
    public static void init(Context context, String engageAppId, String captureDomain,
                            String captureClientId, TraditionalSignInType traditionalSignInType) {
        state.jrEngage = JREngage.initInstance(context.getApplicationContext(), engageAppId, null, null);
        state.captureDomain = captureDomain;
        state.captureClientId = captureClientId;
        state.traditionalSignInType = traditionalSignInType;
        //j.showAuthenticationDialog();
        //j.addDelegate(); remove
        //j.cancelAuthentication();
        //j.createSocialPublishingFragment();
        //j.setAlwaysForceReauthentication();
        //j.setEnabledAuthenticationProviders();
        //j.setTokenUrl();
        //j.signoutUserForAllProviders();
    }

    /**
     * @return the configured Capture app domain
     */
    public static String getCaptureDomain() {
        return state.captureDomain;
    }

    /**
     * @return the configured Capture API client ID
     */
    public static String getCaptureClientId() {
        return state.captureClientId;
    }

    /**
     * @return the currently signed-in user, or null
     */
    public static JRCaptureRecord getSignedInUser() {
        //if (state.signedInUser == null && JREngage.getApplicationContext() != null) {
        //    state.signedInUser = JRCaptureRecord.loadFromDisk(JREngage.getApplicationContext());
        //}
        return state.signedInUser;
    }

    /**
     * Starts the Capture sign-in flow directly on a provider
     * @param fromActivity the activity from which to start the dialog activity
     * @param providerName the name of the provider to show the sign-in flow for. May be null.
     *                     If null, a list of providers (and a traditional sign-in form) is displayed to the
     *                     end-user.
     * @param handler your result handler, called upon completion on the UI thread
     */
    public static void showSignInDialog(Activity fromActivity, String providerName,
                                        SignInResultHandler handler) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(FailureReasons.jumpNotInitialized);
            return;
        }

        state.jrEngage.addDelegate(new JREngageDelegate.SimpleJREngageDelegate() {
            @Override
            public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
                String authInfoToken = auth_info.getAsString("token");

                //JRCapture.performSocialSignIn(authInfoToken, new JRCapture.FetchJsonCallback() {
                JRCapture.performLegacySocialSignIn(authInfoToken, new SignInResponseHandler() {
                    public void onSuccess(JRCaptureRecord record) {
                        state.signedInUser = record;
                        fireHandlerOnSuccess();
                    }

                    public void onFailure(Object error) {
                        fireHandlerOnFailure(error);
                    }
                });
            }

            @Override
            public void jrAuthenticationDidNotComplete() {
                fireHandlerOnFailure(FailureReasons.AuthenticationCanceledByUser);
            }

            @Override
            public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
                fireHandlerOnFailure(error);
            }

            @Override
            public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
                fireHandlerOnFailure(error);
            }
        });

        state.signInHandler = handler;
        state.jrEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);
    }

    /**
     * Signs the signed-in user out, and removes their record from disk.
     * @param applicationContext
     */
    public static void signOutCaptureUser(Context applicationContext) {
        state.signedInUser = null;
        JRCaptureRecord.deleteFromDisk(applicationContext);
    }

    /*package*/ static void fireHandlerOnFailure(Object failureParam) {
        SignInResultHandler handler_ = state.signInHandler;
        state.signInHandler = null;
        if (handler_ != null) handler_.onFailure(failureParam);

    }

    // Package level because of use from TradSignInUi:
    /*package*/ static void fireHandlerOnSuccess() {
        SignInResultHandler handler_ = state.signInHandler;
        state.signInHandler = null;
        if (handler_ != null) handler_.onSuccess();

    }

    public enum TraditionalSignInType { EMAIL, USERNAME }

    /**
     * Headless API for Capture traditional account sign-in
     * @param signInName the end user's user name or email address
     * @param password the end user's password
     * @param handler your callback handler, invoked upon completion in the UI thread
     */
    public static void performTraditionalSignIn(String signInName, String password,
                                                final SignInResultHandler handler) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(FailureReasons.jumpNotInitialized);
            return;
        }

        JRCapture.performTraditionalSignIn(signInName, password, state.traditionalSignInType,
                new SignInResponseHandler() {
            @Override
            public void onSuccess(JRCaptureRecord record) {
                state.signedInUser = record;
                handler.onSuccess();
            }

            @Override
            public void onFailure(Object error) {
                handler.onFailure(error);
            }
        });
    }

    public interface SignInResultHandler {
        public enum FailureReasons {
            invalidApiResponse, jumpNotInitialized, AuthenticationCanceledByUser
        }

        /**
         * Called when Capture sign-in has succeeded
         */
        void onSuccess();

        /**
         * Called when Capture sign-in has failed.
         * @param error
         */
        void onFailure(Object error);
    }

    /**
     * To be called from Application#onCreate()
     * @param context the application context, used to interact with the disk
     */
    public static void maybeLoadUserFromDisk(Context context) {
        state.signedInUser = JRCaptureRecord.loadFromDisk(context);
    }

    /**
     * To be called from Activity#onPause
     * @param context the application context, used to interact with the disk
     */
    public static void maybeSaveUserToDisk(Context context) {
        if (state.signedInUser != null) state.signedInUser.saveToDisk(context);
    }
}