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

import android.app.*;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.Button;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.R;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.session.*;
import com.janrain.android.engage.types.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishing UI
 */
public class JRPublishActivity extends TabActivity implements TabHost.OnTabChangeListener {
    private static final int FAILURE_DIALOG = 1;
    private static final int SUCCESS_DIALOG = 2;

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    /**
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     */

//    private class FinishReceiver extends BroadcastReceiver {
//
//        private final String TAG = JRPublishActivity.TAG + "-" + FinishReceiver.class.getSimpleName();
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String target = intent.getStringExtra(
//                    JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
//            if (JRPublishActivity.class.toString().equals(target)) {
//                tryToFinishActivity();
//                Log.i(TAG, "[onReceive] handled");
//            } else if (Config.LOGD) {
//                Log.i(TAG, "[onReceive] ignored");
//            }
//        }
//    }

    /**
     * UI display attributes for for each of the supported providers.
     * Used to map between
     */

    private static class ProviderDisplayInfo {
        // whether or not activity info section is displayed
        private boolean mIsMediaContentVisible;
        // provider icon resource id
        private int mIconResId;
        // share section background color
        private int mShareBgColorResId;
        // share button resource id
        private int mShareButtonResId;

        ProviderDisplayInfo(boolean isInfoVisible, int iconResId, int shareBgColorResId, int shareBtnResId) {
            mIsMediaContentVisible = isInfoVisible;
            mIconResId = iconResId;
            mShareBgColorResId = shareBgColorResId;
            mShareButtonResId = shareBtnResId;
        }

        boolean getIsMediaContentVisible() {
            return mIsMediaContentVisible;
        }
        int getIconResId() {
            return mIconResId;
        }
        int getShareBgColorResId() {
            return mShareBgColorResId;
        }
        int getShareBtnResId() {
            return mShareButtonResId;
        }
    }


    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRPublishActivity.class.getSimpleName();

    private static HashMap<String, ProviderDisplayInfo> PROVIDER_MAP;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    static {
        PROVIDER_MAP = new HashMap<String, ProviderDisplayInfo>();
        PROVIDER_MAP.put("Facebook",
                new ProviderDisplayInfo(
                        true,
                        R.drawable.icon_facebook_30x30,
                        R.color.bg_clr_facebook,
                        R.drawable.button_facebook_280x40));
        PROVIDER_MAP.put("Twitter",
                new ProviderDisplayInfo(
                        false,
                        R.drawable.icon_twitter_30x30,
                        R.color.bg_clr_twitter,
                        R.drawable.button_twitter_280x40));
        PROVIDER_MAP.put("MySpace",
                new ProviderDisplayInfo(
                        false,
                        R.drawable.icon_myspace_30x30,
                        R.color.bg_clr_myspace,
                        R.drawable.button_myspace_280x40));
        PROVIDER_MAP.put("LinkedIn",
                new ProviderDisplayInfo(
                        false,
                        R.drawable.icon_linkedin_30x30,
                        R.color.bg_clr_linkedin,
                        R.drawable.button_linkedin_280x40));
    }

    private static final Map<String, Integer> icon_resources = new HashMap<String, Integer>(){
       {
           put("facebook", R.drawable.ic_facebook_tab);
           put("linkedin", R.drawable.ic_linkedin_tab);
           put("myspace", R.drawable.ic_myspace_tab);
           put("twitter", R.drawable.ic_twitter_tab);
           put("yahoo", R.drawable.ic_yahoo_tab);
       }
    };

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    //private FinishReceiver mFinishReceiver;

    //reference to the library model
    private JRSessionData mSessionData;

    //
    private JRProvider mSelectedProvider;
    private JRAuthenticatedUser mLoggedInUser;
    private JRSessionDelegate mSessionDelegate;
    private JRActivityObject mActivityObject;

    //
    private String mShortenedActivityURL = null;//"http://fakeshort"; //null if it hasn't been shortened
    private int mMaxCharacters;

    //UI state variables
    private boolean mUserHasEditedText = false;
    private boolean mWeHaveJustAuthenticated = false;
    private boolean mWeAreCurrentlyPostingSomething = false;

    //UI views
    private RelativeLayout mMediaContentView;
    private TextView mCharacterCountView;
    private TextView mPreviewLabelView;
    private ImageView mProviderIcon;
    private EditText mUserCommentView;
    private LinearLayout mShareButtonContainer;
    private LinearLayout mProfilePicAndButtonsHorizontalLayout;
    private ImageView mProfilePicImage;
    private LinearLayout mNameAndSignOutContainer;
    private TextView mUserName;
    private Button mSignOutButton;
    private Button mJustShareButton;
    private Button mConnectAndShareButton;

    //
    private SharedLayoutHelper mLayoutHelper;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_activity);

