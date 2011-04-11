package com.janrain.android.quicksignin;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/6/11
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProfilesActivity extends ListActivity {

    /**
     * Array adapter used to render individual providers in list view.
     */
    private class ProfileAdapter extends ArrayAdapter<String> {
        private int mResourceId;

        public ProfileAdapter(Context context, int resId, ArrayList<String> items) {
            super(context, -1, items);

            mResourceId = resId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(mResourceId, null);
                Log.i(TAG, "[getView] with null converView");
            } else Log.i(TAG, "[getView] with non null convertView");

            ImageView icon = (ImageView)v.findViewById(R.id.rowIcon);
            TextView label = (TextView)v.findViewById(R.id.rowLabel);

            String profile = getItem(position);

            label.setText(profile);

            return v;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = ProfilesActivity.class.getSimpleName();
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

    private ArrayList<String> mProfilesList;
    private ProfileAdapter mAdapter;
    private ProfileData mProfileData;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public ProfilesActivity() {
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
        setContentView(R.layout.provider_listview);


        mProfileData = ProfileData.getInstance();
        mProfilesList = mProfileData.getProfilesList();

        if (mProfilesList == null) {
            mProfilesList = new ArrayList<String>();
        }

        mAdapter = new ProfileAdapter(this, R.layout.profiles_listview_row, mProfilesList);
        setListAdapter(mAdapter);

        if (mProfilesList.size() == 0) {

        }
    }

    public void onResume () {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * This method will be called when an item in the list is selected.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        String profile = mAdapter.getItem(pos);
        mProfileData.setCurrentProfile(profile);


    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the shared menu
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback for creating dialogs that are managed.
     */
    protected Dialog onCreateDialog(int id) {
        return null;
    }

    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        finish();
    }
}