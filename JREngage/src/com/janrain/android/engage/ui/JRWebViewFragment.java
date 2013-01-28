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
package com.janrain.android.engage.ui;

import java.net.URL;
import java.util.List;

import android.app.Dialog;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.webkit.ConsoleMessage;
import android.widget.FrameLayout;
import android.os.Handler;
import com.janrain.android.engage.utils.ThreadUtils;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.R;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.AndroidUtils;

/**
 * @internal
 *
 * @class JRWebViewActivity
 * Container for authentication web view.
 */
public class JRWebViewFragment extends JRUiFragment {
    private static final String KEY_DIALOG_TITLE = "jr_dialog_title";
    private static final String KEY_DIALOG_MESSAGE = "jr_dialog_message";
    private static final String KEY_PROVIDER_NAME = "jr_saved_provider_name";
    private static final String KEY_IS_ALERT_SHOWING = "mIsAlertShowing";
    private static final String KEY_IS_FINISH_PENDING = "mIsFinishPending";
    private static final String KEY_IS_LOADING_MOBILE_ENDPOINT = "mIsLoadingMobileEndpoint";
    private static final String KEY_IS_SPINNER_ON = "jr_spinner_on";
    private static final String KEY_CURRENTLY_LOADING_WEBVIEW_URL = "jr_current_webview_url";
    private static final String JR_RETAIN = "jr_retain_frag";
    private static final int KEY_ALERT_DIALOG = 1;

    public static final int RESULT_RESTART = Activity.RESULT_FIRST_USER;
    //public static final int RESULT_FAIL_AND_RESTART = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_BAD_OPENID_URL = Activity.RESULT_FIRST_USER + 2;
    public static final int RESULT_FAIL_AND_STOP = Activity.RESULT_FIRST_USER + 3;

    private WebView mWebView;
    private boolean mIsAlertShowing = false;
    private boolean mIsFinishPending = false;
    private boolean mIsLoadingMobileEndpoint = false;
    private String mCurrentlyLoadingUrl;
    //private boolean mUseDesktopUa = false;
    private JRProvider mProvider;
    private WebSettings mWebViewSettings;
    private ProgressBar mProgressSpinner;
    private RetainFragment mRetain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        JREngage.logd(TAG, "[onCreateView]");
        if (mSession == null) return null;
        //StrictMode.ThreadPolicy tp = StrictMode.getThreadPolicy();
        //StrictMode.allowThreadDiskReads();
        //StrictMode.allowThreadDiskWrites();
        View view = inflater.inflate(R.layout.jr_provider_webview, container, false);
        //StrictMode.setThreadPolicy(tp);

        mWebView = (WebView)view.findViewById(R.id.jr_webview);
        mProgressSpinner = (ProgressBar)view.findViewById(R.id.jr_webview_progress);

        mWebViewSettings = mWebView.getSettings();

        // Shim some information about the OS version into the WebView for use by hax ala Yahoo:
        mWebView.addJavascriptInterface(new Object() {
        			// These functions may be invoked via the javascript binding, but they are
        			// never invoked from this Java code, so they will always generate compiler
        			// warnings, so those warnings are suppressed safely.
                    @SuppressWarnings("unused")
					String getAndroidIncremental() {
                        return Build.VERSION.INCREMENTAL;
                    }

                    @SuppressWarnings("unused")
					String getAndroidRelease() {
                        return Build.VERSION.RELEASE;
                    }

                    @SuppressWarnings("unused")
					int getAndroidSdkInt() {
                        return AndroidUtils.SDK_INT;
                    }
                }, "jrengage_mobile");

