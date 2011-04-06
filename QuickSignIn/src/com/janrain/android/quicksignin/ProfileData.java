package com.janrain.android.quicksignin;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.utils.Archiver;
import com.janrain.android.engage.utils.ListUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/6/11
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProfileData {
    private static final String TAG = ProfileData.class.getSimpleName();

	private static ProfileData sInstance;


    private static final String ARCHIVE_ALL_PROFILES = "allProfiles";

    public static ProfileData getInstance() {

        if (sInstance != null) {
            if (Config.LOGD) {
                Log.d(TAG, "[getInstance] returning existing instance.");
                //todo this should probably be an error, as it ignores the parameters instead of
                //reinstantiating the library
            }
            return sInstance;
        }

        sInstance = new ProfileData();
        if (Config.LOGD) {
            Log.d(TAG, "[getInstance] returning new instance.");
        }
        return sInstance;
    }

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private ArrayList<String> mProfiles;
    private String mCurrentProfile;
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private ProfileData() {
        if (Config.LOGD) {
            Log.d(TAG, "[ctor] creating instance.");
        }

//            ApplicationInfo ai = JREngage.getContext().getApplicationInfo();
//            String appName = JREngage.getContext().getPackageManager().getApplicationLabel(ai).toString();
//            try { mUrlEncodedAppName = URLEncoder.encode(appName, "UTF-8"); }
//            catch (UnsupportedEncodingException e) { Log.e(TAG, e.toString()); }
//
//            String libraryVersion = JREngage.getContext().getString(R.string.jr_engage_version);
//            String diskVersion = Prefs.getAsString("JREngageVersion", "");
//
//            if (diskVersion.equals(libraryVersion)) {
            // Load the list of basic providers
//            mProfiles = (ArrayList<String>) Archiver.load(ARCHIVE_ALL_PROFILES);
//            if (Config.LOGD) {
//                if (ListUtils.isEmpty(mProfiles)) {
//                    Log.d(TAG, "[ctor] basic providers is empty");
//                } else {
//                    Log.d(TAG, "[ctor] basic providers: [" + TextUtils.join(",", mProfiles) + "]");
//                }
//            }
//            }
//            else {
//                mProfiles = new ArrayList<String>();
//            }

        mProfiles = new ArrayList<String>();
        mProfiles.add("Alice");
        mProfiles.add("Bob");
        mProfiles.add("Carol");
        mProfiles.add("Dave");
        mProfiles.add("Edith");
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    public ArrayList<String> getProfilesList() {
        return mProfiles;
    }

    public void addProfile(String profile) {
        mProfiles.add(profile);
    }

    public void setCurrentProfile(String profile) {
        mCurrentProfile = profile;
    }

    public String getCurrentProfile() {
        return mCurrentProfile;
    }
}
