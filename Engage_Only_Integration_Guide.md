Use the Engage for Android library following these steps:

1.  [Declare](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/engage-for-android/#declare-and-import)
    the library project dependency and add the required elements to your `AndroidManifest.xml` file.
2.  [Initialize](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/engage-for-android/#initialize)
    the library.
3.  Begin authentication or sharing by calling one of the two `show...Dialog methods`. See:

    *   [Social Sign-In](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/engage-for-android/#social-sign-in)
    *   [Social Sharing](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/engage-for-android/#social-sharing)

4.  You will also want to implement [Server-side Authentication](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/engage-for-android/#server-side-authentication).

To begin, sign in to Engage and configure the providers you wish to use for authentication or social sharing.
You will also need your 20-character Application ID from the Engage Dashboard.

### Declare the JUMP Activities

Ensure the presence of the `android.permission.INTERNET` permission in your `<uses-permission>` element, and
copy from `.../Jump/AndroidManifest.xml`, adding the following two `<activity>` XML elements, and to your
project's `AndroidManifest.xml` file:

    <manifest xmlns:android="http://schemas.android.com/apk/res/android" ... >

      <uses-permission android:name="android.permission.INTERNET" />
      <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="13" />

      ...

      <application ... >

      ...


        <!-- The following activities are for the Janrain Engage for Android library -->
        <!-- This activity must have a dialog theme such as Theme.Holo.Dialog, or
            Theme.Dialog, etc.

            Using android:theme="@style/Theme.Janrain.Dialog" will result in Theme.Dialog on API 4-10 and
            Theme.Holo.DialogWhenLarge.NoActionBar on API 11+
        -->
        <activity
            android:name="com.janrain.android.engage.ui.JRFragmentHostActivity"
            android:configChanges="orientation|screenSize"
            android:theme="@style/Theme.Janrain.Dialog.Light"
            android:windowSoftInputMode="adjustResize|stateHidden"
            />

        <!-- This activity must have a normal (non-dialog) theme such as Theme, Theme.Light, Theme.Holo, etc.

            Using android:theme="@style/Theme.Janrain" or "@style/Theme.Janrain.Light" will result in
            Theme (or Theme.Light) on API 4-10 and
            Theme.Holo (or Theme.Holo.Light) on API 11+
        -->
        <activity
            android:name="com.janrain.android.engage.ui.JRFragmentHostActivity$Fullscreen"
            android:configChanges="orientation|screenSize"
            android:theme="@style/Theme.Janrain.Light"
            android:windowSoftInputMode="adjustResize|stateHidden"
            />

      ...

      </application>

    </manifest>

Note: If you wish to target a version of Android lower than 13 (which is 3.2) you may. To do so, change the
`android:targetSdkVersion`, to your desired deployment target. _You must still build against API 13+
even when targeting a lower API level._ The build SDK used when compiling your project is defined by your
project's local.properties. `android list target` to get a list of targets available in your installation of
the Android SDK. `android update project -p . -t target_name_or_target_installation_id` to update the build
SDK for your project. (Note that this does *not* affect your project's `minSdkVersion` or `targetSdkVersion`.

Import the following classes:

    import com.janrain.android.engage.JREngage;
    import com.janrain.android.engage.JREngageDelegate;
    import com.janrain.android.engage.JREngageError;
    import com.janrain.android.engage.net.async.HttpResponseHeaders;
    import com.janrain.android.engage.types.JRActivityObject;
    import com.janrain.android.engage.types.JRDictionary;

## Initialize

Interaction begins by calling the `JREngage.initInstance` method, which returns a `JREngage`
object:

    private static final String ENGAGE_APP_ID = "";
    private static final String ENGAGE_TOKEN_URL = "";
    private JREngage mEngage;
    private JREngageDelegate mEngageDelegate = ...;

    ...

    mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);

The [initInstance](http://janrain.github.com/engage.android/docs/html/classcom_1_1janrain_1_1android_1_1engage_1_1_j_r_engage.html#a469d808d2464c065bc16dedec7a2cc23)
method takes four arguments, `activity`, `appId`, `tokenUrl`, and `delegate`:

`activity` — (required) Your application’s Android `Activity` from which the Engage for Android
activities will be started.
`appId` — (required) The Application ID of your Janrain Engage application (found on the Engage
Dashboard).
`tokenUrl` — (optional) The token URL to which authentication from your application posts.
`delegate` — (optional) An implementation of the `JREngageDelegate` interface through which you
can receive responses and event information from the library.

## Social Sign-In

Once the `JREngage` object has been initialized, your application can start user authentication by
calling the [showAuthenticationDialog](http://janrain.github.com/engage.android/docs/html/classcom_1_1janrain_1_1android_1_1engage_1_1_j_r_engage.html#a0de1aa16e951a1b62e2ef459b1596e83)
 method.
`mEngage.showAuthenticationDialog();`

To receive the user's basic profile data, implement the
[jrAuthenticationDidSucceedForUser](http://janrain.github.com/jump.ios/gh_docs/capture/html/protocol_j_r_capture_signin_delegate-p.html#aa5dc2ae621394b1a97b55eb3fca6b2ef)
method of
[JREngageDelegate](http://janrain.github.com/engage.android/docs/html/interfacecom_1_1janrain_1_1android_1_1engage_1_1_j_r_engage_delegate.html):

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
      JRDictionary profile = auth_info.getAsDictionary("profile");
      String displayName = profile.getAsString("displayName");
      String message = "Authentication successful for user: " + displayName));

      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

### More

*   For configuring your token URL to handle server-side authentication, please see the
    [Server-side Authentication](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/engage-for-android/#server-side-authentication)
    below.
*   For customizing the look and feel of the sign-in experience, please see the
    [Custom UI for Android](/documentation/mobile-libraries/advanced-topics/custom-ui-for-android/).

## Social Sharing

If you want to share an activity, first create an instance of the
[`JRActivityObject`](http://janrain.github.com/engage.android/docs/html/classcom_1_1janrain_1_1android_1_1engage_1_1types_1_1_j_r_activity_object.html):

    String activityText = "added JREngage to her Android application!";
    String activityLink = "http://janrain.com";

    JRActivityObject jrActivity = new JRActivityObject(activityText, activityLink);

Enrich the activity to be shared by populating some of the optional fields. Here an exciting Facebook action
link is added:

    activityObject.addActionLink(new JRActionLink("Download the Quick Share demo!",
          "https://market.android.com/details?id=com.janrain.android.quickshare");

Then pass the activity to the
[`showSocialPublishingDialogWithActivity`](http://janrain.github.com/engage.android/docs/html/classcom_1_1janrain_1_1android_1_1engage_1_1_j_r_engage.html#aef1ecf0e43afeed0eb0a779c67eff285)
method:

    mEngage.showSocialPublishingDialogWithActivity(jrActivity);

## Server-side Authentication

If you would like to access any of the extra features available in the Janrain Engage API, or if you would
like to complete server-side authentication, do so by implementing a token URL as follows:

1.  Create a server side HTTP or HTTPS endpoint (preferably HTTPS). This will be your `auth_info`
    token URL, and mobile devices running your mobile app will POST an Engage
    `[auth_info](/documentation/api/auth_info/ "auth_info")` token to this endpoint, in exchange for an
    access token for your web service.
2.  From the new endpoint, extract the token. It's POSTed in a parameter named `token`.
3.  Call `auth_info`. Supply the token just received, and your application's 40-character Engage API
    key.
4.  Process the profile data returned from `[auth_info](/documentation/api/auth_info/ "auth_info")`,
    and log your user into your web application. (The unique and secure key you should use to identify the
    user is the `identifier` field of the `profile` node.) As necessary create and return
    access tokens or session cookies in your endpoint's response. Your mobile app will receive that response.

For example, in Ruby on Rails, you might rely on the `ActionController` session and the cookie that
it sets like so:

    # The following helper class is from the Engage sample code found at
    # https://github.com/janrain/Janrain-Sample-Code
    ENGAGE = Rpx::RpxHelper.new("your_api_key_here",
                                "http://rpxnow.com",
                                "your_engage_app_realm_here") # e.g. mytestapp

    # This is the Engage auth_info token URL -- the endpoint which spawns
    # new mobile user sessions.
    def mobileEngageSignIn
       auth_info = ENGAGE.auth_info(params[:token])

       identifier = auth_info['identifier']

       user = User.find_or_create_by_engage_identifier(identifier)
       # do other stuff, like populate the User record with the auth_info

       session[:user_id] = user.id
    end

If you're using the Rails `ActionController` session, you should set the cookie expiration to an
appropriate value for a mobile device:

    # This initializer block is found in app/config/environment.rb
    Rails::Initializer.run do |config|
      config.action_controller.session[:session_expires] = 10.years.from_now
    end

Then, make sure that you save the cookie in your mobile app; for example:

org.apache.http.cookie.Cookie[] mSessionCookies;

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                 HttpResponseHeaders responseHeaders,
                                                 String tokenUrlPayload,
                                                 String provider) {
        mSessionCookies = responseHeaders.getCookies();
    }

From your new `auth_info` token URL you can also access access other Engage features. For example, you
could call `[get_contacts](/documentation/api/get_contacts/ "get_contacts")`* and use the contact list
returned to find other users of your mobile app that this user may know.

_* Some features are limited to Pro, Plus, or Enterprise customers only._

To configure the library with your token URL, pass it to
[`initInstance`](http://janrain.github.com/engage.android/docs/html/classcom_1_1janrain_1_1android_1_1engage_1_1_j_r_engage.html#a469d808d2464c065bc16dedec7a2cc23)
when initializing the library:

    private static final String ENGAGE_APP_ID = "";
    private static final String ENGAGE_TOKEN_URL = "";
    private JREngage mEngage;
    private JREngageDelegate mEngageDelegate;

    ...

    mEngage = JREngage.initInstance(this, engageAppId, engageTokenUrl, mEngageDelegate);

Alternatively, you can change the token URL at any time using the
[`setTokenUrl`](http://janrain.github.com/engage.android/docs/html/classcom_1_1janrain_1_1android_1_1engage_1_1_j_r_engage.html#a9cae37926c51b92a0d934b65cd14829c)
method:

    JREngage mEngage;

    ...

    mEngage.setTokenUrl(newTokenUrl);

You may configure the library with a null or empty token URL, and this authentication step will be skipped.

Whether or not the library posts the token to the token URL, your Android application must not contain your
Engage API key.

### Next

You may want to look at our [PhoneGap](/documentation/mobile-libraries/phonegapcordova/ "PhoneGap/Cordova")
integration, or the [Advanced Topics](/documentation/mobile-libraries/advanced-topics/ "Advanced Topics")
section.