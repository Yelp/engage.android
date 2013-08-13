/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android.quicksignin;

import android.util.Log;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.utils.Archiver;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileData {
    private static final String TAG = ProfileData.class.getSimpleName();

    private static ProfileData sInstance;

    private static final String ARCHIVE_ALL_PROFILES = "allProfiles";
    private static final String ARCHIVE_LOGIN_SNAPSHOTS = "loginSnapshots";

    private List<SigninSnapshot> mSigninSnapshots = new ArrayList<SigninSnapshot>();
    private Map<String, JRDictionary> mProfiles;
    private JRDictionary mCurrentProfile;

    public static ProfileData getInstance() {

        if (sInstance != null) {
            Log.d(TAG, "[getInstance] returning existing instance.");

            return sInstance;
        }

        sInstance = new ProfileData();
        Log.d(TAG, "[getInstance] returning new instance.");

        return sInstance;
    }

    private ProfileData() {
        Log.d(TAG, "[ctor] creating instance.");

        try {
            mSigninSnapshots = Archiver.load(ARCHIVE_LOGIN_SNAPSHOTS);
            mProfiles = Archiver.load(ARCHIVE_ALL_PROFILES);
        } catch (Archiver.LoadException e) {
            mSigninSnapshots = new ArrayList<SigninSnapshot>();
            mProfiles = new HashMap<String, JRDictionary>();
        }
    }

    public List<SigninSnapshot> getProfilesList() {
        return mSigninSnapshots;
    }

    public void addProfile(JRDictionary auth_info, String provider) {
        String timestamp = DateFormat.getDateTimeInstance().format(new Date());

        JRDictionary profile = (auth_info == null) ? null : auth_info.getAsDictionary("profile");
        String identifier = (profile == null) ? null : profile.getAsString("identifier");
        String displayName = (profile == null) ? null : profile.getAsString("displayName");

        if (profile != null) {
            if (profile.containsKey("name")) profile.put("name", flattenName(profile));
            if (profile.containsKey("address")) profile.put("address", flattenAddress(profile));
        }

        SigninSnapshot snapshot = new SigninSnapshot(timestamp, identifier, provider, displayName);
        mSigninSnapshots.add(0, snapshot);

        mProfiles.put(identifier, profile);

        Archiver.asyncSave(ARCHIVE_ALL_PROFILES, mProfiles);
        Archiver.asyncSave(ARCHIVE_LOGIN_SNAPSHOTS, mSigninSnapshots);
    }

    public void deleteLoginSnapshotAtPosition(int position) {
        mSigninSnapshots.remove(position);
        Archiver.asyncSave(ARCHIVE_ALL_PROFILES, mProfiles);
        Archiver.asyncSave(ARCHIVE_LOGIN_SNAPSHOTS, mSigninSnapshots);
    }

    public void deleteAllProfiles() {
        mSigninSnapshots.clear();
        mProfiles.clear();

        Archiver.asyncSave(ARCHIVE_ALL_PROFILES, mProfiles);
        Archiver.asyncSave(ARCHIVE_LOGIN_SNAPSHOTS, mSigninSnapshots);
    }

    public void setCurrentProfileByIdentifier(String identifier) {
        mCurrentProfile = mProfiles.get(identifier);
    }

    public JRDictionary getCurrentProfile() {
        return mCurrentProfile;
    }

    private String flattenName(JRDictionary profile) {
        JRDictionary name = profile.getAsDictionary("name");

        if (name.containsKey("formatted"))
            return name.getAsString("formatted");
        else
            return (name.containsKey("honorificPrefix") ?
                    name.getAsString("honorificPrefix") + " " : "") +
                    (name.containsKey("givenName") ?
                            name.getAsString("givenName") + " " : "") +
                    (name.containsKey("middleName") ?
                            name.getAsString("middleName") + " " : "") +
                    (name.containsKey("familyName") ?
                            name.getAsString("familyName") + " " : "") +
                    (name.containsKey("honorificSuffix") ?
                            name.getAsString("honorificSuffix") : "");
    }

    private String flattenAddress(JRDictionary profile) {
        JRDictionary address = profile.getAsDictionary("address");

        if (address.containsKey("formatted"))
            return address.getAsString("formatted");
        else
            return
                    (address.containsKey("streetAddress") ?
                            address.getAsString("streetAddress") + " " : "") +
                            (address.containsKey("locality") ?
                                    address.getAsString("locality") + " " : "") +
                            (address.containsKey("region") ?
                                    address.getAsString("region") + " " : "") +
                            (address.containsKey("postalCode") ?
                                    address.getAsString("postalCode") + " " : "") +
                            (address.containsKey("country") ?
                                    address.getAsString("country") : "");
    }
}

