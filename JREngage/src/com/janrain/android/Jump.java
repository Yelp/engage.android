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
import android.util.Pair;
import com.janrain.android.capture.CaptureJsonUtils;
import com.janrain.android.capture.Connection;
import com.janrain.android.capture.JRCapture;
import com.janrain.android.capture.JRCaptureRecord;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRDictionary;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Jump {
    private enum State {
        STATE();

        private JRCaptureRecord signedInUser;
        private JREngage jrEngage;
        private String captureDomain;
        private String captureClientId;
    }

    private static final String TAG = Jump.class.getSimpleName();
    private static final State state = State.STATE;

    public static void init(Context context, String engageAppId, String captureDomain,
                            String captureClientId) {
        state.jrEngage = JREngage.initInstance(context.getApplicationContext(), engageAppId, null, null);
        state.captureDomain = captureDomain;
        state.captureClientId = captureClientId;
        //j.showAuthenticationDialog();
        //j.addDelegate(); remove
        //j.cancelAuthentication();
        //j.createSocialPublishingFragment();
        //j.setAlwaysForceReauthentication();
        //j.setEnabledAuthenticationProviders();
        //j.setTokenUrl();
        //j.signoutUserForAllProviders();
    }

    public static String getCaptureDomain() {
        return state.captureDomain;
    }

    public static String getCaptureClientId() {
        return state.captureClientId;
    }

    public static JRCaptureRecord getSignedInUser() {
        if (state.signedInUser == null && JREngage.getApplicationContext() != null) {
            state.signedInUser = JRCaptureRecord.loadFromDisk(JREngage.getApplicationContext());
        }
        return state.signedInUser;
    }

    public static void showSignInDialog(Activity fromActivity, final SignInResultHandler handler) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(SignInResultHandler.FailureReasons.jumpNotInitialized);
            return;
        }

        state.jrEngage.addDelegate(new JREngageDelegate.SimpleJREngageDelegate() {
            @Override
            public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
                /***
                 * client_id
                 * locale
                 * response_type
                 * redirect_uri
                 * token
                 * attributeUpdates
                 * thin_registration
                 * flow_name
                 */
                String authInfoToken = auth_info.getAsString("token");

                JRCapture.performSocialSignIn(authInfoToken, new JRCapture.FetchJsonCallback() {
                    public void run(JSONObject response) {
                        if (response == null) {
                            handler.onFailure(Jump.SignInResultHandler.FailureReasons.invalidApiResponse);
                            return;
                        }
                        if ("ok".equals(response.opt("stat"))) {
                            Object user = response.opt("capture_user");
                            if (user instanceof JSONObject) {
                                state.signedInUser = new JRCaptureRecord(((JSONObject) user));
                                handler.onSuccess();
                            } else {
                                handler.onFailure(SignInResultHandler.FailureReasons.invalidApiResponse);
                            }
                        } else {
                            handler.onFailure(response);
                        }
                    }
                });

            }

            @Override
            public void jrAuthenticationDidNotComplete() {
                handler.onFailure(SignInResultHandler.FailureReasons.AuthenticationCanceledByUser);
            }

            @Override
            public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
                handler.onFailure(error);
            }

            @Override
            public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
                handler.onFailure(error);
            }
        });

        state.jrEngage.showAuthenticationDialog(fromActivity, tradSignInUi);
    }

    public enum TraditionalSignInType { EMAIL, USERNAME }

    public void performTraditionalSignIn(String username, String password, TraditionalSignInType type,
                                         SignInResultHandler handler) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(SignInResultHandler.FailureReasons.jumpNotInitialized);
            return;
        }

        String url = "https://" + state.captureDomain + "/oauth/auth_native_traditional";
    }

    public interface SignInResultHandler {
        public enum FailureReasons {
            invalidApiResponse, jumpNotInitialized, AuthenticationCanceledByUser
        }

        void onSuccess();
        void onFailure(Object error);
    }
}