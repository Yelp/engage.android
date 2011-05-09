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
package com.janrain.android.engage.utils;

import android.text.TextUtils;
import android.util.Log;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * @internal
 *
 * @class StringUtils
 * String utility methods.
 */
public final class StringUtils {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = StringUtils.class.getSimpleName();

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * <p>Removes <code>separator</code> from the end of
     * <code>str</code> if it's there, otherwise leave it alone.</p>
     *
     * <pre>
     * StringUtils.chomp(null, *)         = null
     * StringUtils.chomp("", *)           = ""
     * StringUtils.chomp("foobar", "bar") = "foo"
     * StringUtils.chomp("foobar", "baz") = "foobar"
     * StringUtils.chomp("foo", "foo")    = ""
     * StringUtils.chomp("foo ", "foo")   = "foo "
     * StringUtils.chomp(" foo", "foo")   = " "
     * StringUtils.chomp("foo", "foooo")  = "foo"
     * StringUtils.chomp("foo", "")       = "foo"
     * StringUtils.chomp("foo", null)     = "foo"
     * </pre>
     *
     * @param str  the String to chomp from, may be null
     * @param separator  separator String, may be null
     * @return String without trailing separator, <code>null</code> if null String input
     */
    public static String chomp(String str, String separator) {
        if (TextUtils.isEmpty(str) || separator == null) {
            return str;
        }
        if (str.endsWith(separator)) {
            return str.substring(0, str.length() - separator.length());
        }
        return str;
    }

    /**
     * Converts the specified string to it's JSON representation.
     *
     * @param str
     *      The string to be converted to JSON.
     * @return
     *      The JSONified string.
     */
    public static String toJSON(String str) {
        String retval = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            retval = mapper.writeValueAsString(str);
        } catch (IOException e) {
            Log.w(TAG, "[toJSON] problem serializing JSON string: ", e);
        }

        return retval;
    }

    /**
     * Converts the byte array to String using UTF-8 encoding.
     *
     * @param bytes
     *      The byte array to be converted.
     *
     * @param defaultStr
     *      The value to return if conversion fails.
     *
     * @return
     *      Decoded byte array if successful, defaultStr otherwise.
     */
    public static String decodeUtf8(byte[] bytes, String defaultStr) {
        final String UTF8 = "UTF-8";
        try {
            return new String(bytes, UTF8);
        } catch (UnsupportedEncodingException e) {
            return defaultStr;
        }
    }

    /**
     * Converts string to boolean, checking for all supported meanings of true.
     *
     * @param stringToConvert
     *      The string to be converted to a boolean value.
     *
     * @param defaultValue
     *      The default value if the string cannot be converted.
     *
     * @return
     *      The converted value if convertible, default value otherwise.
     */
    public static boolean stringToBoolean(String stringToConvert, boolean defaultValue) {
        boolean retval = defaultValue;
        if (isTrue(stringToConvert)) {
            retval = true;
        } else if (isFalse(stringToConvert)) {
            retval = false;
        }
        return retval;
    }

    /**
     * Checks to see if the specified string is one of the supported values that
     * equates to true.
     *
     * @param stringToCheck
     *      The string to examine.
     *
     * @return
     *      <code>true</code> if the value is true, <code>false</code> otherwise.
     */
    public static boolean isTrue(String stringToCheck) {
        final String[] STRINGS = { "true", "yes" };
        boolean retval = false;
        if (!TextUtils.isEmpty(stringToCheck)) {
            for (String s : STRINGS) {
                if (s.equalsIgnoreCase(stringToCheck)) {
                    retval = true;
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * Checks to see if the specified string is one of the supported values that
     * equates to false.
     *
     * @param stringToCheck
     *      The string to examine.
     *
     * @return
     *      <code>true</code> if the value is false, <code>false</code> otherwise.
     */
    public static boolean isFalse(String stringToCheck) {
        final String[] STRINGS = { "false", "no" };
        boolean retval = false;
        if (!TextUtils.isEmpty(stringToCheck)) {
            for (String s : STRINGS) {
                if (s.equalsIgnoreCase(stringToCheck)) {
                    retval = true;
                    break;
                }
            }
        }
        return retval;
    }

    public static String generateStackTrace(String msg) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Exception(msg).printStackTrace(new PrintStream(baos));
        return baos.toString();
    }


    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private StringUtils() {
        // no instance allowed, static utility class
    }
}
/**
 * @endinternal
 **/

