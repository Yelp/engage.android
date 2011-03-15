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
package com.janrain.android.engage.net;

import android.util.Log;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP cookie helper methods.
 */
public final class CookieHelper {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    /* Tag used for logging. */
    private static final String TAG = CookieHelper.class.getSimpleName(); 

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Remove cookie from cookie store.
     *
     * @param cookie
     *      The cookie to be removed.
     */
    public static void deleteCookie(Cookie cookie) {
        //
        // TODO:  implement
        // TODO:  No direct way to delete cookies w/ Apache cookie implementation.
    }

    /**
     * Remove the specified cookies from the cookie store.
     *
     * @param cookies
     *      The cookies to be removed.
     */
    public static void deleteCookies(List<Cookie> cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                deleteCookie(cookie);
            }
        }
    }

    /**
     * Remove all cookies for the specified URL.
     *
     * @param url
     */
    public static void deleteCookiesByUrl(String url) {
        deleteCookies(getCookiesByUrl(url));
    }

    /**
     * Gets cookie store instance.
     *
     * @return
     */
    public static CookieStore getCookieStore() {
        return new DefaultHttpClient().getCookieStore();
    }

    /**
     * Gets all cookies in the cookie store.
     *
     * @return
     */
    public static List<Cookie> getAllCookies() {
        return getCookieStore().getCookies();
    }

    /**
     * Gets all cookies that match the specified domain.
     *
     * @param domain
     * @return
     */
    public static List<Cookie> getCookiesByDomain(String domain) {
        List<Cookie> matchingCookies = new ArrayList<Cookie>();
        final List<Cookie> allCookies = getAllCookies();
        for (Cookie cookie : allCookies) {
            if (cookie.getDomain().equalsIgnoreCase(domain)) {
                matchingCookies.add(cookie);
            }
        }
        return matchingCookies;
    }

    /**
     * Gets all cookies that match the domain/host part of the specified url.
     * 
     * @param url
     * @return
     */
    public static List<Cookie> getCookiesByUrl(String url) {
        List<Cookie> matchingCookies = null;

        try {
            // Convert url string to object, so we can get parts
            final URL urlObj = new URL(url);

            // If the port is not specified in the URL, we'll use the default port
            int port = urlObj.getPort();
            port = (port == -1) ? urlObj.getDefaultPort() : port;

            // Build origin object from URL parts
            final CookieOrigin origin = new CookieOrigin(
                    urlObj.getHost(), port, urlObj.getPath(), false);
            // Cookie spec object used for comparing/matching
            final CookieSpecBase specBase = new BrowserCompatSpec();

            // Search through all cookies and look for matches
            for (Cookie cookie : getAllCookies()) {
                if (specBase.match(cookie, origin)) {
                    matchingCookies.add(cookie);
                }
            }
        } catch (MalformedURLException ignore) {
            Log.e(TAG, "[getCookiesByUrl] malformed URL: " + url);
            // send a bad url, get an empty list in return
        }
        
        if (matchingCookies == null) {
            matchingCookies = new ArrayList<Cookie>();
        }
        return matchingCookies;
    }



    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private CookieHelper() {
        // no instance
    }

}
