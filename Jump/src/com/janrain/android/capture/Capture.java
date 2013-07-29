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
     * @param username The username (or email address).
     * @param password The password
     * @param type The type of traditional sign-in, which determines the key-field used to sign-in.
     * @param handler a call-back handler.
     * @return a connection handle
     */
    public static CaptureApiConnection performTraditionalSignIn(String username,
                                                                String password,
                                                                TraditionalSignInType type,
                                                                SignInResultHandler handler,
                                                                String mergeToken) {

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
        connection.maybeAddParam("merge_token", mergeToken);
        connection.fetchResponseAsJson(handler);
        return connection;
    }

    /**
     * @deprecated
     */
    public static CaptureApiConnection performTraditionalSignIn(String username,
                                                                String password,
                                                                TraditionalSignInType type,
                                                                SignInResultHandler handler) {
        return performTraditionalSignIn(username, password, type, handler, null);
    }

    /**
     * Indicates a change was made to the JSON record which cannot be effected on Capture.
     * Currently, this is always the result of changes which violate the record schema.
     */
    public static class InvalidApidChangeException extends Exception {
        /*package*/ InvalidApidChangeException(String description) {
            super(description);
        }
    }

    /**
     * An interface used to communicate Capture API request results, or errors.
     */
    public static interface CaptureApiRequestCallback {
        /**
         * Called on successful API request
         */
        public void onSuccess();

        /**
         * Called on occurrence of error
         * @param e the error which occurred
         */
        public void onFailure(CaptureApiError e);
    }

    /**
     * Performs a social sign-in to Capture with the given Engage auth_info token
     *
     * @param authInfoToken an Engage auth_info token (the Engage instance for which the auth_info token is
     *                      value must be properly configured with the Capture instance which the JUMP library
     *                      has been configured with.
     * @param handler a sign-in result handler.
     * @param identityProvider the identity provider that the authInfoToken is valid for
     * @param mergeToken the merge token for this sign-in, if any
     */
    public static CaptureApiConnection performSocialSignIn(String authInfoToken,
                                                           final SignInResultHandler handler,
                                                           String identityProvider, String mergeToken) {
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
        handler.authenticationToken = authInfoToken;
        handler.identityProvider = identityProvider;
        CaptureApiConnection c =
                new CaptureApiConnection("https://" + getCaptureDomain() + "/oauth/auth_native");
        c.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", "token",
                "redirect_uri", "http://android-library",
                "token", authInfoToken,
                "thin_registration", "true"
        );
        c.maybeAddParam("merge_token", mergeToken);
        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     * @deprecated
     */
    public static CaptureApiConnection performSocialSignIn(String authInfoToken,
                                                           final SignInResultHandler handler) {
        return performSocialSignIn(authInfoToken, handler, null, null);
    }


    /**
     * Subclass this to
     */
    public static abstract class SignInResultHandler implements FetchJsonCallback {
        private boolean canceled = false;
        private String authenticationToken;
        private String identityProvider;

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
                onFailure(new CaptureApiError(response, authenticationToken, identityProvider));
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

