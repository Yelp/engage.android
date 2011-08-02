package com.janrain.android.engage.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.session.JRSessionData;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 7/11/11
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class JRUiFragment extends Fragment {
    public static final int REQUEST_LANDING = 1;
    public static final int REQUEST_WEBVIEW = 2;

    private FinishReceiver mFinishReceiver;
    private HashMap<Integer, Dialog> mManagedDialogs = new HashMap<Integer, Dialog>();
    private boolean mEmbeddedMode = false;

    protected SharedLayoutHelper mLayoutHelper;
    protected JRSessionData mSessionData;
    protected static String TAG = JRUiFragment.class.getSimpleName();


    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     **/
    private class FinishReceiver extends BroadcastReceiver {
        private final String TAG = JRUiFragment.TAG + "-" + FinishReceiver.class.getSimpleName();

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
    public void onActivityCreated(Bundle savedInstanceState) {
        if (Config.LOGD) Log.d(TAG, "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);
        mLayoutHelper = new SharedLayoutHelper(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Config.LOGD) Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        mSessionData = JRSessionData.getInstance();

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            getActivity().registerReceiver(mFinishReceiver, JRFragmentHostActivity.FINISH_INTENT_FILTER);
        }
    }

    @Override
    public void onResume() {
        if (Config.LOGD) Log.d(TAG, "[onResume]");
        super.onResume();
        mLayoutHelper.showHideTaglines();
    }

    @Override
    public void onDestroy() {
        if (Config.LOGD) Log.d(TAG, "[onDestroy]");
        super.onDestroy();

        if (mFinishReceiver != null) getActivity().unregisterReceiver(mFinishReceiver);
    }

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
        Intent i = new Intent(getActivity(), JRFragmentHostActivity.class);
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