        ensureWebViewSettings(mWebViewSettings);

        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setDownloadListener(mWebViewDownloadListener);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(KEY_IS_SPINNER_ON)) {
                showProgressSpinner();
            } else {
                hideProgressSpinner();
            }
        }

        return view;
    }

    private void ensureWebViewSettings(WebSettings webViewSettings) {
        //webViewSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) " +
        //        "AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.142 Safari/535.19");
        webViewSettings.setSavePassword(false);
        webViewSettings.setSupportMultipleWindows(true);
        webViewSettings.setBuiltInZoomControls(true);
        webViewSettings.setLoadsImagesAutomatically(true);
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSettings.setSupportZoom(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mSession == null) return;

        if (getArguments().getInt(JR_FRAGMENT_FLOW_MODE) == JR_FRAGMENT_FLOW_BETA_DIRECT_SHARE) {
            mWebView.loadUrl("http://nathan.janrain.com/~nathan/share_widget_webview/beta_share.html");
            mWebView.loadUrl("javascript:jrengage_beta_share_activity = " +
                    getArguments().getString(JR_ACTIVITY_JSON));
            String jsUrl = "http://rpxnow.com/js/lib/phonetell-dev/share_beta.js";
            mWebView.loadUrl("javascript:jrengage_beta_share_js_url = '" + jsUrl + "'");
            //String weinreUrl = "http://10.0.1.109:8080/target/target-script-min.js#anonymous";
            //mWebView.loadUrl("javascript:weinreUrl = '" + weinreUrl + "';");

            JREngage.logd(TAG, "returning from onActivityCreated early due to beta share widget flow mode");
            return;
        }

        mProvider = mSession.getCurrentlyAuthenticatingProvider();
        if (mProvider == null) {
            Log.e(TAG, "[onActivityCreated] null provider, bailing out");
            return;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_PROVIDER_NAME)) {
            mProvider = mSession.getProviderByName(savedInstanceState.getString(KEY_PROVIDER_NAME));
            mIsAlertShowing = savedInstanceState.getBoolean(KEY_IS_ALERT_SHOWING);
            mIsFinishPending = savedInstanceState.getBoolean(KEY_IS_FINISH_PENDING);
            mIsLoadingMobileEndpoint = savedInstanceState.getBoolean(KEY_IS_LOADING_MOBILE_ENDPOINT);
            String currentUrl = savedInstanceState.getString(KEY_CURRENTLY_LOADING_WEBVIEW_URL);
            configureWebViewUa();
            mWebView.restoreState(savedInstanceState);
            if (currentUrl != null) mWebView.loadUrl(currentUrl);
        } else {
            mProvider = mSession.getCurrentlyAuthenticatingProvider();
            configureWebViewUa();
            final Handler uiThread = new Handler();
            ThreadUtils.executeInBg(new Runnable() {
                public void run() {
                    final URL startUrl = mSession.startUrlForCurrentlyAuthenticatingProvider();
                    uiThread.post(new Runnable() {
                        public void run() {
                            mWebView.loadUrl(startUrl.toString());
                        }
                    });
                }
            });
        }

        FragmentManager fm = getActivity().getSupportFragmentManager();
        mRetain = (RetainFragment) fm.findFragmentByTag(JR_RETAIN);
        if (mRetain == null) {
            mRetain = new RetainFragment();
            mRetain.setTargetFragment(this, 0);
            fm.beginTransaction().add(mRetain, JR_RETAIN).commit();
        }
    }

    private void configureWebViewUa() {
        String customUa = mProvider.getWebViewOptions().getAsString(JRDictionary.KEY_USER_AGENT);
        //if (mUseDesktopUa) mWebViewSettings.setUserAgentString(getString(R.string.jr_desktop_browser_ua));
        if (customUa != null) mWebViewSettings.setUserAgentString(customUa);
    }

    @Override
    public void onStart() {
        super.onStart();
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        if (mCurrentlyLoadingUrl != null) mWebView.loadUrl(mCurrentlyLoadingUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        // onDestroyView may be called even if onCreateView never is, guard against NPEs
        if (mWebView != null) {
            // The WebView is stopped because otherwise it sometimes fires errant intent picker dialogs :/
            mWebView.stopLoading();

            // This listener's callback assumes the activity is running, but if the user presses
            // the back button while the WebView is transitioning between pages the activity may
            // not be shown when this listener is fired, which would cause a crash, so we unset
            // the listener here.
            mWebView.setWebViewClient(null);
            mWebView.setDownloadListener(null);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mWebView.destroy();

        if (mRetain != null) JRConnectionManager.stopConnectionsForDelegate(mRetain.mConnectionDelegate);
    }

    @Override
    /*package*/ void finishFragment() {
        if (mRetain != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(mRetain).commit();
        }
        super.finishFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mProvider != null) outState.putString(KEY_PROVIDER_NAME, mProvider.getName());
        outState.putBoolean(KEY_IS_ALERT_SHOWING, mIsAlertShowing);
        outState.putBoolean(KEY_IS_FINISH_PENDING, mIsFinishPending);
        outState.putBoolean(KEY_IS_LOADING_MOBILE_ENDPOINT, mIsLoadingMobileEndpoint);
        outState.putBoolean(KEY_IS_SPINNER_ON, mProgressSpinner.getVisibility() == View.VISIBLE);
        outState.putString(KEY_CURRENTLY_LOADING_WEBVIEW_URL, mCurrentlyLoadingUrl);
        mWebView.saveState(outState);

        super.onSaveInstanceState(outState);
    }

    private void showAlertDialog(String title, String message) {
        Bundle options = new Bundle();
        options.putString(KEY_DIALOG_TITLE, title);
        options.putString(KEY_DIALOG_MESSAGE, message);
        showDialog(KEY_ALERT_DIALOG, options);
        mIsAlertShowing = true;
    }

    @Override
    /*package*/ Dialog onCreateDialog(int id, Bundle options) {
        if (id == KEY_ALERT_DIALOG) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(options.getString(KEY_DIALOG_TITLE))
                    .setMessage(options.getString(KEY_DIALOG_MESSAGE))
                    .setPositiveButton(getString(R.string.jr_dialog_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mIsAlertShowing = false;
                            if (mIsFinishPending) {
                                mIsFinishPending = false;
                                tryToFinishFragment();
                            }
                        }
                    }).create();
        }
        
        return super.onCreateDialog(id, options);
    }

    @Override
    /*package*/ void onPrepareDialog(int id, Dialog d, Bundle options) {
        if (id == KEY_ALERT_DIALOG) {
            d.setTitle(options.getString(KEY_DIALOG_TITLE));
            ((AlertDialog) d).setMessage(options.getString(KEY_DIALOG_MESSAGE));
            return;
        }

        super.onPrepareDialog(id, d, options);
    }

    @Override
    /*package*/ void tryToFinishFragment() {
        JREngage.logd(TAG, "[tryToFinishFragment]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            if (mRetain != null) JRConnectionManager.stopConnectionsForDelegate(mRetain.mConnectionDelegate);
            finishFragment();
        }
    }

    private boolean isMobileEndpointUrl(String url) {
        if (mSession == null) return false;
        final String endpointUrl = mSession.getRpBaseUrl() + "/signin/device";
        return ((!TextUtils.isEmpty(url)) && (url.startsWith(endpointUrl)));
    }

    private void showProgressSpinner() {
        if (mProgressSpinner == null) return;
        mProgressSpinner.setVisibility(View.VISIBLE);
    }

    private void hideProgressSpinner() {
        if (mProgressSpinner == null) return;
        if (!mIsLoadingMobileEndpoint) mProgressSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.jr_menu_item_refresh));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals(getString(R.string.jr_menu_item_refresh))) {
            JREngage.logd(TAG, "refreshing WebView");
            mWebView.reload();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void loadMobileEndpointUrl(String url) {
        mIsLoadingMobileEndpoint = true;
        showProgressSpinner();

        String urlToLoad = url + "&auth_info=true";
        JREngage.logd(TAG, "[loadMobileEndpointUrl] loading URL: " + urlToLoad);

        JRConnectionManager.createConnection(urlToLoad, mRetain.mConnectionDelegate, null);
    }

    private DownloadListener mWebViewDownloadListener = new DownloadListener() {
        /* Used by pre 2.3 (2.2?) instead of shouldOverrideUrlLoading because of platform bugs. */
        public void onDownloadStart(String url,
                                    String userAgent,
                                    String contentDisposition,
                                    String mimetype,
                                    long contentLength) {
            JREngage.logd(TAG, "[onDownloadStart] URL: " + url + " | mimetype: " + mimetype
                    + " | length: " + contentLength);

            if (isMobileEndpointUrl(url)) loadMobileEndpointUrl(url);
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Seems to be broken according to this:
            // http://code.google.com/p/android/issues/detail?id=2887
            // This method is getting called only once in the Engage flow, when there are at least two
            // redirects involved.
            // Another bug documents that this method isn't called on a form submission via POST
            // http://code.google.com/p/android/issues/detail?id=9122
            JREngage.logd(TAG, "[shouldOverrideUrlLoading]: " + view + ", " + url);

            if (isMobileEndpointUrl(url)) {
                loadMobileEndpointUrl(url);
                return true;
            }

            /* Intercept and sink mailto links because the webview auto-linkifies example email addresses
             * in the Google and Yahoo login pages and it's too easy to fat finger them :(
             */
            if (Uri.parse(url).getScheme().equals("mailto")) return true;

            // Same for any scheme besides HTTP or HTTPS, e.g. market://
            return !(Uri.parse(url).getScheme().equals("http") || Uri.parse(url).getScheme().equals("https"));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            JREngage.logd(TAG, "[onPageStarted] url: " + url);

            /* Check for mobile endpoint URL. */
            if (isMobileEndpointUrl(url)) {
                JREngage.logd(TAG, "[onPageStarted] looks like JR mobile endpoint URL");
                loadMobileEndpointUrl(url);
                view.stopLoading();
                view.loadUrl("about:blank");
            } else {
                mCurrentlyLoadingUrl = url;
            }

            //if (getArguments().getInt(JR_FRAGMENT_FLOW_MODE) == JR_FRAGMENT_FLOW_BETA_DIRECT_SHARE) {
            //    Uri parsedUrl = Uri.parse(url);
            //    if (parsedUrl.getPath().equals("xdr")) {
            //        JREngage.logd(TAG, "auto-closing XDR");
            //        view.stopLoading();
            //        view.loadUrl("javascript:self.close();");
            //        return;
            //    }
            //}

            showProgressSpinner();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            JREngage.logd(TAG, "[onPageFinished] URL: " + url);
            mCurrentlyLoadingUrl = null;

            hideProgressSpinner();

            if (mProvider == null) {
                JREngage.logd(TAG, "returning from onPageFinished early due to beta share widget flow " +
                        "mode");
                return;
            }
            /* We inject some JS into the WebView. The JS is from the configuration pulled down from 
             * Engage. This way we can remotely fix up pages which render poorly/brokenly, like Yahoo!.
             */
            List<String> jsInjects =
                    mProvider.getWebViewOptions().getAsListOfStrings(JRDictionary.KEY_JS_INJECTIONS, true);
            for (String i : jsInjects) view.loadUrl("javascript:" + i);

            boolean showZoomControl = 
                    mProvider.getWebViewOptions().getAsBoolean(JRDictionary.KEY_SHOW_ZOOM_CONTROL);
            if (showZoomControl) view.invokeZoomPicker();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            super.onReceivedError(view, errorCode, description, url);
            Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                    + " | URL: " + url);

            hideProgressSpinner();

            if (mProvider == null) {
                JREngage.logd(TAG, "returning from onReceivedError early due to beta share widget flow " +
                        "mode");
                return;
            }

            mIsFinishPending = true;
            showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                    getString(R.string.jr_webview_error_dialog_msg));

            setFragmentResult(JRWebViewFragment.RESULT_FAIL_AND_STOP);
            mSession.triggerAuthenticationDidFail(new JREngageError(
                    "Authentication failed: " + description,
                    JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                    JREngageError.ErrorType.AUTHENTICATION_FAILED));
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            /* Hide the progress spinner if the page is more than half finished loading.
             * This is motivated by two things:
             *  - The page is usually loaded enough to interact with
             *  - Hyves keeps some page resource request open or something for several minutes
             *    (during which time the page is otherwise ~completely loaded.)
             */
            if (newProgress > 50) {
                hideProgressSpinner();
            }
        }

        @Override
        public boolean onCreateWindow(WebView view,
                                      boolean isDialog,
                                      boolean isUserGesture,
                                      Message resultMsg) {
            JREngage.logd(TAG, "[onCreateWindow] " + view);

            WebView newWebView = new WebView(getActivity());

            view.setVisibility(View.GONE);
            ((FrameLayout) view.getParent()).addView(newWebView, 0, view.getLayoutParams());
            view.getParent().focusableViewAvailable(newWebView);

            ensureWebViewSettings(newWebView.getSettings());
            newWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
            newWebView.getSettings().setBuiltInZoomControls(false);
            newWebView.setWebViewClient(mWebViewClient);
            newWebView.setWebChromeClient(this);

            // Experimental popup displayed as dialog code
            //Dialog d = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
            //d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //FrameLayout fl = new FrameLayout(getActivity());
            //fl.addView(newWebView);
            //fl.setPadding(AndroidUtils.scaleDipToPixels(15),
            //        AndroidUtils.scaleDipToPixels(15),
            //        AndroidUtils.scaleDipToPixels(15),
            //        AndroidUtils.scaleDipToPixels(15));
            //d.addContentView(fl, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            //                ViewGroup.LayoutParams.MATCH_PARENT));
            //d.show();

            ((WebView.WebViewTransport) resultMsg.obj).setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            JREngage.logd(TAG, "[onCloseWindow]: " + window);
            if (window != mWebView) {
                // TODO fix hardcoding of FB here
                mWebView.loadUrl("javascript:janrain.engage.share.loginPopupCallback('facebook');");
                ((FrameLayout) window.getParent()).removeView(window);
                mWebView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            JREngage.logd(TAG, "[console] message: '" + AndroidUtils.consoleMessageGetMessage(consoleMessage)
                    + "'");
            return true;
        }
    };

    @Override
    /*package*/ void onBackPressed() {
        JREngage.logd(TAG, "[onBackPressed]");
        if (mRetain != null) JRConnectionManager.stopConnectionsForDelegate(mRetain.mConnectionDelegate);
        doAuthRestart();
    }

    //public void setUseDesktopUa(boolean use) {
    //    mUseDesktopUa = use;
    //}

    private void connectionDidFinishLoading(HttpResponseHeaders headers,
                                           byte[] payload,
                                           String requestUrl,
                                           Object tag) {
        String payloadString = new String(payload);
        JREngage.logd(TAG, "[connectionDidFinishLoading] tag: " + tag + " | payload: " + payloadString);

        hideProgressSpinner();

        JRDictionary payloadDictionary;
        String alertTitle, alertMessage, logMessage;
        try {
            payloadDictionary = JRDictionary.fromJsonString(payloadString);
        } catch (JSONException e) {
            Log.e(TAG, "[connectionDidFinishLoading] failure parsing JSON: " + payloadString);
            mIsFinishPending = true;
            showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                    getString(R.string.jr_webview_error_dialog_msg));
            setFragmentResult(JRWebViewFragment.RESULT_FAIL_AND_STOP);
            mSession.triggerAuthenticationDidFail(new JREngageError(
                    "Authentication failed: " + payloadString,
                    JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                    JREngageError.ErrorType.AUTHENTICATION_FAILED));
            return;
        }

        JRDictionary resultDictionary = payloadDictionary.getAsDictionary("rpx_result");
        // TODO null guard here
        final String result = resultDictionary.getAsString("stat");
        if ("ok".equals(result)) {
            // TODO back button is no longer disabled because of the switch from modal dialog
            // to progress spinner, fix the code path when the user hits the back button now.

            if (!isSharingFlow()) mSession.saveLastUsedAuthProvider();
            mSession.triggerAuthenticationDidCompleteWithPayload(resultDictionary);
            finishFragmentWithResult(Activity.RESULT_OK);
        } else {
            final String error = resultDictionary.getAsString("error");

            if ("Discovery failed for the OpenID you entered".equals(error) ||
                    "Your OpenID must be a URL".equals(error)) {
                alertTitle = getString(R.string.jr_webview_bad_user_input_title);
                if (mProvider.requiresInput()) {
                    String s = mProvider.getUserInputDescriptor();
                    alertMessage = getString(R.string.jr_webview_bad_user_input_message, s);
                } else {
                    alertMessage = getString(R.string.jr_webview_generic_auth_error_message);
                    Log.e(TAG, "[connectionDidFinishLoading]: unrecognized openid error");
                }

                mIsFinishPending = true;
                setFragmentResult(RESULT_BAD_OPENID_URL);
                showAlertDialog(alertTitle, alertMessage);
                //} else if ("The URL you entered does not appear to be an OpenID".equals(error)) {
                // The error text changed :/
            } else if (error.matches(".*you entered does not appear to be an OpenID")) {
                alertTitle = getString(R.string.jr_webview_bad_user_input_title);
                if (mProvider.requiresInput()) {
                    alertMessage = getString(R.string.jr_webview_bad_user_input_message,
                            mProvider.getUserInputDescriptor());
                } else {
                    alertMessage = getString(R.string.jr_webview_generic_auth_error_message);
                }

                logMessage = "The URL you entered does not appear to be an OpenID: ";
                Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                mIsFinishPending = true;
                setFragmentResult(RESULT_BAD_OPENID_URL);
                showAlertDialog(alertTitle, alertMessage);
            } else if ("Please enter your OpenID".equals(error)) {
                // Caused by entering a ~blank OpenID URL

                mIsFinishPending = true;
                setFragmentResult(RESULT_BAD_OPENID_URL);
                // TODO resource-ify
                showAlertDialog("OpenID Error", "The URL you entered does not appear to be an OpenID");
            } else if ("canceled".equals(error)) {
                mProvider.setForceReauth(true);
                doAuthRestart();
            } else {
                Log.e(TAG, "unrecognized error: " + error);
                mIsFinishPending = true;
                showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                        getString(R.string.jr_webview_error_dialog_msg));
                setFragmentResult(JRWebViewFragment.RESULT_FAIL_AND_STOP);
                mSession.triggerAuthenticationDidFail(new JREngageError(
                        "Authentication failed: " + payloadString,
                        JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                        JREngageError.ErrorType.AUTHENTICATION_FAILED));
            }
        }
    }

    private void doAuthRestart() {
        JREngage.logd(TAG, "[doAuthRestart]");
        if (isSpecificProviderFlow() && mProvider != null && !mProvider.requiresInput()) {
            mSession.triggerAuthenticationDidCancel();
            finishFragmentWithResult(RESULT_RESTART);
        } else {
            mSession.triggerAuthenticationDidRestart();
            finishFragmentWithResult(RESULT_RESTART);
        }
    }

    private void connectionDidFail(Exception ex, String requestUrl, Object tag) {
        JREngage.logd(TAG, "[connectionDidFail] userdata: " + tag, ex);

        if (hasView()) {
            // This is intended to not run if the user pressed the back button after the MEU started
            // loading but before it failed.
            // The test is probably not quite right and that if the timing is bad both onBackPressed()
            // and this method will call setResult
            mIsFinishPending = true;
            showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                    getString(R.string.jr_dialog_network_error));
            setFragmentResult(JRWebViewFragment.RESULT_FAIL_AND_STOP);
            mSession.triggerAuthenticationDidFail(new JREngageError(
                    "Authentication failed",
                    JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                    JREngageError.ErrorType.AUTHENTICATION_FAILED,
                    ex));
        }
    }

    /**
     * @internal
     * This class serves to respond to the MEU connection and delegates the result to the real fragment.
     * This is necessary because the real fragment can be destroyed and recreated if it's  in an Activity
     * which is destroyed and recreated because it cannot setRetainInstance(true) because it may be added to
     * the back stack.
     */
    public static class RetainFragment extends Fragment {
        private static final String TAG  = RetainFragment.class.getSimpleName();
        JRWebViewFragment mTarget;

        /* The deferred connectionDidFinishLoading message */
        HttpResponseHeaders mDeferredCdflH;
        byte[] mDeferredCdflBa;
        String mDeferredCdflS;
        Object mDeferredCdflO;

        /* The deferred connectionDidFail message */
        Exception mDeferredCdfE;
        String mDeferredCdfS;
        Object mDeferredCdfO;
        
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setRetainInstance(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            mTarget = (JRWebViewFragment) getTargetFragment();
            boolean a = isResumed();
            maybeDispatchMessages();
        }

        @Override
        public void onStop() {
            super.onStop();
            mTarget = null;
        }

        private JRConnectionManagerDelegate mConnectionDelegate = new JRConnectionManagerDelegate() {
            public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                   byte[] payload,
                                                   String requestUrl,
                                                   Object tag) {
                JREngage.logd(TAG, "[connectionDidFinishLoading]");
                mDeferredCdflH = headers;
                mDeferredCdflBa = payload;
                mDeferredCdflS = requestUrl;
                mDeferredCdflO = tag;

                maybeDispatchMessages();
            }

            public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
                JREngage.logd(TAG, "[connectionDidFail]");
                mDeferredCdfE = ex;
                mDeferredCdfS = requestUrl;
                mDeferredCdfO = tag;

                maybeDispatchMessages();
            }
        };

        private void maybeDispatchMessages() {
            if (mTarget != null && isResumed()) {
                if (mDeferredCdflH != null) {
                    mTarget.connectionDidFinishLoading(
                            mDeferredCdflH, mDeferredCdflBa, mDeferredCdflS, mDeferredCdflO);
                    mDeferredCdflH = null;
                    mDeferredCdflBa = null;
                    mDeferredCdflS = null;
                    mDeferredCdflO = null;
                }

                if (mDeferredCdfE != null) {
                    mTarget.connectionDidFail(mDeferredCdfE, mDeferredCdfS, mDeferredCdfO);
                    mDeferredCdfE = null;
                    mDeferredCdfS = null;
                    mDeferredCdfO = null;
                }
            }
        }
    }

    @Override
    /*package*/ boolean shouldShowTitleWhenDialog() {
        return getCustomUiConfiguration() != null &&
                getCustomUiConfiguration().mShowWebViewTitleWhenDialog != null &&
                getCustomUiConfiguration().mShowWebViewTitleWhenDialog;
    }

    @Override
    String getCustomTitle() {
        if (getCustomUiConfiguration() != null) return getCustomUiConfiguration().mWebViewTitle;
        return null;
    }
}