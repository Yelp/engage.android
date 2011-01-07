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
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.janrain.android.engage.ui;

import com.janrain.android.engage.R;

/**
 * Common resource operations.
 */
public final class ResourceHelper {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Gets the icon resource id for the specified provider.
     */
    public static int providerNameToIconResourceId(String providerName) {
        if ("aol".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_aol_30x30;
        } else if ("blogger".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_blogger_30x30;
        } else if ("facebook".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_facebook_30x30;
        } else if ("flickr".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_flickr_30x30;
        } else if ("google".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_google_30x30;
        } else if ("hyves".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_hyves_30x30;
        } else if ("linkedin".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_linkedin_30x30;
        } else if ("live".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_live_id_30x30;
        } else if ("livejournal".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_livejournal_30x30;
        } else if ("myopenid".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_myopenid_30x30;
        } else if ("myspace".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_myspace_30x30;
        } else if ("netlog".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_netlog_30x30;
        } else if ("openid".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_openid_30x30;
        } else if ("paypal".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_paypal_30x30;
        } else if ("twitter".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_twitter_30x30;
        } else if ("verisign".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_verisign_30x30;
        } else if ("wordpress".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_wordpress_30x30;
        } else if ("yahoo".equalsIgnoreCase(providerName)) {
            return R.drawable.icon_yahoo_30x30;
        }
        return R.drawable.icon_unknown;
    }

    /**
     * Gets the logo image resource id for the specified provider.
     */
    public static int providerNameToLogoResourceId(String providerName) {
        if ("aol".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_aol_280x65;
        } else if ("blogger".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_blogger_280x65;
        } else if ("facebook".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_facebook_280x65;
        } else if ("flickr".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_flickr_280x65;
        } else if ("google".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_google_280x65;
        } else if ("hyves".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_hyves_280x65;
        } else if ("linkedin".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_linkedin_280x65;
        } else if ("live".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_live_id_280x65;
        } else if ("livejournal".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_livejournal_280x65;
        } else if ("myopenid".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_myopenid_280x65;
        } else if ("myspace".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_myspace_280x65;
        } else if ("netlog".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_netlog_280x65;
        } else if ("openid".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_openid_280x65;
        } else if ("paypal".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_paypal_280x65;
        } else if ("twitter".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_twitter_280x65;
        } else if ("verisign".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_verisign_280x65;
        } else if ("yahoo".equalsIgnoreCase(providerName)) {
            return R.drawable.logo_yahoo_280x65;
        }
        return -1;
    }

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private ResourceHelper() {
        /* private constructor - no instance */
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

}
