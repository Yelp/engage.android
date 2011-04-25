package com.janrain.android.quicksignin;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.IOUtils;
import com.janrain.android.engage.utils.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/6/11
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProfilesActivity extends ListActivity implements View.OnClickListener, JREngageDelegate {

    private static HashMap<String, Drawable> provider_list_icon_drawables =
            new HashMap<String, Drawable>();

    private final static HashMap<String, Integer> provider_list_icon_resources =
            new HashMap<String, Integer>(){
            {
                    put("icon_aol", com.janrain.android.engage.R.drawable.icon_aol);
                    put("icon_blogger", com.janrain.android.engage.R.drawable.icon_blogger);
                    put("icon_facebook", com.janrain.android.engage.R.drawable.icon_facebook);
                    put("icon_flickr", com.janrain.android.engage.R.drawable.icon_flickr);
                    put("icon_google", com.janrain.android.engage.R.drawable.icon_google);
                    put("icon_hyves", com.janrain.android.engage.R.drawable.icon_hyves);
                    put("icon_linkedin", com.janrain.android.engage.R.drawable.icon_linkedin);
                    put("icon_live_id", com.janrain.android.engage.R.drawable.icon_live_id);
                    put("icon_livejournal", com.janrain.android.engage.R.drawable.icon_livejournal);
                    put("icon_myopenid", com.janrain.android.engage.R.drawable.icon_myopenid);
                    put("icon_myspace", com.janrain.android.engage.R.drawable.icon_myspace);
                    put("icon_netlog", com.janrain.android.engage.R.drawable.icon_netlog);
                    put("icon_openid", com.janrain.android.engage.R.drawable.icon_openid);
                    put("icon_paypal", com.janrain.android.engage.R.drawable.icon_paypal);
                    put("icon_twitter", com.janrain.android.engage.R.drawable.icon_twitter);
                    put("icon_verisign", com.janrain.android.engage.R.drawable.icon_verisign);
                    put("icon_wordpress", com.janrain.android.engage.R.drawable.icon_wordpress);
                    put("icon_yahoo", com.janrain.android.engage.R.drawable.icon_yahoo);
           }
    };


    /**
     * Array adapter used to render individual providers in list view.
     */
    private class ProfileAdapter extends ArrayAdapter<LoginSnapshot> implements View.OnClickListener {
        private int mResourceId;

        public ProfileAdapter(Context context, int resId, ArrayList<LoginSnapshot> items) {
            super(context, -1, items);

            mResourceId = resId;
        }

        private Drawable getProviderIconDrawable(Context c, String providerName) {
            String drawableName = "icon_" + providerName;
            HashMap<String, Drawable> drawableMap = provider_list_icon_drawables;
            HashMap<String, Integer> resourceMap = provider_list_icon_resources;

            if (drawableMap.containsKey(drawableName)) return drawableMap.get(drawableName);

            if (resourceMap.containsKey(drawableName)) {
                Drawable r = c.getResources().getDrawable(resourceMap.get(drawableName));
                drawableMap.put(drawableName, r);
                return r;
            }

            try {
                String iconFileName = "providericon~" + drawableName + ".png";

                Bitmap icon = BitmapFactory.decodeStream(c.openFileInput(iconFileName));
                if (icon != null) {
                    icon.setDensity(android.util.DisplayMetrics.DENSITY_MEDIUM);
                }
                else {
                    c.deleteFile(iconFileName);
                    //downloadIcons(c);
                    return c.getResources().getDrawable(com.janrain.android.engage.R.drawable.icon_unknown);
                }

                return new BitmapDrawable(c.getResources(), icon);
            }
            catch (FileNotFoundException e) {
                //downloadIcons(c);

                return c.getResources().getDrawable(com.janrain.android.engage.R.drawable.icon_unknown);
            }
    }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(mResourceId, null);
                Log.i(TAG, "[getView] with null converView");
            } else Log.i(TAG, "[getView] with non null convertView");

            ImageView icon = (ImageView)v.findViewById(R.id.row_profile_provider_icon);
            TextView name = (TextView)v.findViewById(R.id.row_profile_preferred_username_label);
            TextView timestamp = (TextView)v.findViewById(R.id.row_profile_timestamp_label);
            Button deleteRow = (Button)v.findViewById(R.id.row_delete_button);

            LoginSnapshot snapshot = getItem(position);

            Log.d(TAG, "[getView] for row " + ((Integer) position).toString() + ": " + snapshot.getDisplayName());

            icon.setImageDrawable(getProviderIconDrawable(getContext(), snapshot.getProvider()));
            name.setText(snapshot.getDisplayName());
            timestamp.setText(snapshot.getTimeStamp());

            deleteRow.setTag(position);
            deleteRow.setOnClickListener(this);

            if (mEditing)
                deleteRow.setVisibility(View.VISIBLE);
            else
                deleteRow.setVisibility(View.GONE);
            
            return v;
        }

        public void onClick(View view) {
            Integer position = ((Integer)view.getTag());
            mProfileData.deleteLoginSnapshotAtPosition(position);
            this.notifyDataSetChanged();
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = ProfilesActivity.class.getSimpleName();

    private static final String ENGAGE_APP_ID = "";
    private static final String ENGAGE_TOKEN_URL = "";

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

    private boolean mEditing;
    private MenuItem mEditProfilesButton;
    private MenuItem mClearAllProfilesButton;

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

    private String readAsset(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            return null;
        }
    }


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

        String engageAppId = TextUtils.isEmpty(readAsset("app_id.txt")) ?
                null : readAsset("app_id.txt").trim();
        String engageTokenUrl = null;

//        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);
        mEngage = JREngage.initInstance(this, engageAppId, engageTokenUrl, this);        

        mEditing = false;

        mEditProfilesButton = (MenuItem)findViewById(R.id.edit_profiles);
        mClearAllProfilesButton = (MenuItem)findViewById(R.id.delete_all_profiles);

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
        mProfileData.setCurrentProfileByIdentifier(snapshot.getIdentifier());
        this.startActivity(new Intent(this, ProfileDetailActivity.class));
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_managing_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mEditing)
            menu.findItem(R.id.edit_profiles).setTitle("Edit Profiles");
        else
            menu.findItem(R.id.edit_profiles).setTitle("Done Editing");

        return true;
    }

    /**
    * This hook is called whenever an item in your options menu is selected.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profiles:
                if (!mEditing) {
                    mEditing = true;
                    mAddProfile.setText("Done Editing");
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
                else {
                    mEditing = false;
                    mAddProfile.setText("Add Another Profile");
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
            case R.id.delete_all_profiles:
                mEditing = false;
                mAddProfile.setText("Add Another Profile");
                mProfileData.deleteAllProfiles();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        if (mEditing) {
            mEditing = false;
            mAddProfile.setText("Add Another Profile");
            mAdapter.notifyDataSetChanged();
        }
        else {
            mEngage.showAuthenticationDialog();
        }
    }
}