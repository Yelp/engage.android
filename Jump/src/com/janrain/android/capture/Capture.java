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
import com.janrain.android.utils.ApiConnection;
import org.json.JSONObject;

import static com.janrain.android.Jump.TraditionalSignInType;
import static com.janrain.android.Jump.TraditionalSignInType.EMAIL;
import static com.janrain.android.Jump.getCaptureClientId;
import static com.janrain.android.utils.LogUtils.throwDebugException;

/**
 * This class implements Capture operations
 * It's not meant to be used directly, but rather through com.janrain.android.Jump
 */
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
        String signInNameAttrName = type == EMAIL ? "email" : "username";
        CaptureApiConnection connection = new CaptureApiConnection("/oauth/auth_native_traditional");
        connection.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", "token",
                "redirect_uri", Jump.getRedirectUri(),
                signInNameAttrName, username,
                "password", password,
                "form", Jump.getCaptureTraditionalSignInFormName());
        connection.maybeAddParam("merge_token", mergeToken);
        connection.maybeAddParam("bp_channel", Jump.getBackplaneChannelUrl());
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
        handler.authenticationToken = authInfoToken;
        handler.identityProvider = identityProvider;
        CaptureApiConnection c = new CaptureApiConnection("/oauth/auth_native");
        c.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", "token",
                "redirect_uri", Jump.getRedirectUri(),
                "token", authInfoToken,
                "thin_registration", String.valueOf(Jump.getCaptureEnableThinRegistration())
        );

        c.maybeAddParam("flow_version", Jump.getCaptureFlowVersion());
        c.maybeAddParam("flow", Jump.getCaptureFlowName());
        c.maybeAddParam("registration_form", Jump.getCaptureSocialRegistrationFormName());
        c.maybeAddParam("bp_channel", Jump.getBackplaneChannelUrl());
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
     * Registers a new user
     *
     * @param newUser the user record to fill the form fields for the registration form with.
     * @param socialRegistrationToken the social registration token, or null to perform a tradtional
     *                                registration
     * @param handler a sign-in result handler. (Successful registrations are also sign-ins.)
     *
     */
    public static CaptureApiConnection performRegistration(JSONObject newUser,
                                                           String socialRegistrationToken,
                                                           final SignInResultHandler handler) {
        if (newUser == null) throwDebugException(new IllegalArgumentException("null newUser"));

        // need to download flow
        // then translate object to form fields
        // then submit form

        String registrationForm = socialRegistrationToken != null ?
                Jump.getCaptureSocialRegistrationFormName() :
                Jump.getCaptureTraditionalRegistrationFormName();

        String url = socialRegistrationToken != null ? "/oauth/register_native" :
                "/oauth/register_native_traditional";

        CaptureApiConnection c = new CaptureApiConnection(url);

        c.addAllToParams(CaptureFlowUtils.getFormFields(newUser, registrationForm, Jump.getCaptureFlow()));

        //NSString *refreshSecret = [JRCaptureData generateAndStoreRefreshSecret];
        //if (!refreshSecret)
        //{
        //    [JRCapture maybeDispatch:@selector(registerUserDidFailWithError:) forDelegate:delegate
        //    withArg:[JRCaptureError invalidInternalStateErrorWithDescription:@"unable to generate secure "
        //    "random refresh secret"]];
        //    return;
        //}

        c.addAllToParams(
                "client_id", Jump.getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", Jump.getResponseType(),
                "redirect_uri", Jump.getRedirectUri(),
                "flow", Jump.getCaptureFlowName(),
                "form", registrationForm
                //"refresh_secret", refreshSecret
                );

        c.maybeAddParam("bp_channel", Jump.getBackplaneChannelUrl());
        c.maybeAddParam("flow_version", CaptureFlowUtils.getFlowVersion(Jump.getCaptureFlow()));
        c.maybeAddParam("token", socialRegistrationToken);

        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     * @internal
     */
    public static abstract class SignInResultHandler implements ApiConnection.FetchJsonCallback {
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

        public abstract void onSuccess(CaptureRecord record);

        public abstract void onFailure(CaptureApiError error);
    }
}

