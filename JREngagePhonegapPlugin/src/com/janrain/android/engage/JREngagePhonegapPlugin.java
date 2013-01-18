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

 Author: lillialexis
 Date:   12/28/11
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.janrain.android.engage;


import android.widget.Toast;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import org.apache.cordova.api.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.janrain.android.engage.JREngageError.ConfigurationError;

public class JREngagePhonegapPlugin extends CordovaPlugin implements JREngageDelegate {
    private static String TAG = "[JREngagePhonegapPlugin]";
    private JREngage mJREngage;

    private JRDictionary mFullAuthenticationResponse     = null;
    private JRDictionary mFullSharingResponse            = null;
    private ArrayList<JRDictionary> mAuthenticationBlobs = null;
    private ArrayList<JRDictionary> mShareBlobs          = null;

    private boolean mWeAreSharing = false;
    static int instantiationCount = 0;
    private CallbackContext mCallback;

    {
        if (instantiationCount > 0) {
            JREngage.logd(TAG, "More than one instance, instantiation count: " + instantiationCount);
        }
        instantiationCount++;
    }

    @Override
    public boolean execute(final String cmd, final JSONArray args,
                                        final CallbackContext callback) {
        mCallback = callback;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (cmd.equals("print")) {
                        showToast(args.getString(0));
                    } else if (cmd.equals("initializeJREngage")) {
                        String appId = args.get(0) == JSONObject.NULL ? null : args.getString(0);
                        String tokenUrl = args.get(1) == JSONObject.NULL ? null : args.getString(1);
                        initializeJREngage(appId, tokenUrl);
                    } else if (cmd.equals("showAuthenticationDialog")) {
                        showAuthenticationDialog();
                    } else if (cmd.equals("showSharingDialog")) {
                        showSharingDialog(args.getString(0));
                    } else { // TODO: Test these errors and verify that the single quotes are fine
                        postResultAndResetState(new PluginResult(PluginResult.Status.INVALID_ACTION,
                                buildFailureString(-1, "Unknown action: " + cmd)));
                    }
                } catch (JSONException e) {
                    postResultAndResetState(buildJsonFailureResult(-1, "Error parsing arguments for " + cmd));
                }
            }
        });

        return true;
    }

    private void saveTheAuthenticationBlobForLater() {
        if (mAuthenticationBlobs == null) 
            mAuthenticationBlobs = new ArrayList<JRDictionary>();

        mAuthenticationBlobs.add(mFullAuthenticationResponse);
        mFullAuthenticationResponse = null;
    }

    private void resetPluginState() {
        mWeAreSharing               = false;
        mFullAuthenticationResponse = null;
        mFullSharingResponse        = null;
        mAuthenticationBlobs        = null;
        mShareBlobs                 = null;
    }

    private void postResultAndResetState(PluginResult result) {
        resetPluginState();
        mCallback.sendPluginResult(result);
    }

    private PluginResult buildSuccessResult(JRDictionary successDictionary) {
        String message = successDictionary.toJson();

        JREngage.logd("[buildSuccessResult]", message);
        return new PluginResult(PluginResult.Status.OK, message);
    }

    private PluginResult buildJsonFailureResult(int code, String message) {
        return new PluginResult(PluginResult.Status.JSON_EXCEPTION, buildFailureString(code, message));
    }

    private PluginResult buildFailureResult(JREngageError error) {
        return buildFailureResult(error.getCode(), error.getMessage());
    }
    
    private PluginResult buildFailureResult(int code, String message) {
        return new PluginResult(PluginResult.Status.ERROR, buildFailureString(code, message));
    }
    
    private String buildFailureString(int code, String message) {
        JRDictionary errorDictionary = new JRDictionary();
        errorDictionary.put("code", code);
        errorDictionary.put("message", message);
        errorDictionary.put("stat", "fail");

        return errorDictionary.toJson();
    }

    private void showToast(final String message) {
        Toast myToast = Toast.makeText(cordova.getActivity(), message, Toast.LENGTH_SHORT);
        myToast.show();

        postResultAndResetState(new PluginResult(PluginResult.Status.OK, message));
    }

    private void initializeJREngage(String appId, String tokenUrl) {
        if (appId == null || appId.equals("")) {
            postResultAndResetState(buildFailureResult(ConfigurationError.MISSING_APP_ID_ERROR,
                    "Missing appId in call to initialize"));
            return;
        }

        try {
            mJREngage = JREngage.initInstance(cordova.getActivity(), appId, tokenUrl, this);
        } catch (IllegalArgumentException e) {
            postResultAndResetState(buildFailureResult(ConfigurationError.GENERIC_CONFIGURATION_ERROR,
                    "There was an error initializing JREngage: returned JREngage object was null"));
            return;
        }

        postResultAndResetState(new PluginResult(PluginResult.Status.OK,
                "{'stat':'ok','message':'Initializing JREngage...'}"));
    }

    private void showAuthenticationDialog() {
        mJREngage.showAuthenticationDialog(cordova.getActivity());
    }

    private void showSharingDialog(String activityString) {
        mWeAreSharing = true;

        if (activityString == null || activityString.equals("")) {
            postResultAndResetState(buildFailureResult(JREngageError.SocialPublishingError.ACTIVITY_NULL,
                    "Activity object is required and cannot be null"));
            return;
        }

        JRActivityObject activity;
        try {
            activity = new JRActivityObject(JRDictionary.fromJsonString(activityString));
        } catch (JSONException e) {
            postResultAndResetState(buildJsonFailureResult(JREngageError.SocialPublishingError.BAD_ACTIVITY_JSON,
                    "The JSON passed was not a valid activity object"));
            return;
        } catch (IllegalArgumentException ignore) {
            postResultAndResetState(buildJsonFailureResult(JREngageError.SocialPublishingError.BAD_ACTIVITY_JSON,
                    "The JSON passed was not a valid activity object"));
            return;
        }
        
        mJREngage.showSocialPublishingDialog(cordova.getActivity(), activity);
    }

    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        JREngage.logd(TAG, "[jrEngageDialogDidFailToShowWithError] " + error);
        postResultAndResetState(buildFailureResult(error));
    }

    /* Happens on user backing out of authentication, so report user cancellation */
    // TODO: Test this when sharing
    public void jrAuthenticationDidNotComplete() {
        JREngage.logd(TAG, "[jrAuthenticationDidNotComplete] User Canceled");
        postResultAndResetState(buildFailureResult(JREngageError.AuthenticationError.AUTHENTICATION_CANCELED,
                "User canceled authentication"));
    }

    public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        JREngage.logd(TAG, "[jrAuthenticationDidFailWithError] " + error);
        // TODO: Test this on Android (auth fails as well as sharing fails)
        if (!mWeAreSharing)
            postResultAndResetState(buildFailureResult(error));
    }

    public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                                   JREngageError error,
                                                                   String provider) {
        Log.e(TAG, "[jrAuthenticationCallToTokenUrlDidFail] " + error);
        if (!mWeAreSharing)
            postResultAndResetState(buildFailureResult(error));
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        JREngage.logd(TAG, "[jrAuthenticationDidSucceedForUser]");

        auth_info.remove("stat");

        mFullAuthenticationResponse = new JRDictionary();
        mFullAuthenticationResponse.put("auth_info", auth_info);
        mFullAuthenticationResponse.put("provider", provider);
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                              HttpResponseHeaders response,
                                                              String tokenUrlPayload,
                                                              String provider) {
        JREngage.logd(TAG, "[jrAuthenticationDidReachTokenUrl]");

        mFullAuthenticationResponse.put("tokenUrl", tokenUrl);
        mFullAuthenticationResponse.put("tokenUrlPayload", tokenUrlPayload);
        mFullAuthenticationResponse.put("stat", "ok");

        if (mWeAreSharing) saveTheAuthenticationBlobForLater();
        else postResultAndResetState(buildSuccessResult(mFullAuthenticationResponse));
    }


    public void jrSocialDidNotCompletePublishing() {
        JREngage.logd(TAG, "[jrSocialDidNotCompletePublishing] User Canceled");
        postResultAndResetState(buildFailureResult(JREngageError.SocialPublishingError.CANCELED_ERROR,
                "User canceled authentication"));
    }

    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                 JREngageError error,
                                                 String provider) {
        JREngage.logd(TAG, "[jrSocialPublishJRActivityDidFail]");
        JRDictionary shareBlob = new JRDictionary();

        shareBlob.put("provider", provider);
        shareBlob.put("stat", "fail");
        shareBlob.put("code", error.getCode());
        shareBlob.put("message", error.getMessage());

        if (mShareBlobs == null)
            mShareBlobs = new ArrayList<JRDictionary>();

        mShareBlobs.add(shareBlob);
    }

    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        JREngage.logd(TAG, "[jrSocialDidPublishJRActivity]");
        JRDictionary shareBlob = new JRDictionary();

        shareBlob.put("provider", provider);
        shareBlob.put("stat", "ok");

        if (mShareBlobs == null)
            mShareBlobs = new ArrayList<JRDictionary>();

        mShareBlobs.add(shareBlob);
    }

    public void jrSocialDidCompletePublishing() {
        JREngage.logd(TAG, "[jrSocialDidCompletePublishing]");
        if (mFullSharingResponse == null)
            mFullSharingResponse = new JRDictionary();

        if (mAuthenticationBlobs != null)
            mFullSharingResponse.put("signIns", mAuthenticationBlobs);

        if (mShareBlobs != null)
            mFullSharingResponse.put("shares", mShareBlobs);

        postResultAndResetState(buildSuccessResult(mFullSharingResponse));
    }
}




