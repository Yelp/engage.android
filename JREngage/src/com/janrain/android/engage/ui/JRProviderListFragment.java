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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Config;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @internal
 *
 * @class JRProvidersActivity
 * Displays list of [basic] providers.
 */
public class JRProviderListFragment extends Fragment {
    private ListView mListView;
    private TextView mEmptyTextView;

    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     **/
    private class FinishReceiver extends BroadcastReceiver {

        private final String TAG = JRProviderListFragment.TAG
                + "-"
                + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(JRUserInterfaceMaestro.EXTRA_FINISH_FRAGMENT_TARGET);
            //if (JRProviderListActivity.class.toString().equals(target)) {
            if (JRProviderListFragment.class.toString().equals(target)) {
                tryToFinishActivity();
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

    /**
     * @internal
     *
     * @class ProviderAdapter
     * Array adapter used to render individual providers in list view.
     **/
    private class ProviderAdapter extends ArrayAdapter<JRProvider> {
        LayoutInflater li = (LayoutInflater)
                JREngage.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        public ProviderAdapter() {
            // The super class only ends up using the last parameter passed into this super constructor,
            // the List.  The first two parameters are never used.
            //getActivity()

            super(JREngage.getContext(), 0, mProviderList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // This line is pretty much copied from the super implementation, which doesn't attach
                // the newly inflated View to it's parent. I don't know why.
                convertView = li.inflate(R.layout.jr_provider_listview_row, parent, false);
            }

            ImageView icon = (ImageView) convertView.findViewById(R.id.jr_row_provider_icon);
            TextView label = (TextView) convertView.findViewById(R.id.jr_row_provider_label);

            JRProvider provider = getItem(position);

            Drawable providerIcon = provider.getProviderIcon(getContext());
            icon.setImageDrawable(providerIcon);
            label.setText(provider.getFriendlyName());

            return convertView;
        }
    }

    private static final String TAG = JRProviderListFragment.class.getSimpleName();
    private static final int TIMER_MAX_ITERATIONS = 30;

    private SharedLayoutHelper mLayoutHelper;
    private JRSessionData mSessionData;
    private ArrayList<JRProvider> mProviderList;
    private ProviderAdapter mAdapter;
    private Timer mTimer;
    private int mTimerCount = 0;
    private FinishReceiver mFinishReceiver;

    /**
     * Used to alert user that no providers can be found.  Runs on the UI thread.
     */
    private Runnable mNoProvidersFoundRunner = new Runnable() {
        public void run() {
            mEmptyTextView.setVisibility(View.VISIBLE);

            mLayoutHelper.dismissProgressDialog();
            Toast.makeText(getActivity(),
                    "No providers found.",
                    Toast.LENGTH_LONG).show();
        }
    };

    /**
     * Used to update the provider list on the UI thread.
     */
    private Runnable mProvidersLoadedRunner = new Runnable() {
        public void run() {
            mListView.setVisibility(View.VISIBLE);
            for (JRProvider p : mProviderList) mAdapter.add(p);
            mAdapter.notifyDataSetChanged();
            mLayoutHelper.dismissProgressDialog();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //switch ((mNum-1)%6) {
        //    case 1: style = DialogFragment.STYLE_NO_TITLE; break;
        //    case 2: style = DialogFragment.STYLE_NO_FRAME; break;
        //    case 3: style = DialogFragment.STYLE_NO_INPUT; break;
        //    case 4: style = DialogFragment.STYLE_NORMAL; break;
        //    case 5: style = DialogFragment.STYLE_NORMAL; break;
        //    case 6: style = DialogFragment.STYLE_NO_TITLE; break;
        //    case 7: style = DialogFragment.STYLE_NO_FRAME; break;
        //    case 8: style = DialogFragment.STYLE_NORMAL; break;
        //}
        //switch ((mNum-1)%6) {
        //    case 4: theme = android.R.style.Theme_Holo; break;
        //    case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
        //    case 6: theme = android.R.style.Theme_Holo_Light; break;
        //    case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
        //    case 8: theme = android.R.style.Theme_Holo_Light; break;
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSessionData = JRSessionData.getInstance();

        View listView = inflater.inflate(R.layout.jr_provider_listview, container, false);

        mListView = (ListView) listView.findViewById(android.R.id.list);
        mEmptyTextView = (TextView) listView.findViewById(android.R.id.empty);

        mLayoutHelper = new SharedLayoutHelper(getActivity());

        mProviderList = mSessionData.getBasicProviders();

        if (mProviderList == null) {
            mProviderList = new ArrayList<JRProvider>();
        }

        mAdapter = new ProviderAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(itemClickListener);

        if (mProviderList.size() == 0) {
            mListView.setVisibility(View.GONE);

            /* Show progress and poll for results */
            mLayoutHelper.showProgressDialog();
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    doSessionPoll();
                }
            }, 0, 500);
        }

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            getActivity().registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }

        return listView;
    }

    @Override
    public void onResume () {
        super.onResume();

        Log.d(TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mFinishReceiver != null)
        getActivity().unregisterReceiver(mFinishReceiver);

        if (mTimer != null) mTimer.cancel();
    }

    /**
     * This method will be called when an item in the list is selected.
     */
    private ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            JRProvider provider = mAdapter.getItem(position);
            mSessionData.setCurrentlyAuthenticatingProvider(provider);

            // todo
            // || provider.getName().equals(mSessionData.getReturningBasicProvider())
            // used to be part of the if conditional, it seems to be related to giving the user an
            // opportunity to switch accounts if they already have credentials, however if that's the
            // intention mSessionData.getAuthenticatedUserForProvider() != null or something should be
            // used so that providers that aren't the returning basic provider are also afforded the same
            // possibility.

            if (provider.requiresInput()) {
                JRUserInterfaceMaestro.getInstance().showUserLanding();
            } else {
                JRUserInterfaceMaestro.getInstance().showWebView();
            }
        }
    };

    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        getActivity().finish();
    }

    /**
     * Called by timer (on fire interval).  Used when providers are not found in JRSessionData.
     * Continues polling until providers are found or the polling threshold is hit.
     */
    private void doSessionPoll() {
        ++mTimerCount;
        if (Config.LOGD) {
            Log.d(TAG, "[doSessionPoll] timer count: " + mTimerCount);
        }

        if (mTimerCount > TIMER_MAX_ITERATIONS) {
            mTimer.cancel();
            getActivity().runOnUiThread(mNoProvidersFoundRunner);
            Log.w(TAG, "[doSessionPoll] providers not found, max iterations hit, timer cancelled...");
        } else {
            ArrayList<JRProvider> providers = mSessionData.getBasicProviders();
            if (providers.size() > 0) {
                mProviderList = providers;
                getActivity().runOnUiThread(mProvidersLoadedRunner);
                mTimer.cancel();
                Log.i(TAG, "[doSessionPoll] providers found, timer cancelled...");
            } else {
                Log.i(TAG, "[doSessionPoll] no providers yet, will retry soon...");
            }
        }
    }
}