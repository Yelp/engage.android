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


/**
 * Example of Android PhoneGap Plugin
 */

import java.io.File;


import android.widget.Toast;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
/**
 * PhoneGap plugin which can be involved in following manner from javascript
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
 * DirectoryListing.list("/sdcard",
 *			  successCallback
 *			  failureCallback);
 *
 * }
 * </pre>
 * @author Rohit Ghatol
 *
 */
public class JREngagePhoneGapWrapper extends Plugin implements JREngageDelegate {

    /** List Action */
    public static final String ACTION="list";

//    @Override
//    public PluginResult execute(String action, JSONArray data, String callbackId) {
//        Log.d("DirectoryListPlugin", "Plugin Called");
//        PluginResult result = null;
//        if (ACTION.equals(action)) {
//            try {
//                String fileName = data.getString(0);
//                JSONObject fileInfo = getDirectoryListing(new File(fileName));
//                Log.d("DirectoryListPlugin", "Returning "+ fileInfo.toString());
//                result = new PluginResult(Status.OK, fileInfo);
//            } catch (JSONException jsonEx) {
//                Log.d("DirectoryListPlugin", "Got JSON Exception "+ jsonEx.getMessage());
//                result = new PluginResult(Status.JSON_EXCEPTION);
//            }
//        }
//        else {
//            result = new PluginResult(Status.INVALID_ACTION);
//            Log.d("DirectoryListPlugin", "Invalid action : "+action+" passed");
//        }
//        return result;
//    }

//    /**
//     * Gets the Directory listing for file, in JSON format
//     * @param file The file for which we want to do directory listing
//     * @return JSONObject representation of directory list.
//     *  e.g {"filename":"/sdcard","isdir":true,"children":[{"filename":"a.txt","isdir":false},{..}]}
//     * @throws JSONException
//     */
//    private JSONObject getDirectoryListing(File file) throws JSONException {
//        JSONObject fileInfo = new JSONObject();
//        fileInfo.put("filename", file.getName());
//        fileInfo.put("isdir", file.isDirectory());
//        if (file.isDirectory()) {
//            JSONArray children = new JSONArray();
//            fileInfo.put("children", children);
//            if (null != file.listFiles()) {
//                for (File child : file.listFiles()) {
//                    children.put(getDirectoryListing(child));
//                }
//            }
//        }
//        return fileInfo;
//    }

    private JREngage mJREngage;
    private boolean mFinishedAuthentication;
    private PluginResult mResult;

    @Override
    public synchronized PluginResult execute(final String cmd, final JSONArray args, final String callback) {
        mFinishedAuthentication = false;
        try {
            ctx.runOnUiThread(new Runnable()
            {
                public void run() {
                    try {
                        if(cmd.equals("toast")) {
                            showToast(args.getString(0));
                        } else if(cmd.equals("initializeJREngage")) {
                            initializeJREngage(args.getString(0), args.getString(1));
                        } else if (cmd.equals("showAuthenticationDialog")) {
                            showAuthenticationDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.d("[JREngagePhoneGapWrapper]", "error: ", e);
        }

        // TODO: Add thread protection (got into weird infinite loop)
        while (!mFinishedAuthentication) {
            Log.d("[JREngagePhoneGapWrapper]", "mFinishedAuthentication = false");
            try {
                wait();
                //Thread.sleep(100, 0);
            } catch (InterruptedException e) {
                Log.d("[JREngagePhoneGapWrapper]", "error: ", e);
//                mResult = new PluginResult(Status.ERROR, "sleep error");
//                mFinishedAuthentication = true;
            }
        }

        Log.d("[JREngagePhoneGapWrapper]", "mFinishedAuthentication = true");

        return mResult; //new PluginResult(PluginResult.Status.OK, "yo yo");
    }

    private PluginResult showAuthenticationDialog() {
        mJREngage.showAuthenticationDialog();
        return new PluginResult(PluginResult.Status.OK, "yo yo");
    }

    private PluginResult initializeJREngage(String appId, String tokenUrl) {
        JREngage.sLoggingEnabled = true;
        mJREngage = JREngage.initInstance(ctx, appId, tokenUrl, this);
        return new PluginResult(PluginResult.Status.OK, "yo");
    }

    private PluginResult showToast(final String message) {
//        ctx.runOnUiThread(new Runnable()
//        {
//            public void run() {
                Toast myToast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
                myToast.show();
//            }
//        });

        return new PluginResult(PluginResult.Status.OK, message);
    }

    public synchronized void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        Log.d("[jrEngageDialogDidFailToShowWithError]", "ERROR");
        mResult = new PluginResult(Status.ERROR, "sleep error");
        mFinishedAuthentication = true;
        notifyAll();
    }

    public synchronized void jrAuthenticationDidNotComplete() {
        Log.d("[jrAuthenticationDidNotComplete]", "ERROR");
        mResult = new PluginResult(Status.ERROR, "sleep error");
        mFinishedAuthentication = true;
        notifyAll();
    }

    public synchronized void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        Log.d("[jrAuthenticationDidFailWithError]", "ERROR");
        mResult = new PluginResult(Status.ERROR, "sleep error");
        mFinishedAuthentication = true;
        notifyAll();
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        Log.d("[jrAuthenticationDidSucceedForUser]", "SUCCESS");
        //mResult = new PluginResult(Status.OK, "authentication");
    }

    public synchronized void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        Log.d("[jrAuthenticationCallToTokenUrlDidFail]", "ERROR");
        mResult = new PluginResult(Status.ERROR, "sleep error");
        mFinishedAuthentication = true;
        notifyAll();
    }

    public synchronized void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
        Log.d("[jrAuthenticationDidReachTokenUrl]", "SUCCESS");
        mResult = new PluginResult(Status.OK, "authentication");
        mFinishedAuthentication = true;
        notifyAll();
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




