package com.janrain.android.engage.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import com.janrain.android.engage.JREngage;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 6/2/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidUtils {
    public static final String TAG = AndroidUtils.class.getSimpleName();
    private AndroidUtils() {}

    public static boolean isSmallNormalOrLargeScreen() {
        int screenConfig = getScreenSize();

        // Galaxy Tab 7" (the first one) reports SCREENLAYOUT_SIZE_NORMAL
        // Motorola Xoom reports SCREENLAYOUT_SIZE_XLARGE
        // Nexus S reports SCREENLAYOUT_SIZE_NORMAL

        return screenConfig == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_SMALL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isCupcake() {
        return Build.VERSION.RELEASE.startsWith("1.5");
    }

    public static int getAndroidSdkInt() {
        Field SDK_INT;
        try {
            SDK_INT = Build.VERSION.class.getField("SDK_INT");

            return SDK_INT.getInt(null);
        } catch (NoSuchFieldException e) {
            // Must be Cupcake
            return 3;
        } catch (IllegalAccessException e) {
            // Not expected
            throw new RuntimeException(e);
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final int SDKINT = getAndroidSdkInt();

    public static ApplicationInfo getApplicationInfo() {
        String packageName = JREngage.getActivity().getPackageName();
        try {
            return JREngage.getActivity().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static int scaleDipToPixels(int dip) {
        Context c = JREngage.getActivity();
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (((float) dip) * scale);
    }

    private static int getScreenSize() {
        int screenConfig = JREngage.getActivity().getResources().getConfiguration().screenLayout;
        screenConfig &= Configuration.SCREENLAYOUT_SIZE_MASK;
        return screenConfig;
    }

    public static boolean isXlarge() {
        return (getScreenSize() == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }
}
