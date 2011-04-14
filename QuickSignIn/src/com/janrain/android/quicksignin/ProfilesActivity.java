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
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

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
public class ProfilesActivity extends ListActivity implements View.OnClickListener, JREngageDelegate {
    /**
     * Array adapter used to render individual providers in list view.
     */
    private class ProfileAdapter extends ArrayAdapter<LoginSnapshot> {
        private int mResourceId;

        public ProfileAdapter(Context context, int resId, ArrayList<LoginSnapshot> items) {
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

            LoginSnapshot snapshot = getItem(position);

            label.setText(snapshot.getDisplayName());

            return v;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = ProfilesActivity.class.getSimpleName();

    private static final String ENGAGE_APP_ID = "";
    private static final String ENGAGE_TOKEN_URL = null;//"http://jrengage-for-android.appspot.com/login";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private ArrayList<LoginSnapshot> mProfilesList;
    private ProfileAdapter mAdapter;
    private ProfileData mProfileData;

    private JREngage mEngage;

    private Button mAddProfile;
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
        setContentView(R.layout.profiles_listview);

        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);        

        mAddProfile = (Button)findViewById(R.id.btn_add_profile);
        mAddProfile.setOnClickListener(this);

        mProfileData = ProfileData.getInstance();
        mProfilesList = mProfileData.getProfilesList();

        if (mProfilesList == null) {
            mProfilesList = new ArrayList<LoginSnapshot>();
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
        LoginSnapshot snapshot = mAdapter.getItem(pos);
        mProfileData.setCurrentProfile(snapshot.getIdentifier());


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


    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidNotComplete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        JRDictionary profile = (auth_info == null) ? null : auth_info.getAsDictionary("profile");
        String displayName = (profile == null) ? null : profile.getAsString("displayName");
        String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                ? "" : (" for user: " + displayName));

        mProfileData.addProfile(auth_info, provider);
        mAdapter.notifyDataSetChanged();
                    
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, String tokenUrlPayload, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidNotCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onClick(View view) {
        mEngage.showAuthenticationDialog();
    }
}