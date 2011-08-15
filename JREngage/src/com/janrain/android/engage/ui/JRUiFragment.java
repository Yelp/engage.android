package com.janrain.android.engage.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Config;
import android.util.Log;
import android.view.*;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRSessionData;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 7/11/11
 * Time: 1:51 PM
 */
public abstract class JRUiFragment extends Fragment {
    public static final int REQUEST_LANDING = 1;
    public static final int REQUEST_WEBVIEW = 2;
    public static final int DIALOG_ABOUT = 1000;
    public static final int DIALOG_PROGRESS = 1001;

    private FinishReceiver mFinishReceiver;
    private HashMap<Integer, Dialog> mManagedDialogs = new HashMap<Integer, Dialog>();
    private boolean mEmbeddedMode = false;

    protected JRSessionData mSessionData;
    protected String TAG = JRUiFragment.class.getSimpleName();


    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     **/
    private class FinishReceiver extends BroadcastReceiver {
        private String TAG = JRUiFragment.this.TAG + "-" + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(JRFragmentHostActivity.EXTRA_FINISH_FRAGMENT_TARGET);

            if (JRUiFragment.this.getClass().toString().equals(target)) {
                if (!isEmbeddedMode()) {
                    tryToFinishActivity();
                }
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onAttach(activity);

        if (mFinishReceiver == null) mFinishReceiver = new FinishReceiver();
        getActivity().registerReceiver(mFinishReceiver, JRFragmentHostActivity.FINISH_INTENT_FILTER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Config.LOGD) Log.d(TAG, "[onCreate]");

        mSessionData = JRSessionData.getInstance();
        setRetainInstance(true);
    }

    //onCreateView

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (Config.LOGD) Log.d(TAG, "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);

        mSessionData = JRSessionData.getInstance();
    }

    @Override
    public void onStart() {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onStart();
    }

    @Override
    public void onResume() {
        if (Config.LOGD) Log.d(TAG, "[onResume]");
        super.onResume();
        showHideTaglines();
    }

    @Override
    public void onPause() {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (Config.LOGD) Log.d(TAG, "[onDestroyView]");

        for (Dialog d : mManagedDialogs.values()) d.dismiss();
        mManagedDialogs = new HashMap<Integer, Dialog>();

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (Config.LOGD) Log.d(TAG, "[onDestroy]");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        if (mFinishReceiver != null) getActivity().unregisterReceiver(mFinishReceiver);

        super.onDetach();
    }

    //--
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onInflate(activity, attrs, savedInstanceState);

        if (JRSessionData.getInstance() == null) {
            throw new IllegalStateException("You must call JREngage.initInstance before inflating " +
                    "JREngage fragments.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (Config.LOGD) Log.d(TAG, "[" + new Object(){}.getClass().getEnclosingMethod().getName() + "]");
        super.onHiddenChanged(hidden);
    }
    //--

    private void showHideTaglines() {
        boolean hideTagline = mSessionData.getHidePoweredBy();
        int visibility = hideTagline ? View.GONE : View.VISIBLE;

        View tagline = getView().findViewById(R.id.jr_tagline);
        if (tagline != null) tagline.setVisibility(visibility);

        View bonusTagline = getView().findViewById(R.id.jr_email_sms_powered_by_text);
        if (bonusTagline != null) bonusTagline.setVisibility(visibility);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (mSessionData.getHidePoweredBy()) {
            return;
        } else {
            inflater.inflate(R.menu.jr_about_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.jr_menu_about) {
            showDialog(DIALOG_ABOUT);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.jr_progress_loading));
        return progressDialog;
    }

    protected void showProgressDialog() {
        showDialog(DIALOG_PROGRESS);
    }

    protected void dismissProgressDialog() {
        dismissDialog(DIALOG_PROGRESS);
    }

    private AlertDialog getAboutDialog() {
        AlertDialog.Builder builder;
        AlertDialog dialog;

        LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.jr_about_dialog, null);

        builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        builder.setPositiveButton(R.string.jr_about_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();

        return dialog;
    }

    private boolean isEmbeddedMode() {
        return mEmbeddedMode;
    }

    public void setEmbeddedMode(boolean embeddedMode) {
        mEmbeddedMode = embeddedMode;
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case DIALOG_ABOUT:
                dialog = getAboutDialog();
                break;
            case DIALOG_PROGRESS:
                dialog = getProgressDialog();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    protected void onPrepareDialog(int id, Dialog d) {}

    protected void showDialog(int dialogId) {
        Dialog d;
        if (mManagedDialogs.containsKey(dialogId)) {
            d = mManagedDialogs.get(dialogId);
        } else {
            d = onCreateDialog(dialogId);
            mManagedDialogs.put(dialogId, d);
        }
        onPrepareDialog(dialogId, d);
        d.show();
    }

    protected void dismissDialog(int dialogId) {
        Dialog d = mManagedDialogs.get(dialogId);
        d.dismiss();
    }

    private void startActivityForFragId(int fragId, int requestCode) {
        startActivityForFragId(fragId, requestCode, null);
    }

    private void startActivityForFragId(int fragId, int requestCode, Bundle opts) {
        boolean showTitle;
        switch (fragId) {
            case JRFragmentHostActivity.JR_LANDING:
                showTitle = true;
                break;
            case JRFragmentHostActivity.JR_WEBVIEW:
                showTitle = false;
                break;
            default: throw new JRFragmentHostActivity.IllegalFragmentIdException(fragId);
        }

        Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(getActivity(), showTitle);
        i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, fragId);
        if (opts != null) i.putExtras(opts);
        startActivityForResult(i, requestCode);
    }

    protected void showUserLanding() {
        startActivityForFragId(JRFragmentHostActivity.JR_LANDING, REQUEST_LANDING);
    }

    protected void showWebView(boolean socialSharingSignIn) {
        Bundle opts = new Bundle();
        opts.putBoolean(JRWebViewFragment.SOCIAL_SHARING_MODE, socialSharingSignIn);
        startActivityForFragId(JRFragmentHostActivity.JR_WEBVIEW, REQUEST_WEBVIEW, opts);
    }

    protected void showWebView() {
        startActivityForFragId(JRFragmentHostActivity.JR_WEBVIEW, REQUEST_WEBVIEW);
    }

    protected void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        getActivity().finish();
    }
}
