package com.janrain.android.engage.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.janrain.android.engage.JREngage;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 6/2/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Android {
    public static final String TAG = Android.class.getSimpleName();
    private Android() {}

    public static boolean isCupcake() {
        return Build.VERSION.RELEASE.startsWith("1.5");
    }


    public static int getAndroidSdkInt() {
        Field SDK_INT = null;
        try {
            SDK_INT = Build.VERSION.class.getField("SDK_INT");

            return (Integer) SDK_INT.getInt(null);
        } catch (NoSuchFieldException e) {
            // Must be Cupcake
            return 3;
        } catch (IllegalAccessException e) {
            // Not expected
            throw new RuntimeException(e);
        }
    }

    public static ApplicationInfo getApplicationInfo() {
        String packageName = JREngage.getContext().getPackageName();
        try {
            return JREngage.getContext().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
