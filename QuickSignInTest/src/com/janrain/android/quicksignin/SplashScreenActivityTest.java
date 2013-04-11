package com.janrain.android.quicksignin;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.janrain.android.quicksignin.SplashScreenTest \
 * com.janrain.android.quicksignin.tests/android.test.InstrumentationTestRunner
 */
public class SplashScreenTest extends ActivityInstrumentationTestCase2<SplashScreen> {

    public SplashScreenTest() {
        super("com.janrain.android.quicksignin", SplashScreen.class);
    }

}
