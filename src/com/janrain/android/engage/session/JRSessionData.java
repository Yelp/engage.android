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
*/
package com.janrain.android.engage.session;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.types.JRProviderList;
import com.janrain.android.engage.utils.Archiver;
import org.apache.http.HttpRequest;

import android.text.TextUtils;

import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

public class JRSessionData implements JRConnectionManagerDelegate {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRSessionData.class.getSimpleName();
    
	private static JRSessionData sInstance;

    private static final String ARCHIVE_USERS = "users";
    private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
    private static final String ARCHIVE_BASIC_PROVIDERS = "basicProviders";
    private static final String ARCHIVE_SOCIAL_PROVIDERS = "socialProviders";
    private static final String ARCHIVE_ICONS_STILL_NEEDED = "iconsStillNeeded";
    private static final String ARCHIVE_PROVIDERS_WITH_ICONS = "providersWithIcons";
    private static final String ARCHIVE_BASE_URL = "baseUrl";
    private static final String ARCHIVE_HIDE_POWERED_BY = "hidePoweredBy";
    private static final String ARCHIVE_LAST_USED_SOCIAL = "lastUsedSocialProvider";
    private static final String ARCHIVE_LAST_USED_BASIC = "lastUsedBasicProvider";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	public static JRSessionData getInstance() {
		return sInstance;
	}
	
	public static JRSessionData getInstance(String appId, String tokenUrl, 
			JRSessionDelegate delegate) {
		
		if (sInstance != null) {
            if (Config.LOGD) {
                Log.d(TAG, "[getInstance] returning existing instance.");
            }
			return sInstance;
		}
		
		if (TextUtils.isEmpty(appId)) {
            Log.w(TAG, "[getInstance] null instance w/ null appId specified.");
			return null;
		}
		
		sInstance = new JRSessionData(appId, tokenUrl, delegate);
        if (Config.LOGD) {
            Log.d(TAG, "[getInstance] returning new instance.");
        }
		return sInstance;
	}
	
    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

	private ArrayList<JRSessionDelegate> mDelegates;
	
	private JRProvider mCurrentProvider;
	private String mReturningBasicProvider;
	private String mReturningSocialProvider;
	
	private JRDictionary mAllProviders;
	private JRProviderList mBasicProviders;
	private JRProviderList mSocialProviders;
	private JRDictionary mAuthenticatedUsersByProvider;
	
	private String mSavedConfigurationBlock;
	private String mNewEtag;
    private String mGitCommit;

    private JRProviderList mProvidersWithIcons;
    private JRDictionary mIconsStillNeeded;

	private JRActivityObject mActivity;
	
	private String mTokenUrl;
	private String mBaseUrl;
	private String mAppId;
    private String mDevice;

    private boolean mHidePoweredBy;

	private boolean mAlwaysForceReauth;
	private boolean mForceReauth;

	private boolean mSocialSharing;
	
	private boolean mDialogIsShowing;
	private JREngageError mError;
	
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private JRSessionData(String appId, String tokenUrl, JRSessionDelegate delegate) {
        if (Config.LOGD) {
            Log.d(TAG, "[ctor] creating instance.");
        }

		mDelegates = new ArrayList<JRSessionDelegate>();
		mDelegates.add(delegate);
		
		mAppId = appId;
		mTokenUrl = tokenUrl;

        mAuthenticatedUsersByProvider = new JRDictionary();
        mAllProviders = new JRDictionary();

        // load cached data
        Context ctx = JREngage.getContext();

        // Load dictionary of authenticated users.  If the dictionary is not found, the
        // archiver will return a new (empty) list.
        mAuthenticatedUsersByProvider = JRDictionary.unarchive(ctx, ARCHIVE_USERS);

        // Load the list of all providers
        mAllProviders = JRDictionary.unarchive(ctx, ARCHIVE_ALL_PROVIDERS);

        // Load the list of basic providers
        mBasicProviders = JRProviderList.unarchive(ctx, ARCHIVE_BASIC_PROVIDERS);

        // Load the list of social providers
        mSocialProviders = JRProviderList.unarchive(ctx, ARCHIVE_SOCIAL_PROVIDERS);

        // Load the list of icons that the library should re-attempt to download, in case
        // previous attempts failed for whatever reason
        mIconsStillNeeded = JRDictionary.unarchive(ctx, ARCHIVE_ICONS_STILL_NEEDED);

        // Load the set of providers that already have all of their icons; checking this list
        // is faster than checking for the icons themselves
        mProvidersWithIcons = JRProviderList.unarchive(ctx, ARCHIVE_PROVIDERS_WITH_ICONS);

        // Load the base url and whether or not we need to hide the tagline.
        mBaseUrl = Archiver.loadString(ctx, ARCHIVE_BASE_URL);
        mHidePoweredBy = Archiver.loadBoolean(ctx, ARCHIVE_HIDE_POWERED_BY);

        // And load the last used basic and social providers
        mReturningSocialProvider = Archiver.loadString(ctx, ARCHIVE_LAST_USED_SOCIAL);
        mReturningBasicProvider = Archiver.loadString(ctx, ARCHIVE_LAST_USED_BASIC);

        mError = startGetConfiguration();
	}
	
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // DELEGATE METHODS
    // ------------------------------------------------------------------------

	public void connectionDidFail(JREngageError error, HttpRequest request,
			Object userdata) {
		// TODO Auto-generated method stub
		
	}

	public void connectionDidFinishLoading(String payload, HttpRequest request,
			Object userdata) {
		// TODO Auto-generated method stub
		
	}

	public void connectionDidFinishLoading(HttpResponseHeaders fullResponse,
			byte[] payload, HttpRequest request, Object userdata) {
		// TODO Auto-generated method stub
		
	}

	public void connectionWasStopped(Object userdata) {
		// TODO Auto-generated method stub
		
	}

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    private JREngageError startGetConfiguration() {
        String nameAndVersion = appNameAndVersion();

        // stub
        return null;
    }

    private String appNameAndVersion() {
        final String FMT = "appName=%s.%s&version=%d_%s";

        Context ctx = JREngage.getContext();
        PackageManager pkgMgr = ctx.getPackageManager();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pkgMgr.getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "[appNameAndVersion] package manager issue -> ", e);
        }

        return (pkgInfo != null)
                ? String.format(FMT,
                    pkgInfo.packageName,
                    pkgInfo.applicationInfo.nonLocalizedLabel,
                    pkgInfo.versionCode,
                    pkgInfo.versionName)
                : "";
    }
}
