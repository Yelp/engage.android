package com.janrain.android.quicksignin;

import android.content.Context;
import java.text.DateFormat;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.Archiver;

import java.util.*;

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
    private static final String ARCHIVE_LOGIN_SNAPSHOTS = "loginSnapshots";


    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private Context mContext;

    private ArrayList<LoginSnapshot> mLoginSnapshots;
    private HashMap<String, JRDictionary> mProfiles;
    private JRDictionary mCurrentProfile;
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

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
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private ProfileData() {
        if (Config.LOGD) {
            Log.d(TAG, "[ctor] creating instance.");
        }

        mLoginSnapshots = (ArrayList<LoginSnapshot>)Archiver.load(ARCHIVE_LOGIN_SNAPSHOTS);
        if (mLoginSnapshots == null)
            mLoginSnapshots = new ArrayList<LoginSnapshot>();

        mProfiles = (HashMap<String, JRDictionary>)Archiver.load(ARCHIVE_ALL_PROFILES);
        if (mProfiles == null)
            mProfiles = new HashMap<String, JRDictionary>();
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    public ArrayList<LoginSnapshot> getProfilesList() {
        return mLoginSnapshots;
    }

    public void addProfile(JRDictionary auth_info, String provider) {
//        Calendar rightNow = Calendar.getInstance();
//        Date date = new Date(location.getTime());
//        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
//        mTimeText.setText("Time: " + dateFormat.format(date));

        String timestamp = DateFormat.getDateTimeInstance().format(new Date());//rightNow.toString();//"12:00 PM April 14th, 2011";

        JRDictionary profile = (auth_info == null) ? null : auth_info.getAsDictionary("profile");
        String identifier = (auth_info == null) ? null : auth_info.getAsString("identifier");
        String displayName = (profile == null) ? null : profile.getAsString("displayName");

        LoginSnapshot snapshot = new LoginSnapshot(timestamp, identifier, provider, displayName);
        mLoginSnapshots.add(snapshot);

        mProfiles.put(identifier, profile);

        Archiver.save(ARCHIVE_ALL_PROFILES, mProfiles);
        Archiver.save(ARCHIVE_LOGIN_SNAPSHOTS, mLoginSnapshots);
    }

    public void setCurrentProfileByIdentifier(String identifier) {
        mCurrentProfile = mProfiles.get(identifier);
    }

    public JRDictionary getCurrentProfile() {
        return mCurrentProfile;
    }

//    public JRDictionary getProfileForIdentifier(String identifier) {
//        return mProfiles.get(identifier);
//    }
}

