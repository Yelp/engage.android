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

import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
public class JRProvidersActivity extends ListActivity {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     **/
    private class FinishReceiver extends BroadcastReceiver {

        private final String TAG = JRProvidersActivity.TAG
                + "-"
                + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
            if (JRProvidersActivity.class.toString().equals(target)) {
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
        private int mResourceId;

        public ProviderAdapter(Context context, int resId, ArrayList<JRProvider> items) {
            super(context, -1, items);

            mResourceId = resId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(mResourceId, null);
                Log.i(TAG, "[getView] with null converView");
            } else Log.i(TAG, "[getView] with non null convertView");

            ImageView icon = (ImageView) v.findViewById(R.id.jr_row_provider_icon);
            TextView label = (TextView) v.findViewById(R.id.jr_row_provider_label);

            final JRProvider provider = getItem(position);

            Drawable providerIcon = provider.getProviderIcon(getContext());
            icon.setImageDrawable(providerIcon);
            label.setText(provider.getFriendlyName());

            return v;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRProvidersActivity.class.getSimpleName();
    private static final int TIMER_MAX_ITERATIONS = 30;

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
    private ArrayList<JRProvider> mProviderList;
    private ProviderAdapter mAdapter;
    private Timer mTimer;
    private int mTimerCount;
    private FinishReceiver mFinishReceiver;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    /**
     * Used to alert user that no providers can be found on the UI thread.
     */
    private Runnable mNoProvidersFoundRunner = new Runnable() {
        public void run() {
            mLayoutHelper.dismissProgressDialog();
            Toast.makeText(JRProvidersActivity.this,
                    "No providers found.",
                    Toast.LENGTH_LONG).show();
        }
    };

    /**
     * Used to update the provider list on the UI thread.
     */
    private Runnable mProvidersLoadedRunner = new Runnable() {
        public void run() {
            mAdapter.notifyDataSetChanged();
            for (JRProvider provider : mProviderList) {
                mAdapter.add(provider);
            }
            mAdapter.notifyDataSetChanged();
            mLayoutHelper.dismissProgressDialog();
        }
    };


    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public JRProvidersActivity() {
    }

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
        setContentView(R.layout.jr_provider_listview);

        mTimerCount = 0;
        mTimer = new Timer();
        mLayoutHelper = new SharedLayoutHelper(this);

        mSessionData = JRSessionData.getInstance();

        //for the case when this activity is relaunched after the process was killed
        if (mSessionData == null) {
            Log.e(TAG, "JRProvidersActivity bailing out after a process kill/restart");
            finish();
            return;
        }

        mProviderList = mSessionData.getBasicProviders();

        if (mProviderList == null) {
            mProviderList = new ArrayList<JRProvider>();
        }

        mAdapter = new ProviderAdapter(this, R.layout.jr_provider_listview_row, mProviderList);
        setListAdapter(mAdapter);

        //registerForContextMenu(getListView());

        if (mProviderList.size() == 0) {
            // show progress and poll for results
            mLayoutHelper.showProgressDialog();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    doSessionPoll();
                }
            }, 0, 1000);
        }

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }

        // XXX
        // What's happening is that we need to start the landing activity from this onCreate method
        // So that this activities FinishHandler is registered (which happens in this onCreate)
        // otherwise (if the activity is started from the UI maestro) this activity isn't created
        // until it's popped into view, at which point the FinishHandler is registered, but by
        // which time this activity has already missed it's finish message.  The getStack call
        // allows us to add the LandingActivity to the managed activity stack after we start it with
        // startActivityForResult. startActivityForResult has a special case for requestCodes >= 0
        // that makes this activity's window not draw.  That can only be called from within
        // an Activity, however, so the UI maestro can't use it (because it uses a generic
        // application Context.)
        if (!TextUtils.isEmpty(mSessionData.getReturningBasicProvider())) {
            mSessionData.setCurrentlyAuthenticatingProvider(
                    mSessionData.getReturningBasicProvider());
            startActivityForResult(new Intent(this, JRLandingActivity.class), 0);
            JRUserInterfaceMaestro.getInstance()
                    .getManagedActivityStack().push(JRLandingActivity.class);
        }
    }

    public void onResume () {
        super.onResume();
        JREngage.setContext(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (com.janrain.android.engage.utils.Android.asdf()
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        mSessionData.triggerAuthenticationDidCancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFinishReceiver != null)
        unregisterReceiver(mFinishReceiver);

        mTimer.cancel();
    }

    // This test code adds a context menu to the providers.
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.provider_listview_row_menu, menu);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//      AdapterView.AdapterContextMenuInfo info =
//              (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//      switch (item.getItemId()) {
//      case R.id.force_reauth:
//        mAdapter.getItem((int) info.id).setForceReauth(true);
//        return true;
//      default:
//        return super.onContextItemSelected(item);
//      }
//    }

    /**
     * This method will be called when an item in the list is selected.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        JRProvider provider = mAdapter.getItem(pos);
        mSessionData.setCurrentlyAuthenticatingProvider(provider);

        //todo
        // || provider.getName().equals(mSessionData.getReturningBasicProvider())
        //used to be part of the if conditional, seems to be related to giving the user an
        //opportunity to switch accounts if they already have credentials, however if that's the
        //intention mSessionData.getAuthenticatedUserForProvider() != null or something should be
        //used so that providers that aren't the returning basic provider are also afforded the same
        //possibility.

        if (provider.requiresInput()) {
            JRUserInterfaceMaestro.getInstance().showUserLanding();
        } else {
            JRUserInterfaceMaestro.getInstance().showWebView();
        }
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

    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        finish();
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
            runOnUiThread(mNoProvidersFoundRunner);
            Log.w(TAG, "[doSessionPoll] providers not found, max iterations hit, timer cancelled...");
        } else {
            ArrayList<JRProvider> providers = mSessionData.getBasicProviders();
            if (providers.size() > 0) {
                mProviderList = providers;
                runOnUiThread(mProvidersLoadedRunner);
                mTimer.cancel();
                Log.i(TAG, "[doSessionPoll] providers found, timer cancelled...");
            } else {
                Log.i(TAG, "[doSessionPoll] no providers yet, will retry soon...");
            }
        }
    }
}