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
package com.janrain.android.engage.prefs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.janrain.android.engage.JREngage;

/**
 * @internal
 * @class Prefs
 * Utility class used for managing preferences in the Android global shared preferences.
 */
public final class Prefs {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    public static final String KEY_JR_USER_INPUT = "jr_pref_user_input.";
    public static final String KEY_JR_WELCOME_STRING = "jr_pref_welcome_string.";
    public static final String KEY_JR_FORCE_REAUTH = "jr_pref_force_reauth.";
    public static final String KEY_JR_HIDE_POWERED_BY = "jr_hide_powered_by";
    public static final String KEY_JR_CONFIGURATION_ETAG = "jr_configuration_etag";
    public static final String KEY_JR_LAST_USED_BASIC_PROVIDER = "jr_last_used_basic_provider";
    public static final String KEY_JR_LAST_USED_SOCIAL_PROVIDER = "jr_last_used_social_provider";
    public static final String KEY_JR_BASE_URL = "jr_base_url";
    public static final String KEY_JR_ENGAGE_LIBRARY_VERSION = "jr_engage_library_version";


    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Wrapper for getting shared preference string value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static String getAsString(String key, String defValue) {
        return getSharedPreferences().getString(key, defValue);
    }

    /**
     * Wrapper for getting shared preference boolean value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static boolean getAsBoolean(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }

    /**
     * Wrapper for getting shared preference integer value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static int getAsInt(String key, int defValue) {
        return getSharedPreferences().getInt(key, defValue);
    }

    /**
     * Wrapper for saving a string value to the shared preferences.
     *
     * @param key   Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putString(String key, String value) {
        SharedPreferences.Editor ed = getEditor();
        ed.putString(key, value);
        ed.commit();
    }

    /**
     * Wrapper for saving a boolean value to the shared preferences.
     *
     * @param key   Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putBoolean(String key, boolean value) {
        SharedPreferences.Editor ed = getEditor();
        ed.putBoolean(key, value);
        ed.commit();
    }

    /**
     * Wrapper for saving an integer value to the shared preferences.
     *
     * @param key   Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putInt(String key, int value) {
        SharedPreferences.Editor ed = getEditor();
        ed.putInt(key, value);
        ed.commit();
    }

    /**
     * Helper method used to get the default shared preferences instance.
     */
    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(JREngage.getContext());
    }

    /**
     * Helper method used to get a shared preferences editor instance.
     */
    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private Prefs() {
        /* private constructor - utility class | no instance */
    }
}