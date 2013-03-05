/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2012, Janrain, Inc.
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

package com.janrain.android.capture;

import com.janrain.android.Jump;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import org.json.JSONObject;

import static android.text.TextUtils.join;
import static com.janrain.android.Jump.SignInResultHandler.FailureReasons.invalidApiResponse;
import static com.janrain.android.Jump.TraditionalSignInType.EMAIL;

public class JRCapture {
    private JRCapture() {}

    public static JRConnectionManagerDelegate performTraditionalSignIn(String username,
                                                                       String password,
                                                                       Jump.TraditionalSignInType type,
                                                                       FetchJsonCallback handler) {
        /**
         * client_id
         * locale
         * response_type
         * redirect_uri
         * an email address param
         * a password param
         * form
         * attributeUpdates
         */
        String signInNameAttrName = type == EMAIL ? "email" : "username";
        String url = "https://" + Jump.getCaptureDomain() + "/oauth/auth_native_traditional";
        CaptureApiConnection connection = new CaptureApiConnection(url);
        connection.addAllToParams("client_id", Jump.getCaptureClientId(),
                "locale", "en_US",
                "response_type", "token",
                "redirect_uri", "http://android.library",
                signInNameAttrName, username,
                "password", password,
                "form", "signin");
        return connection.fetchResponseAsJson(handler);
    }

    public static JRConnectionManagerDelegate performLegacyTraditionalSignIn(String username,
                                                                             String password,
                                                                             Jump.TraditionalSignInType type,
                                                                             FetchJsonCallback handler) {
        String url = "https://" + Jump.getCaptureDomain() + "/oauth/mobile_signin_username_password";
        CaptureApiConnection connection = new CaptureApiConnection(url);
        connection.addAllToParams("client_id", Jump.getCaptureClientId(),
                "redirect_uri", "http://android.library",
                "email", username,
                "password", password);
        return connection.fetchResponseAsJson(handler);
    }

    public static class InvalidApidChangeException extends Exception {
        public InvalidApidChangeException(String description) {
            super(description);
        }
    }

    public static interface RequestCallback {
        public void onSuccess();

        public void onFailure(Object e);
    }

    public static void performSocialSignIn(String authInfoToken, final FetchJsonCallback handler) {
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

        CaptureApiConnection c = new CaptureApiConnection("https://" + Jump.getCaptureDomain() + "/oauth/auth_native");
        c.addAllToParams("client_id", Jump.getCaptureClientId(),
                "locale", "en_US",
                "response_type", "token",
                "redirect_uri", "http://android-library",
                "token", authInfoToken,
                "thin_registration", "true");
        c.fetchResponseAsJson(handler);
    }

    public static void performLegacySocialSignIn(String authInfoToken, final FetchJsonCallback handler) {
        CaptureApiConnection c = new CaptureApiConnection("https://" + Jump.getCaptureDomain() + "/oauth/mobile_signin");
        c.addAllToParams("client_id", Jump.getCaptureClientId());
        c.addAllToParams("redirect_uri", "http://android-library");
        c.addAllToParams("token", authInfoToken);
        c.fetchResponseAsJson(handler);
    }

    public static abstract class SignInResponseHandler implements FetchJsonCallback {
        private boolean canceled = false;

        public void cancel() {
            canceled = true;
        }

        public void run(JSONObject response) {
            if (canceled) return;
            if (response == null) {
                onFailure(invalidApiResponse);
            } else if ("ok".equals(response.opt("stat"))) {
                Object user = response.opt("capture_user");
                if (user instanceof JSONObject) {
                    String accessToken = response.optString("access_token");
                    //String refreshSecret = response.optString("refresh_secret");
                    JRCaptureRecord record = new JRCaptureRecord(((JSONObject) user), accessToken, null);
                    onSuccess(record);
                } else {
                    onFailure(invalidApiResponse);
                }
            } else {
                onFailure(response);
            }
        }

        public abstract void onSuccess(JRCaptureRecord record);
        public abstract void onFailure(Object error);
    }

    public interface FetchJsonCallback {
        void run(JSONObject jsonObject);
    }

    public interface FetchCallback {
        void run(Object response);
    }
}

