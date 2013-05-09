This Eclipse import guide will guide you through:

* [Downloading the Library](#download-the-library)
* [Adding the Library to Your Android Eclipse Project](#add-the-library-to-your-android-eclipse-project)
* [Troubleshooting Build and Runtime Errors](#troubleshooting-build-and-runtime-errors)

## Download the Library

You can either:

*   Clone the Janrain Engage for Android library from GitHub:
    `git clone git://github.com/janrain/engage.android.git`
*   Or, [download an archive](http://github.com/janrain/engage.android/tags) of the library

**Note:** We recommend that you clone the GitHub repository, and checkout the `master` branch. The `master`
is the stable branch with all the latest bug fixes. By using git to clone the repository, it's easy to keep
up with bug fixes — just run `git pull`.

## Add the Library to Your Android Eclipse Project

1. Ensure that the [Android ADT](http://developer.android.com/sdk/eclipse-adt.html#installing) plug-in is
   installed.
2. In Eclipse, select `File` -> `Import`.
3. Select `Android` -> `Existing Android Code Into Workspace`, click `Next`.
4. Click `Browse`, browse to `.../Jump.android/Jump`, click `Open`.
   Do *not* check `Copy projects into workspace`.
5. Click `Finish`.
6. Select your project in the Package Explorer.
7. From the `Project` menu, select `Properties`.
8. Select `Android` in the left list.
9. In the `Library` section click `Add`.
10. Select `Jump`, click `OK`.
11. Click `Apply`.
12. If your project already includes a copy of the Android Support Library (`android-support-v4.jar`)
    then browse to `.../jump.android/Jump/libs/` and delete `android-support-v4.jar` and skip to step 17
13. Using the already open Properties window, select `Java Build Path` in the left list.
14. Select the `Libraries` tab.
15. Click `Add JARs`.
16. In `Jump/libs/`, select `android-support-v4.jar`, then click `OK`.
18. Set the `Project Build Target` of the Jump Eclipse project to API level 15 or above. Otherwise,
    Eclipse may reset the Jump project build target to a lower level.
19. Your project should now build and run.

**Note:** Because the Engage for Android library provides Android resources it is not sufficient to add
`jrengage.jar` to your Eclipse project build path, you must declare the Android library project dependency
as detailed.

**Note:** If you are adding Jump for Android to a Phonegap project repeat steps 1-5 for the
`.../jump.android/JREngagePhonegapPlugin` folder.

**Note:** There can only be one version of `android-support-v4.jar` in the accumulated set of `Java Build
Path` values for your workspace (although there may be more than one copy of the same version). If you have
a conflict you will need to delete one of the versions from the libs subdirectory of either the Jump SDK or
from your project.

Further Eclipse support is available through the
[Engage for Android support forum](https://support.janrain.com/forums/20122381-android-library-q-a).

## Troubleshooting Build and Runtime errors

* `Error retrieving parent for item`:

    engage.android/JREngage/res/values-v11/styles.xml:3: error: Error retrieving parent for item: No
    resource found that matches the given name '@android:style/Theme.Holo.Light.DialogWhenLarge.NoActionBar'.

   Ensure that the project build SDK is Android API level 13 or higher. Note that this setting is different
   than the targeted API level. See the IDE setup instructions, or message us on the
   [Engage for Android support forum](https://support.janrain.com/forums/20122381-android-library-q-a) for
   more help with this.

* `package android.support.v4.app does not exist`

    engage.android/JREngage/src/com/janrain/android/engage/ui/JRUiFragment.java
    package android.support.v4.app does not exist

   Ensure that the "android-support-v4" library is defined in your IDE and that it references the
   `engage.android/JREngage/libs/android-support-v4.jar` file.

* `package com.janrain.android.engage.net.async does not exist`

  Try cleaning and rebuilding your project. If you are compiling with Eclipse close and restart Eclipse.

### Next Up

[JUMP for Android Guide](Jump_Integration_Guide.md)