package com.janrain.android.engage.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.session.JRSessionData;

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

    private FinishReceiver mFinishReceiver;
    private HashMap<Integer, Dialog> mManagedDialogs = new HashMap<Integer, Dialog>();
    private boolean mEmbeddedMode = false;

    protected SharedLayoutHelper mLayoutHelper;
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
        if (Config.LOGD) Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        mSessionData = JRSessionData.getInstance();
    }

    //onCreateView

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (Config.LOGD) Log.d(TAG, "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);
        mLayoutHelper = new SharedLayoutHelper(getActivity());
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
        mLayoutHelper.showHideTaglines();
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

    public boolean isEmbeddedMode() {
        return mEmbeddedMode;
    }

    public void setEmbeddedMode(boolean embeddedMode) {
        mEmbeddedMode = embeddedMode;
    }

    /* package */ Dialog onCreateDialog(int id) {
        return mLayoutHelper.onCreateDialog(id);
    }

    /* package */ void onPrepareDialog(int id, Dialog d) {}

    protected void showDialog(int dialogId) {
        if (isEmbeddedMode()) {
            Dialog d;
            if (mManagedDialogs.containsKey(dialogId)) {
                d = mManagedDialogs.get(dialogId);
            } else {
                d = onCreateDialog(dialogId);
                mManagedDialogs.put(dialogId, d);
            }
            onPrepareDialog(dialogId, d);
            d.show();
        } else {
            getActivity().showDialog(dialogId);
        }
    }

    protected void dismissDialog(int dialogId) {
        if (isEmbeddedMode()) {
            Dialog d = mManagedDialogs.get(dialogId);
            d.dismiss();
        } else {
            getActivity().dismissDialog(dialogId);
        }
    }

    private void startActivityForFragId(int fragId, int requestCode) {
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
        startActivityForResult(i, requestCode);
    }

    protected void showUserLanding() {
        startActivityForFragId(JRFragmentHostActivity.JR_LANDING, REQUEST_LANDING);
    }

    protected void showWebView() {
        startActivityForFragId(JRFragmentHostActivity.JR_WEBVIEW, REQUEST_WEBVIEW);
    }

    /* package */ SharedLayoutHelper getSharedLayoutHelper() {
        return mLayoutHelper;
    }

    protected void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        getActivity().finish();
    }
}
