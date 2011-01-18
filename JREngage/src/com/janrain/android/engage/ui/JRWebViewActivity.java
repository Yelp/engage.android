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
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.janrain.android.engage.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;
import android.widget.Toast;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.R;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.IOUtils;
import com.janrain.android.engage.utils.StringUtils;
import com.sun.tools.corba.se.idl.ExceptionEntry;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URL;
import java.util.ArrayList;

/**
 * Container for authentication web view.  Mimics JRWebViewController iPhone interface.
 */
public class JRWebViewActivity extends Activity
        implements DownloadListener, JRConnectionManagerDelegate {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    /**
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     */
    private class FinishReceiver extends BroadcastReceiver {

        private final String TAG = JRWebViewActivity.TAG + "-" + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
            if (JRWebViewActivity.class.toString().equals(target)) {
                tryToFinishActivity();
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

    /**
     * Handler for webkit events, etc.
     */
    private class JRWebViewClient extends WebViewClient {

        private final String TAG = JRWebViewClient.class.getSimpleName();

        /*
        NOTE:  This method gets called once, when WebView.loadUrl() is called.  It is not
               called for redirects, which would be really really nice...

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Config.LOGD) {
                Log.d(TAG, "[shouldOverrideUrlLoading] url: " + url);
            }
            view.loadUrl(url);
            return true;
        }
        */

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (Config.LOGD) {
                Log.d(TAG, "[onPageStarted] url: " + url);
            }

            mLayoutHelper.showProgressDialog();


            /*
             * TODO:  FINDME: FIXME:
             *
             * METHOD ONE:
             * Intercept the mobile endpoint URL here in onPageStarted and manually kick off the HTTP
             * POST operation to it.  We use the token value plucked from the HTML form found in the
             * cache.  Johnny Hacker, welcome to Screen-Scraping 101.
             */
            final String thatUrl = mSessionData.getBaseUrl() + "/signin/device";
            if ((!TextUtils.isEmpty(url)) && (url.startsWith(thatUrl))) {
                Log.d(TAG, "[onPageStarted] JREngage URL intercepted, loading data.");
                // stop the current load
                mWebView.stopLoading();
                // fire up the manual connection
                loadMobileEndpointUrl(url);
            } else {
                super.onPageStarted(view, url, favicon);
            }

            /*
             * TODO:  FINDME: FIXME:
             *
             * METHOD TWO:
             * The correct way to do it...provided it worked...or the server side is changed to a
             * HTTP GET mechanism.  Revisit once server-side is fixed.  Will probably need to pluck
             * data from the URL itself.
             */
//            final String thatUrl = mSessionData.getBaseUrl() + "/signin/device";
//            if ((!TextUtils.isEmpty(url)) && (url.startsWith(thatUrl))) {
//                Log.d(TAG, "[onPageStarted] looks like JR mobile endpoint url");
//            }
//
//            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (Config.LOGD) {
                Log.d(TAG, "[onPageFinished] url: " + url);
            }

            /*
             * TODO:  FINDME: FIXME:
             *
             * METHOD ONE:
             * Comb through the cache manager and see if we can dig up the HTML form with
             * the HTTP POST form containing the token.  Oh dirty dirty.
             */
//            CacheManager.CacheResult cacheResult = CacheManager.getCacheFile(url, null);
//            if (cacheResult != null) {
//                byte[] data = IOUtils.readFromStream(cacheResult.getInputStream());
//                if (data != null) {
//                    try {
//                        String sData = new String(data, cacheResult.getEncoding());
//                        Log.d(TAG, "[onPageFinished] data: " + sData);
////                        parsePostHtml(sData);
//                    } catch (Exception ignore) {
//                        // when this happens, the encoding is not specified (means the cached data
//                        // does not interest us).
//                    }
//                }
//            }

            if (!mIsMobileEndpointUrlLoading) {
                mLayoutHelper.dismissProgressDialog();
            }

            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                + " | url: " + url);

            mLayoutHelper.dismissProgressDialog();
            Toast.makeText(JRWebViewActivity.this, description, Toast.LENGTH_LONG).show();

            super.onReceivedError(view, errorCode, description, url);
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRWebViewActivity.class.getSimpleName();

    private static final String RPX_RESULT_TAG = "rpx_result";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private SharedLayoutHelper mLayoutHelper;
    private JRSessionData mSessionData;
    private WebView mWebView;
    private boolean mIsAlertShowing;
    private boolean mIsFinishPending;
    private FinishReceiver mFinishReceiver;
    private boolean mIsMobileEndpointUrlLoading;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public JRWebViewActivity() {
        mIsAlertShowing = false;
        mIsFinishPending = false;
        mIsMobileEndpointUrlLoading = false;
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState
     *      If the activity is being re-initialized after previously being shut down then this
     *      Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *      Note: Otherwise it is null.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_webview);

        CookieSyncManager.createInstance(this);

        mSessionData = JRSessionData.getInstance();
        JRProvider provider = mSessionData.getCurrentProvider();

        if (Config.LOGD) {
            Log.d(TAG, "[onCreate] provider: " + provider.getName());
        }

        mLayoutHelper = new SharedLayoutHelper(this);
        mLayoutHelper.setHeaderText(provider.getFriendlyName());
        mLayoutHelper.showProgressDialog();

        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.clearView();
        mWebView.setWebViewClient(new JRWebViewClient());
        mWebView.setDownloadListener(this);
        mWebView.setInitialScale(100);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportZoom(true);

        URL startUrl = mSessionData.startUrlForCurrentProvider();
        if (startUrl == null) {
            // PROBLEM
        } else {
            mWebView.loadUrl(startUrl.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLayoutHelper.dismissProgressDialog();
        unregisterReceiver(mFinishReceiver);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the shared menu
        mLayoutHelper.inflateAboutMenu(menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mLayoutHelper.handleAboutMenu(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Callback for creating dialogs that are managed.
     */
    protected Dialog onCreateDialog(int id) {
        return mLayoutHelper.onCreateDialog(id);
    }

    private void showAlertDialog(String title, String message) {
        mIsAlertShowing = true;
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mIsAlertShowing = false;
                    if (mIsFinishPending) {
                        mIsFinishPending = false;
                        finish();
                    }
                }
            })
            .show();
    }

    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            finish();
        }
    }

    private boolean isMobileEndpointUrl(String url) {
        final String thatUrl = mSessionData.getBaseUrl() + "/signin/device";
        return ((!TextUtils.isEmpty(url)) && (url.startsWith(thatUrl)));
    }

    private void loadMobileEndpointUrl(String url) {
        if (isMobileEndpointUrl(url)) {
            mIsMobileEndpointUrlLoading = true;

            mLayoutHelper.showProgressDialog();

            //byte[] bytes = buildPostDataBuffer();

            Log.d(TAG, "[loadMobileEndpointUrl] url: " + url);

            if (!JRConnectionManager.createConnection(
                    url + "&auth_info=true", JRWebViewActivity.this, false, RPX_RESULT_TAG)) {

                mLayoutHelper.dismissProgressDialog();
                // TODO:  handle error case

            }

            // return;
        }

        // TODO:  is windows live hack necessary here, as it is on iPhone?
    }

    ///

    /*
     * TODO:  FINDME: FIXME:
     *
     * Data and methods used by METHOD ONE hack-code above.
     */

    private String mToken;
    private String mDevice;

    private void parsePostHtml(String html) {
        if (!TextUtils.isEmpty(html) && html.contains("POST")) {
            String[] lines = html.split("\n");
            if ((lines != null) && (lines.length > 0)) {
                for (String line : lines) {
                    if (line.contains("<input")) {
                        if (line.contains("token")) {
                            mToken = extractInputTagValueAttribute(line);
                            Log.d(TAG, "[parsePostHtml] mToken: " + mToken);
                        } else if (line.contains("device")) {
                            mDevice = extractInputTagValueAttribute(line);
                            Log.d(TAG, "[parsePostHtml] mDevice: " + mDevice);
                        }
                    }
                }
            }
        }
    }

    private String extractInputTagValueAttribute(String s) {
        final String VALUE = "value=";
        int startIndex = s.indexOf(VALUE);
        if (startIndex > 0) {
            startIndex = (startIndex + VALUE.length() + 1);
            int endIndex = s.lastIndexOf("\"");
            if (endIndex > 0) {
                return s.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    private byte[] buildPostDataBuffer() {
        if (!TextUtils.isEmpty(mToken) && !TextUtils.isEmpty(mDevice)) {
            StringBuilder sb = new StringBuilder();
            sb.append("token=").append(mToken).append("&");
            sb.append("device=").append(mDevice);
            return sb.toString().getBytes();
        } else {
            Log.w(TAG, "[buildPostDataBuffer] token is empty, post data is therefore null.");
            Log.w(TAG, "[buildPostDataBuffer] authentication will likely fail.");
        }
        return null;
    }

    ///

    public void onDownloadStart(String url, String userAgent,
            String contentDisposition, String mimetype, long contentLength) {

        if (Config.LOGD) {
            Log.d(TAG, "[onDownloadStart] url: " + url);
            Log.d(TAG, "[onDownloadStart] userAgent: " + userAgent);
            Log.d(TAG, "[onDownloadStart] contentDisposition: " + contentDisposition);
            Log.d(TAG, "[onDownloadStart] mimetype: " + mimetype);
            Log.d(TAG, "[onDownloadStart] contentLength: " + contentLength);
        }

        /*
         * TODO: FINDME: FIXME:
         *
         * In METHOD ONE we are kicking off the mobile endpoint load from
         * JRWebViewClient.onPageStarted() instead.
         */
//        loadMobileEndpointUrl(url);
    }

    ////

    public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
        Log.d(TAG, "[connectionDidFinishLoading] userdata: " + userdata + " | payload: " + payload);

        mLayoutHelper.dismissProgressDialog();

        if ((userdata != null) && (userdata instanceof String)) {
            final String tag = (String)userdata;
            if (tag.equals(RPX_RESULT_TAG)) {
                JRDictionary payloadDictionary = JRDictionary.fromJSON(payload);
                JRDictionary resultDictionary = payloadDictionary.getAsDictionary("rpx_result");
                final String result = resultDictionary.getAsString("stat");
                if ("ok".equals(result)) {
                    // back button?
                    mSessionData.triggerAuthenticationDidCompleteWithPayload(payloadDictionary);
                } else {
                    final String error = resultDictionary.getAsString("error");
                    String alertTitle, alertMessage, logMessage;
                    if ("Discovery failed for the OpenID you entered".equals(error)) {
                        alertTitle = "Invalid Input";
                        alertMessage = (mSessionData.getCurrentProvider().requiresInput())
                                ? String.format("The %s you entered was not valid. Please try again.",
                                    mSessionData.getCurrentProvider().getShortText())
                                : "There was a problem authenticating with this provider. Please try again.";
                        logMessage = "Discovery failed for the OpenID you entered: ";

                        Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                        /*
                        TODO:  iPhone does this:
                        [[self navigationController] popViewControllerAnimated:YES];

                        Should we do this:
                        finish();
                         */
                        showAlertDialog(alertTitle, alertMessage);
                    } else if ("The URL you entered does not appear to be an OpenID".equals(error)) {
                        alertTitle = "Invalid Input";
                        alertMessage = (mSessionData.getCurrentProvider().requiresInput())
                                ? String.format("The %s you entered was not valid. Please try again.",
                                    mSessionData.getCurrentProvider().getShortText())
                                : "There was a problem authenticating with this provider. Please try again.";
                        logMessage = "The URL you entered does not appear to be an OpenID: ";

                        Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                        /*
                        TODO:  iPhone does this:
                        [[self navigationController] popViewControllerAnimated:YES];

                        Should we do this:
                        finish();
                         */
                        showAlertDialog(alertTitle, alertMessage);
                    } else if ("Please enter your OpenID".equals(error)) {
                        JREngageError err = new JREngageError(
                                "Authentication failed: " + payload,
                                JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                                JREngageError.ErrorType.AUTHENTICATION_FAILED);

                        mSessionData.triggerAuthenticationDidFail(err);
                    } else {
                        JREngageError err = new JREngageError(
                                "Authentication failed: " + payload,
                                JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                                JREngageError.ErrorType.AUTHENTICATION_FAILED);

                        showAlertDialog(
                                "Log In Failed",
                                "An error occurred while attempting to sign you in.  Please try again."
                            );

                        mSessionData.triggerAuthenticationDidFail(err);
                    }
                }

            } else if (tag.equals("request")) {
                // connectionDataAlreadyDownloadedThis???
                mWebView.loadDataWithBaseURL(requestUrl, payload, null, "utf-8", null);
            }
        }
    }

    public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload, String requestUrl, Object userdata) {
        Log.d(TAG, "[connectionDidFinishLoading-2] userdata: " + userdata + " | payload: "
                + StringUtils.decodeUtf8(payload, null));
        // Not used
    }

    public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
        Log.i(TAG, "[connectionDidFail] userdata: " + userdata, ex);

        if ((userdata != null) && (userdata instanceof String)) {
            final JREngageError error = new JREngageError(
                    "Authentication failed",
                    JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                    JREngageError.ErrorType.AUTHENTICATION_FAILED,
                    ex);

            final String tag = (String)userdata;
            if (tag.equals(RPX_RESULT_TAG)) {
                // Back button?
                mSessionData.triggerAuthenticationDidFail(error);
            } else if (tag.equals("request")) {
                // Back button?
                mSessionData.triggerAuthenticationDidFail(error);
            }
        }
    }

    public void connectionWasStopped(Object userdata) {
        Log.i(TAG, "[connectionWasStopped] userdata: " + userdata);
    }


}