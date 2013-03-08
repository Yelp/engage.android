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
import org.json.JSONObject;

import static com.janrain.android.Jump.TraditionalSignInType;
import static com.janrain.android.Jump.TraditionalSignInType.EMAIL;
import static com.janrain.android.Jump.getCaptureClientId;
import static com.janrain.android.Jump.getCaptureDomain;

public class Capture {
    private Capture() {}

    /**
     * @param username
     * @param password
     * @param type
     * @param handler
     * @return
     */
    public static CaptureApiConnection performTraditionalSignIn(String username,
                                                                String password,
                                                                TraditionalSignInType type,
                                                                SignInRequestHandler handler) {
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
        String url = "https://" + getCaptureDomain() + "/oauth/auth_native_traditional";
        CaptureApiConnection connection = new CaptureApiConnection(url);
        connection.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", "token",
                "redirect_uri", "http://android.library",
                signInNameAttrName, username,
                "password", password,
                "form", Jump.getCaptureFormName());
        connection.fetchResponseAsJson(handler);
        return connection;
    }

    /**
     *
     */
    public static class InvalidApidChangeException extends Exception {
        /**
         *
         * @param description
         */
        public InvalidApidChangeException(String description) {
            super(description);
        }
    }

    /**
     *
     */
    public static interface CaptureApiRequestCallback {
        /**
         *
         */
        public void onSuccess();

        /**
         *
         * @param e
         */
        public void onFailure(CaptureApiError e);
    }

    /**
     *
     * @param authInfoToken
     * @param handler
     */
    public static CaptureApiConnection performSocialSignIn(String authInfoToken,
                                                           final SignInRequestHandler handler) {
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

        CaptureApiConnection c =
                new CaptureApiConnection("https://" + getCaptureDomain() + "/oauth/auth_native");
        c.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", "token",
                "redirect_uri", "http://android-library",
                "token", authInfoToken,
                "thin_registration", "true"
        );
        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     *
     */
    public static abstract class SignInRequestHandler implements FetchJsonCallback {
        private boolean canceled = false;

        public void cancel() {
            canceled = true;
        }

        public final void run(JSONObject response) {
            if (canceled) return;
            if (response == null) {
                onFailure(CaptureApiError.INVALID_API_RESPONSE);
            } else if ("ok".equals(response.opt("stat"))) {
                Object user = response.opt("capture_user");
                if (user instanceof JSONObject) {
                    String accessToken = response.optString("access_token");
                    //String refreshSecret = response.optString("refresh_secret");
                    CaptureRecord record = new CaptureRecord(((JSONObject) user), accessToken, null);
                    onSuccess(record);
                } else {
                    onFailure(CaptureApiError.INVALID_API_RESPONSE);
                }
            } else {
                onFailure(new CaptureApiError(response));
            }
        }

        /**
         *
         * @param record
         */
        public abstract void onSuccess(CaptureRecord record);

        /**
         *
         * @param error
         */
        public abstract void onFailure(CaptureApiError error);
    }

    /*package*/ interface FetchJsonCallback {
        void run(JSONObject jsonObject);
    }

    /*package*/ interface FetchCallback {
        void run(Object response);
    }
}

