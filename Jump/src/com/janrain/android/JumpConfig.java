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

package com.janrain.android;

import android.content.Context;

/**
 * A 'POJO' -- plain old java object, used to contain your configuration for the JUMP library, to be passed
 * to Jump.init()
 */
public final class JumpConfig {
    /**
     * A context to perform IO with
     */
    public Context context;

    /**
     * The application ID of your Engage app, from the Engage Dashboard
     */
    public String engageAppId;

    /**
     * The application ID of your Capture app. Found on the Capture app's dashboard, at the top of the
     * API clients page.
     */
    public String captureAppId;

    /**
     * The domain of your Capture app, contact your deployment engineer for this
     */
    public String captureDomain;

    /**
     * The Capture API client ID for use with this mobile app.
     */
    public String captureClientId;

    /**
     * The name of the Capture flow to operate with. The flow defines the end-user experience, including
     * defining forms and error messages, validation, state transition, etc.
     */
    public String captureFlowName;

    /**
     * Used to specify an explicit flow version. Use null normally, and Jump will fetch the latest ("HEAD")
     * version of the flow.
     */
    public String captureFlowVersion;

    /**
     * The name of the locale to use in the Capture flow
     */
    public String captureLocale;

    /**
     * Controls whether thin registration is enabled. See Jump_Registration_Guide.md.
     */
    public boolean captureEnableThinRegistration;

    /**
     * The name of the Capture sign-in form in the flow
     */
    public String captureTraditionalSignInFormName;

    /**
     * The name of the social registration form in the flow
     */
    public String captureSocialRegistrationFormName;

    /**
     * The name of the traditional (i.e. username and password) registration form in the flow
     */
    public String captureTraditionalRegistrationFormName;

    /**
     * The type of traditional sign-in. I.e. username or email-address based
     */
    public Jump.TraditionalSignInType traditionalSignInType;

    /**
     * The Backplane channel URL to post identity/login messages to when a user signs in.
     * Can be used in conjunction with the Livefyre native app support.
     */
    public String backplaneChannelUrl;
}