        mSessionData = JRSessionData.getInstance();
        mActivityObject = mSessionData.getActivity();

        mSessionDelegate = createSessionDelegate();
        mSessionData.addDelegate(mSessionDelegate);

        if (mSessionData.getHidePoweredBy()) {
            TextView poweredBy = (TextView)findViewById(R.id.powered_by_text);
            poweredBy.setVisibility(View.GONE);
        }

        TextView title = (TextView)findViewById(R.id.header_text);
        title.setText(getString(R.string.publish_activity_title));

        mMediaContentView = (RelativeLayout) findViewById(R.id.media_content_view);
        mCharacterCountView = (TextView) findViewById(R.id.character_count_view);
        mProviderIcon = (ImageView) findViewById(R.id.provider_icon);
        mShareButtonContainer = (LinearLayout) findViewById(R.id.share_button_container);
        mUserCommentView = (EditText) findViewById(R.id.edit_comment);
        mPreviewLabelView = (TextView) findViewById(R.id.preview_label_view);
        mShareButtonContainer = (LinearLayout) findViewById(R.id.share_button_container);
        mProfilePicAndButtonsHorizontalLayout = (LinearLayout) findViewById(R.id.profile_pic_and_buttons_horizontal_layout);
        mProfilePicImage = (ImageView) findViewById(R.id.profile_pic);
        mNameAndSignOutContainer = (LinearLayout) findViewById(R.id.name_and_sign_out_container);
        mUserName = (TextView) findViewById(R.id.user_name);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mJustShareButton = (Button) findViewById(R.id.just_share_button);
        mConnectAndShareButton = (Button) findViewById(R.id.connect_and_share_button);

