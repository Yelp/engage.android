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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.janrain.android.engage.JREngage;

/**
 * Utility class used for managing preferences in the Android global shared preferences.
 */
public final class Prefs {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	public static final String KEY_JR_USER_INPUT = "jrPrefUserInput";
	public static final String KEY_JR_WELCOME_STRING = "jrPrefWelcomeString";
	public static final String KEY_JR_FORCE_REAUTH = "jrPrefForceReauth";
    
    public static final String KEY_JR_CONFIGURATION_ETAG = "jrConfigurationEtag";
	
    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Wrapper for getting shared preference string value by key.
     *
     * @param key The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static String getAsString(String key, String defValue) {
        return getAsString(JREngage.getContext(), key, defValue);
    }

    /**
     * Wrapper for getting shared preference string value by key.
     *
     * @param ctx The context used to obtain the shared preferences.
     * @param key The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static String getAsString(Context ctx, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(key, defValue);
    }

    /**
     * Wrapper for getting shared preference boolean value by key.
     *
     * @param key The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static boolean getAsBoolean(String key, boolean defValue) {
        return getAsBoolean(JREngage.getContext(), key, defValue);
    }

    /**
     * Wrapper for getting shared preference boolean value by key.
     *
     * @param ctx The context used to obtain the shared preferences.
     * @param key The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static boolean getAsBoolean(Context ctx, String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(key, defValue);
    }

    /**
     * Wrapper for getting shared preference integer value by key.
     *
     * @param key The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static int getAsInt(String key, int defValue) {
        return getAsInt(JREngage.getContext(), key, defValue);
    }

    /**
     * Wrapper for getting shared preference integer value by key.
     *
     * @param ctx The context used to obtain the shared preferences.
     * @param key The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static int getAsInt(Context ctx, String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(key, defValue);
    }

    /**
     * Wrapper for saving a string value to the shared preferences.
     *
     * @param key Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putString(String key, String value) {
        putString(JREngage.getContext(), key, value);
    }

    /**
     * Wrapper for saving a string value to the shared preferences.
     *
     * @param ctx The context used to obtain the shared preferences.
     * @param key Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putString(Context ctx, String key, String value) {
        getEditor(ctx).putString(key, value);
    }

    /**
     * Wrapper for saving a boolean value to the shared preferences.
     *
     * @param key Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putBoolean(String key, boolean value) {
        putBoolean(JREngage.getContext(), key, value);
    }

    /**
     * Wrapper for saving a boolean value to the shared preferences.
     *
     * @param ctx The context used to obtain the shared preferences.
     * @param key Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putBoolean(Context ctx, String key, boolean value) {
        getEditor(ctx).putBoolean(key, value);
    }

    /**
     * Wrapper for saving an integer value to the shared preferences.
     *
     * @param key Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putInt(String key, int value) {
        putInt(JREngage.getContext(), key, value);
    }

    /**
     * Wrapper for saving a integer value to the shared preferences.
     *
     * @param ctx The context used to obtain the shared preferences.
     * @param key Key for value to be saved.
     * @param value The value to be saved.
     */
    public static void putInt(Context ctx, String key, int value) {
        getEditor(ctx).putInt(key, value);
    }

    /**
     * Helper method used to get a shared preferences editor instance.
     */
    private static SharedPreferences.Editor getEditor(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).edit();
    }

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	private Prefs() {
		/* private constructor - utility class | no instance */
	}
	
}
