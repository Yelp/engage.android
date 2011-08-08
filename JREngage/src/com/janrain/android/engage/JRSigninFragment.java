package com.janrain.android.engage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.janrain.android.engage.ui.JRProviderListFragment;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 6/28/11
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class JRSigninFragment extends Fragment {
    public static final String TAG = JRSigninFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Config.LOGD) Log.d(TAG, "[onCreate]");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (Config.LOGD) Log.d(TAG, "[onCreateView]");

        View v = new View(getActivity());
        v.setBackgroundColor(0xffffffff);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Config.LOGD) Log.d(TAG, "[onResume]");

        JRProviderListFragment plf = new JRProviderListFragment();

        //getFragmentManager().beginTransaction().replace(R.id.jr_signin_fragment, plf).commit();
        getFragmentManager().beginTransaction().hide(this).add(plf, "plf").commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Config.LOGD) Log.d(TAG, "[onPause]");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Config.LOGD) Log.d(TAG, "[onDestroyView]");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Config.LOGD) Log.d(TAG, "[onDestroy]");
    }
}