        mConnectAndShareButton.setOnClickListener(mShareButtonListener);
        mJustShareButton.setOnClickListener(mShareButtonListener);
        mUserCommentView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                updateUserCommentView();
                updateCharacterCount();
            }
        });

        mLayoutHelper = new SharedLayoutHelper(this);

        // TODO: When focus leaves the usercontentview and the text is empty, go back to setting the
        // previewlabel to action (if applicable) and the usercontentview's default text to "please enter text"

        fetchShortenedURLs();
        loadViewElementPropertiesWithActivityObject();

        // TODO consider the case of the first usage of the library when the config call hasn't yet returned
        // and display an noninteractive view of the activity or something like the iOS lib
        configureTabs();
    }

    private void configureTabs() {
        // TODO: If no providers

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost(); // The activity TabHost
        tabHost.setup();
        TabHost.TabSpec spec;           // Reused TabSpec for each tab

        int currentIndex = 0, indexOfLastUsedProvider = 0;
        for (JRProvider provider : mSessionData.getSocialProviders())
        {
            // TODO: If provider is NULL

            spec = tabHost.newTabSpec(provider.getName()).setIndicator(provider.getFriendlyName(),
                              res.getDrawable(icon_resources.get(provider.getName())))
                          .setContent(R.id.tab_view_content);
            tabHost.addTab(spec);

            if (provider.getName().equals(mSessionData.getReturningSocialProvider()))
                indexOfLastUsedProvider = currentIndex;

            currentIndex++;
        }

        tabHost.setOnTabChangedListener(this);
        tabHost.setCurrentTab(indexOfLastUsedProvider);
        onTabChanged(tabHost.getCurrentTabTag()); //when TabHost is constructed it defaults to tab 0, so if
                                                  //indexOfLastUsedProvider is 0, the tab change listener won't be
                                                  //invoked, so we call it manually to ensure it is called.  (it's
                                                  //idempotent)
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (mFinishReceiver == null) {
//            mFinishReceiver = new FinishReceiver();
//            registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSessionData.removeDelegate(mSessionDelegate);
//        unregisterReceiver(mFinishReceiver);
    }

    private View.OnClickListener mShareButtonListener = new View.OnClickListener() {
        public void onClick(View view) {
        //    weAreCurrentlyPostingSomething = true;

            if (!mUserCommentView.getText().equals(""))
                mActivityObject.setUserGeneratedContent(mUserCommentView.getText().toString());

            // Current Provider is a variable within session data and contains the provider we are currently authenticating with
            // TODO: Rename this to be more meaningful
            //mSessionData.setCurrentProvider(mSelectedProvider);
            showViewIsLoading(true);

            if (mLoggedInUser == null) {
                authenticateUser();
            } else {
                shareActivity();
            }
        }
    };

    /**
     * Invoked by JRUserInterfaceMaestro via FinishReceiver to close this activity.
     */

//    public void tryToFinishActivity() {
//        Log.i(TAG, "[tryToFinishActivity]");
//        finish();
//    }


    public boolean activityUrlAffectsCharacterCountForSelectedProvider() {
        //twitter + myspace -> true
        return (mSelectedProvider.getSocialSharingProperties().getAsBoolean("url_reduces_max_chars") &&
            mSelectedProvider.getSocialSharingProperties().getAsString("shows_url_as").equals("url"));
    }

    public void updateUserCommentView() {
        if (!mUserHasEditedText) mUserHasEditedText = true;

        if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action"))
            updatePreviewLabelWhenContentReplacesAction();
    }

    public void updatePreviewLabelWhenContentReplacesAction() {
        CharSequence newText = (!mUserCommentView.getText().toString().equals("")) ?
                  mUserCommentView.getText()
                : mActivityObject.getAction();

        if (activityUrlAffectsCharacterCountForSelectedProvider()) {
            mPreviewLabelView.setText("mcspilli " + newText + " "
                    + ((mShortenedActivityURL != null) ? mShortenedActivityURL : R.string.shortening_url));
        } else {
            mPreviewLabelView.setText("mcspilli " + newText);
        }
    }

    //
    // callback handler to update our character count
    //

    public void updateCharacterCount() {
        //TODO make negative numbers red, verify correctness of the 0 remaining characters edge case

        if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) { //twitter, myspace, linkedin
            if (activityUrlAffectsCharacterCountForSelectedProvider() && mShortenedActivityURL == null) { //twitter, myspace
                // Do nothing yet...
            } else {
                int preview_length = mPreviewLabelView.getText().length();
                mCharacterCountView.setText("Remaining characters: " + (mMaxCharacters - preview_length));
                Log.d(TAG, "uCC1: " + (mMaxCharacters - preview_length));
            }
        } else { //facebook, yahoo
            int comment_length = mUserCommentView.getText().length();
            mCharacterCountView.setText("Remaining characters: " + (mMaxCharacters - comment_length));
        }
    }

    //
    // View.OnClickListener
    // used to handle clicks on the share button
    //

    private void logUserOutForProvider(String provider) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[0].getMethodName());
    }

    private void showActivityAsShared(boolean shared) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[0].getMethodName());
    }

    private void showUserAsLoggedIn(boolean loggedIn) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[0].getMethodName());

        int visibleIfLoggedIn = loggedIn ? View.VISIBLE : View.GONE;
        int visibleIfNotLoggedIn = !loggedIn ? View.VISIBLE : View.GONE;

        mJustShareButton.setVisibility(visibleIfLoggedIn);
        mProfilePicImage.setVisibility(visibleIfLoggedIn);
        mNameAndSignOutContainer.setVisibility(visibleIfLoggedIn);

        mConnectAndShareButton.setVisibility(visibleIfNotLoggedIn);

        //[myTriangleIcon setFrame:CGRectMake(loggedIn ? 230 : 151, 0, 18, 18)];
    }

    private void loadUserNameAndProfilePicForUserForProvider(JRAuthenticatedUser user, String providerName) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[0].getMethodName());

        if (user == null || providerName == null) {
            mUserName.setText("");
            //[self setButtonImage:myProfilePic toData:null andSetLoading:myProfilePicActivityIndicator toLoading:NO];
            return;
        }

        mUserName.setText(user.getPreferredUsername());

        // TODO
        ///NSData *cachedProfilePic = [cachedProfilePics objectForKey:providerName];
        Image cachedProfilePic = null;

        if (cachedProfilePic != null)
            ;//[self setButtonImage:myProfilePic toData:cachedProfilePic andSetLoading:myProfilePicActivityIndicator toLoading:NO];
        else if (user.getPhoto() != null)
            ;//[self fetchProfilePicFromUrl:user.photo forProvider:providerName];
        else
            ;//[self setProfilePicToDefaultPic];

    }

    private void showViewIsLoading(boolean loading) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[0].getMethodName());

        if (loading)
            mLayoutHelper.showProgressDialog();
        else
            mLayoutHelper.dismissProgressDialog();
    }

    private void loadViewElementPropertiesWithActivityObject() {
        mUserCommentView.setHint(R.string.please_enter_text);

        mPreviewLabelView.setText(mActivityObject.getAction());

        JRMediaObject mo = null;
        if (mActivityObject.getMedia().size() > 0) mo = mActivityObject.getMedia().get(0);

        ImageView mci = (ImageView) findViewById(R.id.media_content_image);
        TextView  mcd = (TextView)  findViewById(R.id.media_content_description);
        TextView  mct = (TextView)  findViewById(R.id.media_content_title);

        //set the media_content_view = a thumbnail of the media
        try {
            if (mo != null) if (mo.hasThumbnail()) {
                mci.setImageBitmap(BitmapFactory.decodeStream((new URL(mo.getThumbnail())).openStream()));
                Log.d(TAG, "media image url: " + mo.getThumbnail());
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
        }

        //set the media content description
        mcd.setText(mActivityObject.getDescription());

        //set the media content title
        mct.setText(mActivityObject.getTitle());
    }

    private void configureViewElementsBasedOnProvider() {
        // TODO:  check "hasEditedUserContentForActivityAlready"

        // TODO: make this match the docs for the iphone activity object:
        // https://rpxnow.com/docs/iphone_api/interface_j_r_activity_object.html#a2e4ff78f83d0f353f8e0c17ed48ce0ab
        JRDictionary socialSharingProperties = mSelectedProvider.getSocialSharingProperties();

        if (socialSharingProperties.getAsBoolean("content_replaces_action"))
            updatePreviewLabelWhenContentReplacesAction();
        else
            mPreviewLabelView.setText("mcspilli " + mActivityObject.getAction());

        mMaxCharacters = mSelectedProvider.getSocialSharingProperties().getAsInt("max_characters");
        if (mMaxCharacters != -1) {
            mCharacterCountView.setVisibility(View.VISIBLE);
        } else
            mCharacterCountView.setVisibility(View.GONE);

        updateCharacterCount();

        boolean can_share_media = mSelectedProvider.getSocialSharingProperties().getAsBoolean("can_share_media");

        //switch on or off the media content view based on the presence of media and ability to display it
        boolean showMediaContentView = mActivityObject.getMedia().size() > 0 && can_share_media;
        mMediaContentView.setVisibility(showMediaContentView ? View.VISIBLE : View.GONE);

        //switch on or off the action label view based on the provider accepting an action
//        boolean contentReplacesAction = socialSharingProperties.getAsBoolean("content_replaces_action");
//        mPreviewLabelView.setVisibility(contentReplacesAction ? View.GONE : View.VISIBLE);
    }

    private void configureLoggedInUserBasedOnProvider() {
        mLoggedInUser = mSessionData.authenticatedUserForProvider(mSelectedProvider);

        if (mLoggedInUser != null) {
            loadUserNameAndProfilePicForUserForProvider(mLoggedInUser, mSelectedProvider.getName());
            showUserAsLoggedIn(true);
        } else {
            loadUserNameAndProfilePicForUserForProvider(null, null);
            showUserAsLoggedIn(false);
        }
    }
//    shareButtonPressed(Button button) {
//    }

    //
    // Helper methods
    //

    //
    //populates the UI elements with the properties of the activity object
    //

    public void onTabChanged(String tabId) {
        Log.d(TAG, "[onTabChange]: " + tabId);

        mSessionData.setCurrentProviderByName(tabId);

        mSelectedProvider = mSessionData.getCurrentProvider();

        //XXX TabHost is setting our FrameLayout's only child to GONE when loading
        //XXX could be a bug in the TabHost, or could be a misuse of the TabHost system, this is a workaround
        findViewById(R.id.tab_view_content).setVisibility(View.VISIBLE);

        configureViewElementsBasedOnProvider();
        configureLoggedInUserBasedOnProvider();

        //updateCharacterCount();
    }

    private void authenticateUser() {
     /* Set weHaveJustAuthenticated to true, so that when this view returns (for whatever reason... successful auth
        user canceled, etc), the view will know that we just went through the authentication process. */
        mWeHaveJustAuthenticated = true;

     /* If the selected provider requires input from the user, go to the user landing view. Or if
        the user started on the user landing page, went back to the list of providers, then selected
        the same provider as their last-used provider, go back to the user landing view. */
        if (mSelectedProvider.requiresInput()) {
            JRUserInterfaceMaestro.getInstance().showUserLanding();
        } else { /* Otherwise, go straight to the web view. */
            JRUserInterfaceMaestro.getInstance().showWebView();
        }
    }

    private void shareActivity() {
        Log.d(TAG, Thread.currentThread().getStackTrace()[0].getLineNumber() + "");
        mSessionData.setCurrentProvider(mSelectedProvider);
        mSessionData.shareActivityForUser(mLoggedInUser);
    }

    private void fetchShortenedURLs() {
        try {
            final String jsonEncodedActivityUrl = (new JSONStringer()).array().value(mActivityObject.getUrl()).endArray().toString();
            String htmlEncodedJsonEncodedUrl = URLEncoder.encode(jsonEncodedActivityUrl, "UTF8");
            final String urlString = mSessionData.getBaseUrl() + "/openid/get_urls?urls=" + htmlEncodedJsonEncodedUrl;

            JRConnectionManagerDelegate jrcmd = new JRCMD() {
                public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
                    //Log.d(TAG, "Thread: " + Thread.currentThread().toString());
                    try {
                        Log.d(TAG, "short " + payload);
                        JSONObject jso = (JSONObject) (new JSONTokener(payload)).nextValue();
                        jso = jso.getJSONObject("urls");
                        mShortenedActivityURL = jso.getString(mActivityObject.getUrl());
                    } catch (JSONException e) {
                        //todo fail more gracefully when we don't get a good result from rpx?
                        throw new RuntimeException(e);
                    }

                    if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
                        updatePreviewLabelWhenContentReplacesAction();
                        updateCharacterCount();
                    }
                }
            };

            JRConnectionManager.createConnection(urlString, jrcmd, false, null);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case SUCCESS_DIALOG:
                return new AlertDialog.Builder(JRPublishActivity.this).setMessage("Success!")
                                         .setCancelable(false)
                                         .setPositiveButton("Dismiss", null)
                                         .create();
            case FAILURE_DIALOG:
                return new AlertDialog.Builder(JRPublishActivity.this).setMessage("Error: ")
                                         .setPositiveButton("Dismiss", null)
                                         .create();
        }
        return null;
    }

    private JRSessionDelegate createSessionDelegate() {
        return new JRSD() {
            public void authenticationDidRestart() {
                Log.d(TAG, "[authenticationDidRestart]");

                mWeAreCurrentlyPostingSomething = false;
                mWeHaveJustAuthenticated = false;
            }

            public void authenticationDidCancel() {
                Log.d(TAG, "[authenticationDidCancel]");
                mWeAreCurrentlyPostingSomething = false;
                mWeHaveJustAuthenticated = false;
            }

            public void authenticationDidFail(JREngageError error, String provider) {
                Log.d(TAG, "[authenticationDidFail]");
                mWeHaveJustAuthenticated = false;
                mWeAreCurrentlyPostingSomething = false;
            }

            public void authenticationDidComplete(JRDictionary profile, String provider) {
                Log.d(TAG, "[authenticationDidComplete]");
                //myLoadingLabel.text = @"Sharing...";

                mLoggedInUser = mSessionData.authenticatedUserForProvider(mSelectedProvider);

                // QTS: Would we ever expect this to not be the case?
                if (mLoggedInUser != null) {
                    showViewIsLoading(true);
                    loadUserNameAndProfilePicForUserForProvider(mLoggedInUser, provider);
                    showUserAsLoggedIn(true);

                    shareActivity();
                } else {
//                    UIAlertView *alert = [[[UIAlertView alloc] initWithTitle:@"Shared"
//                                                                     message:@"There was an error while sharing this activity."
//                                                                    delegate:nil
//                                                           cancelButtonTitle:@"OK"
//                                                           otherButtonTitles:nil] autorelease];
//                    [alert show];
                    showViewIsLoading(false);
                    mWeAreCurrentlyPostingSomething = false;
                    mWeHaveJustAuthenticated = false;
                }
            }

            public void publishingDidRestart() { mWeAreCurrentlyPostingSomething = false; }
            public void publishingDidCancel() { mWeAreCurrentlyPostingSomething = false; }
            public void publishingDidComplete() { mWeAreCurrentlyPostingSomething = false; }

            public void publishingActivityDidSucceed(JRActivityObject activity, String provider) {
                Log.d(TAG, "[publishingActivityDidSucceed]");
//                UIAlertView *alert = [[[UIAlertView alloc] initWithTitle:@"Shared"
//                                                                 message:[String stringWithFormat:
//                                                                          @"You have successfully shared this activity."]
//                                                                delegate:nil
//                                                       cancelButtonTitle:@"OK"
//                                                       otherButtonTitles:nil] autorelease];
//                [alert show];

//                [alreadyShared addObject:provider];

                showViewIsLoading(false);
                showActivityAsShared(true);

                mWeAreCurrentlyPostingSomething = false;
                mWeHaveJustAuthenticated = false;

                showDialog(SUCCESS_DIALOG);
            }

            public void publishingActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
                Log.d(TAG, "[publishingActivityDidFail]");
                String errorMessage = null;
                boolean reauthenticate = false;

                showViewIsLoading(false);

                switch (error.getCode())
                {// TODO: add strings to string resource file
                    case JREngageError.SocialPublishingError.FAILED:
                        errorMessage = "There was an error while sharing this activity.";
                        break;
                    case JREngageError.SocialPublishingError.DUPLICATE_TWITTER:
                        errorMessage = "There was an error while sharing this activity: Twitter does not allow duplicate status updates.";
                        break;
                    case JREngageError.SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED:
                        errorMessage = "There was an error while sharing this activity: Status was too long.";
                        break;
                    case JREngageError.SocialPublishingError.MISSING_API_KEY:
                        errorMessage = "There was an error while sharing this activity.";
                        reauthenticate = true;
                        break;
                    case JREngageError.SocialPublishingError.INVALID_OAUTH_TOKEN:
                        errorMessage = "There was an error while sharing this activity.";
                        reauthenticate = true;
                        break;
                    default:
                        errorMessage = "There was an error while sharing this activity.";
                        break;
                }

             /* OK, if this gets called right after authentication succeeds, then the navigation controller won't be done
                animating back to this view.  If this view isn't loaded yet, and we call shareButtonPressed, then the library
                will end up trying to push the webview controller onto the navigation controller while the navigation controller
                is still trying to pop the webview.  This creates craziness, hence we check for [self isViewLoaded].
                Also, this prevents an infinite loop of reauthing-failed publishing-reauthing-failed publishing.
                So, only try and reauthenticate is the publishing activity view is already loaded, which will only happen if we didn't
                JUST try and authorize, or if sharing took longer than the time it takes to pop the view controller. */
                if (reauthenticate && !mWeHaveJustAuthenticated)
                {
                    logUserOutForProvider(provider);
                    //shareButtonPressed(null);

                    return;
                }

                mWeAreCurrentlyPostingSomething = false;
                mWeHaveJustAuthenticated = false;

                showDialog(FAILURE_DIALOG);

//                UIAlertView *alert = [[[UIAlertView alloc] initWithTitle:@"Error"
//                                                                 message:errorMessage
//                                                                delegate:nil
//                                                       cancelButtonTitle:@"OK"
//                                                       otherButtonTitles:nil] autorelease];
//                [alert show];

            }
        };
    }

    private abstract class JRCMD implements JRConnectionManagerDelegate {

        public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {}
        public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload, String requestUrl, Object userdata) {}
        public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {}
        public void connectionWasStopped(Object userdata) {}

    }

    private abstract class JRSD implements JRSessionDelegate {
        public void authenticationDidRestart() {}
        public void authenticationDidCancel() {}
        public void authenticationDidComplete(String token, String provider) {}
        public void authenticationDidComplete(JRDictionary profile, String provider) {}
        public void authenticationDidFail(JREngageError error, String provider) {}
        public void authenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, byte[] payload, String provider) {}
        public void authenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {}
        public void publishingDidRestart() {}
        public void publishingDidCancel() {}
        public void publishingDidComplete() {}
        public void publishingActivityDidSucceed(JRActivityObject activity, String provider) {}
        public void publishingActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {}
    }
}

//            (new AsyncTask<String, Void, String>() {
//                protected String doInBackground(String... v) {
//                    try {
//                        InputStream is = (new DefaultHttpClient()).execute(new HttpGet(v[0])).getEntity().getContent();
//                        BufferedReader r = new BufferedReader(new InputStreamReader(is));
//                        StringBuilder total = new StringBuilder();
//                        String line;
//                        while ((line = r.readLine()) != null) { total.append(line); }
//                        Log.d(TAG, total.toString());
//                        return "short";
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//
//                protected void onPostExecute(String s) {
//                    mActionLabelView.setText(s);
//                }
//            }).execute(urlString);
