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
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
/**
 * Phonegap plugin for authenticating with Janrain Engage
 * <p>
 * result example - {"filename":"/sdcard","isdir":true,"children":[{"filename":"a.txt","isdir":false},{..}]}
 * </p>
 * <pre>
 * {@code
 * successCallback = function(result){
 *     //result is a json
 *
 * }
 * failureCallback = function(error){
 *     //error is error message
 * }
 *
 * </pre>
 * @author Lilli Szafranski and Nathan Ramsey
 *
 */
public class JREngagePhonegapPlugin extends Plugin implements JREngageDelegate {
    private static String TAG = "[JREngagePhonegapPlugin]";
    private JREngage mJREngage;
    private boolean mWaitingForLibrary;
    private JRDictionary mStoredAuthInfo;
    private PluginResult mResult;

    @Override
    public synchronized PluginResult execute(final String cmd, final JSONArray args, final String callback) {
        mWaitingForLibrary = true;
        ctx.runOnUiThread(new Runnable() { public void run() {
            try {
                if (cmd.equals("toast")) {
                    showToast(args.getString(0));
                } else if (cmd.equals("initializeJREngage")) {
                    initializeJREngage(args.getString(0), args.getString(1));
                } else if (cmd.equals("showAuthenticationDialog")) {
                    showAuthenticationDialog();
                }
            } catch (JSONException e) {
                postResult(new PluginResult(Status.JSON_EXCEPTION, "Error parsing arguments for " +
                        cmd));
            }
        } });

        // TODO: Add thread protection (got into weird infinite loop)
        while (mWaitingForLibrary) {
            Log.d("[JREngagePhoneGapWrapper]", "mWaitingForLibrary = false");
            try {
                wait();
            } catch (InterruptedException e) {
                /* No exceptions are expected */
                Log.e(TAG, "Interrupted exception: ", e);
                return new PluginResult(Status.ERROR, "Unexpected InterruptedException: " + e);
            }
        }

        Log.d(TAG, "[JREngagePhoneGapWrapper] mWaitingForLibrary = false");

        return mResult;
    }

    private synchronized void postResult(PluginResult result) {
        mResult = result;
        mWaitingForLibrary = false;
        notifyAll();
    }

    private synchronized void showToast(final String message) {
        Toast myToast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        myToast.show();

        postResult(new PluginResult(PluginResult.Status.OK, message));
    }

    private synchronized void showAuthenticationDialog() {
        mJREngage.showAuthenticationDialog();
        postResult(new PluginResult(Status.OK));
    }

    private synchronized void initializeJREngage(String appId, String tokenUrl) {
        JREngage.sLoggingEnabled = true;
        mJREngage = JREngage.initInstance(ctx, appId, tokenUrl, this);
        postResult(new PluginResult(PluginResult.Status.OK));
    }

    public synchronized void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        Log.d(TAG, "[jrEngageDialogDidFailToShowWithError] ERROR");
        postResult(new PluginResult(Status.ERROR, "sleep error1"));
    }

    public synchronized void jrAuthenticationDidNotComplete() {
        Log.d(TAG, "[jrAuthenticationDidNotComplete] ERROR");
        postResult(new PluginResult(Status.ERROR, "sleep error2"));
    }

    public synchronized void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        Log.d(TAG, "[jrAuthenticationDidFailWithError] ERROR");
        postResult(new PluginResult(Status.ERROR, "sleep error3"));
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        Log.d(TAG, "[jrAuthenticationDidSucceedForUser] SUCCESS");
        mStoredAuthInfo = auth_info;
    }

    public synchronized void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                                   JREngageError error,
                                                                   String provider) {
        Log.d(TAG, "[jrAuthenticationCallToTokenUrlDidFail] ERROR");
        postResult(new PluginResult(Status.ERROR, "sleep error"));
    }

    public synchronized void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                              HttpResponseHeaders response,
                                                              String tokenUrlPayload,
                                                              String provider) {
        Log.d(TAG, "[jrAuthenticationDidReachTokenUrl] SUCCESS");
        postResult(new PluginResult(Status.OK, "authentication"));
    }

    public void jrSocialDidNotCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}




