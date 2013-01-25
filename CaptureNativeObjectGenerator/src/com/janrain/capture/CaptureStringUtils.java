/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2013, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.janrain.capture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: nathan Date: 1/24/13 Time: 1:47 PM To change this template use File |
 * Settings | File Templates.
 */
public class CaptureStringUtils {
    private static final List<String> DEPLURALIZATION_LIST = Arrays.asList(
            "accounts", "account", "profiles", "profile", "addresses", "address", "friends", "friend",
            "photos", "photo", "emails", "email", "games", "game", "opponents", "opponent",
            "organizations", "organization", "phoneNumbers", "phoneNumber", "securityQuestions",
            "securityQuestion", "tags", "tag", "urls", "url", "relationships", "relationship",
            "ims", "im", "mice", "mouse", "mices", "mouse");

    public static String javaEntityTypeNameForCaptureAttrName(String name) {
        return "JRCapture" + upcaseFirst(snakeToCamel(name));
    }

    public static String upcaseFirst(String camelName) {
        return camelName.substring(0, 1).toUpperCase() + camelName.substring(1);
    }

    public static String snakeToCamel(String snakeName) {
        String[] namePieces = snakeName.split("_");
        for (int i = 1; i < namePieces.length; i++) {
            namePieces[i] = namePieces[i].substring(0, 1).toUpperCase() + namePieces[i].substring(1);
        }
        String retval = "";
        for (String s : namePieces) retval += s;
        return retval;
    }

    public static String depluralize(String plural) {
        int i;
        if ((i = DEPLURALIZATION_LIST.indexOf(plural)) >= 0) return DEPLURALIZATION_LIST.get(i + 1);
        log("Couldn't depluralize: " + plural);
        return plural;
    }

    public static void log(Object o) {
        System.out.println(o);
        System.out.flush();
    }

    public static String join(String[] a, String separator) {
        return join(Arrays.asList(a), separator);
    }

    private static String join(List l, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object e : l) sb.append(e).append(separator);
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static String readFully(InputStream is) {
        try {
            return new String(readFromStream(is, false));
        } catch (IOException ignore) {
            throw new RuntimeException(ignore);
        }
    }

    public static byte[] readFromStream(InputStream in, boolean shouldThrowOnError) throws IOException {
        if (in != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) baos.write(buffer, 0, len);
                return baos.toByteArray();
            } catch (IOException e) {
                log("[readFromStream] problem reading from input stream: " + e.getLocalizedMessage());
                if (shouldThrowOnError) throw e;
            } finally {
                baos.close();
            }
        } else {
            log("[readFromStream] unexpected null InputStream");
        }

        return null;
    }
}
