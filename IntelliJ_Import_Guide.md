This page tells you how to:

*   [Download the Library](#download-the-library)
*   [Add the Library to Your Android IntelliJ Project](#add-the-library-to-your-android-intellij-project)

## Download the Library

You can either:

*   Clone the Janrain Engage for Android library from GitHub: `git clone
    git://github.com/janrain/jump.android.git`
*   Or, [download an archive](http://github.com/janrain/jump.android/tags) of the library We recommend that
    you clone the GitHub repository, and checkout the `master` branch. `master` is the stable branch with all
    the latest bug fixes. By using git to clone the repository, it's easy to keep up with bug fixes — just run
    `git pull`.

## Add the Library to Your Android IntelliJ Project

1.  Open IntelliJ, and in the project pane, right-click a module and select **Open Module Settings**.
2.  In the leftmost pane, under **Project Settings**, select **Modules**.
3.  Set the Project SDK to Android 3.2 or higher (your project is still deployable to Android 1.6+. This
    setting controls the Android SDK against which your project is compiled. It does not control the Android
    version your project targets. You may still target down to Android 1.6.
4.  Add a module by clicking the + button, just to the right of the **Project Settings** pane.
5.  Select **New > Module**.
6.  Select **Import existing module**.
7.  Click the file chooser button (**...**), browse to the `Jump.iml` file found under
    `jump.android/Jump/Jump.iml`, then click **Finish**.
8.  In the list of modules (the second pane from the left, to the right of the Project Settings pane), expand
    the **Jump** module folder (don't worry if it's underlined and red; this will go away shortly).
9.  Click the **Android** facet, immediately below the **Jump** folder.
10.  Ensure that **Library Module** is selected.
11.  In the list of modules (the second pane from the left), select your application's module.
12.  Select the **Dependencies** tab of your application's module.
13.  At the bottom of the **Dependencies** pane click the **+** button, and then choose **Module Dependency**.
14.  Select **Jump**, click **OK**. Ensure that the new dependency's Scope is set to **Compile**.
15.  Under **Project Settings**, click **Libraries**.
16.  Add a library by clicking the **+** box just to the right of the Project Settings pane, then choose
     **Java** in the **New Project Library** popup window.
17.  Browse to `.../jump.android/Jump/libs`, then select `android-support-v4.jar.`
18.  In the **Choose modules** dialog, click **Cancel**. (The library dependency already exists; the library
     only needs to be defined.) Ensure that library is still created even though you clicked cancel.
19.  Ensure that the new library's name is **android-support-v4**.
20.  Click **OK** to close the **Project Structure** settings window.  Further IDE support is available
     through the [Engage for Android support
     forum](https://support.janrain.com/forums/20122381-android-library-q-a).
