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

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 7/11/11
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class JRUiFragment extends Fragment {
    private FinishReceiver mFinishReceiver;
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
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_FRAGMENT_TARGET);

            if (JRUiFragment.this.getClass().toString().equals(target)) {
                if (!JRUserInterfaceMaestro.getInstance().isEmbeddedMode()) {
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
        super.onActivityCreated(savedInstanceState);
        mLayoutHelper = new SharedLayoutHelper(getActivity());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSessionData = JRSessionData.getInstance();

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            getActivity().registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLayoutHelper.showHideTaglines();
    }


    /* package */ Dialog onCreateDialog(int id) {
        return mLayoutHelper.onCreateDialog(id);
    }

    /* package */ void onPrepareDialog(int id, Dialog d) {}

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mFinishReceiver != null) getActivity().unregisterReceiver(mFinishReceiver);
    }

    /* package */ SharedLayoutHelper getSharedLayoutHelper() {
        return mLayoutHelper;
    }

    protected void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        getActivity().finish();
    }

}
