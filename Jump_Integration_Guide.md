# JUMP for Android

## Features

* Engage social sign-in (includes OpenID, and many OAuth identity providers, e.g. Google, Facebook, etc.)
* Sign-in to Capture accounts
** Either via Engage social sign-in or via traditional username/password sign-in
* Capture account record updates
* Capture Account "thin" social registration -- automatic account creation for social sign-in users.

Notably missing:

* Traditional registration
* Two-step social registration
* Social sign-in account linking to an existing account
* Account-merge sign-in flow (which is like sign-in time account linking.)
* Session refreshing (sessions currently last one hour)

## Usage

Use the JUMP for Android library by following these steps:

1.  [Declare](#declare-and-import) the
    library project dependency and add the required elements to your `AndroidManifest.xml` file.
2.  [Initialize](#initialize) the library.
3.  Begin sign-in by calling Jump#showSignInDialog

Before you begin integrating you will need an array of configuration details:

1. Sign in to the Engage Dashboard
    1. Configure the providers you wish to use for authentication ("Deployment" drop down menu -> "Engage for
       Android").
    2. Retrieve your 20-character Application ID from the Engage Dashboard (In the right column of the "Home"
       page on the dashboard.)
2. Ask your deployment engineer or account manager for your Capture domain
3. Sign in to the Capture dashboard and provision a new API client for your mobile app.
    1. Use the [set_features API](http://developers.janrain.com/documentation/api-methods/capture/clients/set_features/)
       to add the "default_read_only" feature to your new API client.
    2. Use the Capture Dashboard to add a setting for your new API client called "login_client". Set it to true.
4. Ask your deployment engineer or account manager which "flow" you should use.
    1. Set the default_flow_name and default_flow_version setting for your new API client. Ask your deployment
       engineer or account manager for the appropriate values.
5. Coordinate with your deployment engineer or account manager for the correct value for your "flow locale."
   The commonly used value for US English is en-US.
6. Ask your deployment engineer or account manager for the name of the sign-in form in your flow.

Note: You _must_ create a new API client with the correct features and settings for proper operation of the
JUMP for Android SDK.

## Declare and Import

### Declare the Android Library Project Dependency

Using ant, from the directory of your project's AndroidManifest.xml:

    android update project -p . -l ../path/to/Jump

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

### Import the Library

Import the following classes:

    import com.janrain.android.Jump;
    import com.janrain.android.capture.CaptureApiError;
    import com.janrain.android.capture.Capture;

## Initialize

Initialize the library by calling `Jump#init` method. For example:

    String engageAppId = "your Engage App ID";
    String captureDomain = "your Capture domain";
    String captureClientId = "your Capture Client ID";
    String captureLocale = "your Capture flow locale";
    String captureSignInFormName = "your Capture sign-in form's name";
    Jump.TraditionalSignInType signInType = Jump.TraditionalSignInType.EMAIL; // or USERNAME
    Jump.init(this, engageAppId, captureDomain, captureClientId, captureLocale, captureSignInFormName,
            signInType);

## Start Sign-In

Once the Jump library has been initialized, your application can start the sign-in flow by calling the
sign-in dialog display method, `com.janrain.android.Jump#showSignInDialog`. You will need to define a
callback handler which implements the `com.janrain.android.Jump.SignInResultHandler` interface. For example
the SimpleDemo project does this:

    Jump.showSignInDialog(MainActivity.this, null, new Jump.SignInResultHandler() {
        public void onSuccess() {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("success");
            b.setNeutralButton("Dismiss", null);
            b.show();
        }

        public void onFailure(SignInError error) {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("error:" + error);
            b.setNeutralButton("Dismiss", null);
            b.show();
        }
    });

## Read and Modify the Account Record

You can retrieve the signed-in Capture user's account record via `Jump.getSignedInUser()`. The record is
an instance of `org.json.JSONObject`, with some additional methods defined by a subclass. You can read
and write to the record via the `JSONObject` methods.

For example, to read the aboutMe attribute in the record:

    Jump.getSignedInUser().optString("aboutMe")

Any changes made to the record must still obey the
entity type schema from Capture. So, e.g. you cannot add dynamic new attributes, but you can add additional
elements to plurals in the schema.

For example, to write the aboutMe attribute in the record:

    Jump.getSignedInUser().put("aboutMe", "new value here");

To push changes to the record to Capture call `com.janrain.android.capture.CaptureRecord#synchronize`.

## Call the Load and Store Hooks

When your Android application starts you must call `com.janrain.android.Jump#loadFromDisk`.

For example, from the SimpleDemo Application object:

    public class SimpleDemoApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            Jump.loadFromDisk(this);
        }
    }

Whenever your Android application pauses you must call `com.janrain.android.Jump.saveToDisk`.

For example, from MainActivity in SimpleDemo:

    @Override
    protected void onPause() {
        Jump.saveToDisk(this);
        super.onPause();
    }

## Sign the User Out

Call `com.janrain.android.Jump.signOutCaptureUser` to sign the user out.

## Appearance Customization

### Creating Your Own User Interface

You can create your own traditional sign-in user interface and use
`com.janrain.android.Jump.performTraditionalSignIn` to sign users in.
`com.janrain.android.Jump.showSignInDialog` also takes a provider name parameter which, if supplied, will
direct the library to begin the sign-in flow directly with that provider, skipping the stock list of
sign-in providers user interface.

### Customizing the Stock User Interface

The JUMP for Android SDK has an API for appearance customization, and allows for customization through the
Android Theme system as well.

For customizing the look and feel of the sign-in experience, please see
[Custom UI for Android](http://developers.janrain.com/documentation/mobile-libraries/advanced-topics/custom-ui-for-android/).
