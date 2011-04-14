package com.janrain.android.quicksignin;

public class LoginSnapshot {
    private String mTimeStamp;
    private String mIdentifier;
    private String mProvider;
    private String mDisplayName;

    public LoginSnapshot(String timeStamp, String identifier, String provider, String displayName) {
        mTimeStamp = timeStamp;
        mIdentifier = identifier;
        mProvider = provider;
        mDisplayName = displayName;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public String getProvider() {
        return mProvider;
    }

    public String getDisplayName() {
        return mDisplayName;
    }
}
