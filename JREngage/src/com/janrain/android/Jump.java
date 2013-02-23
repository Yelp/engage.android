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
import com.janrain.android.capture.JRCapture;
import com.janrain.android.capture.JRCaptureRecord;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.types.JRDictionary;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

public class Jump {
    private enum State {
        STATE();

        private JRCaptureRecord loggedInUser;
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
                 */
                String authInfoToken = auth_info.getAsString("token");

                Connection c = new Connection("https://" + state.captureDomain + "/oauth/auth_native");
                c.setParams("client_id", state.captureClientId,
                        "locale", "en-US",
                        "response_type", "token",
                        "redirect_uri", "http://none",
                        "token", authInfoToken,
                        "thin_registration", "true");
                JSONObject response = c.fetchResponseAsJson();
                if (response == null) {
                    handler.onFailure(SignInResultHandler.FailureReasons.invalidApiResponse);
                    return;
                }
                if ("ok".equals(response.opt("stat"))) {
                    Object user = response.opt("capture_user");
                    if (user instanceof JSONObject) {
                        state.loggedInUser = new JRCaptureRecord(((JSONObject) user));
                    } else {
                        handler.onFailure(SignInResultHandler.FailureReasons.invalidApiResponse);
                    }
                } else {
                    handler.onFailure(response);
                }
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

        state.jrEngage.showAuthenticationDialog(fromActivity);
    }

    public interface SignInResultHandler {
        public enum FailureReasons {
            invalidApiResponse, jumpNotInitialized, AuthenticationCanceledByUser
        }

        void onSuccess();
        void onFailure(Object error);
    }
}

/*package*/ class Connection {
    String url;
    Set<Pair<String,String>> params = new HashSet<Pair<String, String>>();

    public Connection(String url) {
        this.url = url;
    }

    public void setParams(String... params) {
        for (int i=0; i<params.length-1; i+=2) {
            this.params.add(new Pair<String, String>(params[i], params[i+1]));
        }
    }

    public JSONObject fetchResponseAsJson() {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setDoOutput(true);
            JRCapture.writePostParams(c, params);
            Object response = CaptureJsonUtils.urlConnectionGetJsonContent(c);
            if (response instanceof JSONObject) return (JSONObject) response;
            JREngage.logd("response: " + response);
            return null;
        } catch (MalformedURLException e) {
            JREngage.logd(e.toString());
            return null;
        } catch (IOException e) {
            JREngage.logd(e.toString());
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }
}