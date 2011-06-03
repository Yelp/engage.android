package com.janrain.android.engage.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.janrain.android.engage.JREngage;

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

    public static boolean asdf() {
        // XXX 1.5
        //return android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.DONUT;
        return true;
    }

    public static ApplicationInfo getApplicationInfo() {
        // XXX 1.5
        //return JREngage.getContext().getApplicationInfo();
        String packageName = JREngage.getContext().getPackageName();
        try {
            return JREngage.getContext().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
