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
package com.janrain.android.engage;

import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

/**
 * @brief
 * The JREngageDelegate protocol is adopted by an object that wishes to receive notifications when and
 * information about a user that authenticates with your application and publishes activities to their
 * social networks.
 *
 * This protocol will notify the delegate(s) when authentication and social publishing succeed or fail,
 * it will provider the delegate(s) with the authenticated user's profile data, and, if server-side
 * authentication is desired, it can provide the delegate(s) with the data payload returned by your
 * server's token URL.
 **/
public interface JREngageDelegate {
	
/**
 * \name Configuration
 * Messages sent by JREngage during dialog launch/configuration
 **/
/*@{*/

	/**
	 * Sent if the application tries to show a JREngage dialog, and JREngage failed to show.  May
	 * occur if the JREngage library failed to configure, or if the activity object was nil, etc.
	 *
	 * @param error
	 *   The error that occurred during configuration
	 *
	 * @note
	 * This message is only sent if your application tries to show a JREngage dialog, and not 
	 * necessarily when an error occurs, if, say, the error occurred during the library's 
	 * configuration.  The raison d'etre is based on the possibility that your application may 
	 * preemptively configure JREngage, but never actually use it.  If that is the case, then you 
	 * won't get any error.
	 **/
    void jrEngageDialogDidFailToShowWithError(JREngageError error);
/*@}*/
    

/**
 * \name Authentication
 * Messages sent  by JREngage during authentication
 **/
/*@{*/

    /**
     * Sent if the authorization was canceled for any reason other than an error.  For example, 
     * the user hits the "Cancel" button, any class (e.g., the JREngage delegate) calls the 
     * cancelAuthentication message, or if configuration of the library is taking more than about 
     * 16 seconds (rare) to download.
     **/
    void jrAuthenticationDidNotComplete();
    
    /**
     * \anchor authDidSucceed
     *
     * Tells the delegate that the user has successfully authenticated with the given provider, 
     * passing to the delegate an \c NSDictionary object with the user's profile data
     *
     * @param auth_info
     *   An \c NSDictionary of fields containing all the information Janrain Engage knows about the 
     *   user logging into your application.  Includes the field "profile" which contains the user's 
     *   profile information
     *
     *   The structure of the dictionary (represented here in json) should look something like the 
     *   following:
     * \code
     "auth_info":
     {
       "profile": 
       {
         "displayName": "brian",
         "preferredUsername": "brian",
         "url": "http:\/\/brian.myopenid.com\/",
         "providerName": "Other",
         "identifier": "http:\/\/brian.myopenid.com\/"
       }
     }
     * \endcode
     *
     * @param provider
     *   The name of the provider on which the user authenticated.  For a list of possible strings, 
     *   please see the \ref basicProviders "List of Providers"
     *
     * \sa For a full description of the dictionary and its fields, 
     * please see the <a href="https://rpxnow.com/docs#api_auth_info_response">auth_info 
     * response</a> section of the Janrain Engage API documentation.
     **/
    void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider);

    /**
     * Sent when authentication failed and could not be recovered by the library
     *
     * @param error
     *   The error that occurred during authentication
     *
     * @param provider
     *   The name of the provider on which the user tried to authenticate.  For a list of possible 
     *   strings, please see the \ref basicProviders "List of Providers"
     *
     * \note
     * This message is not sent if authentication was canceled.  To be notified of a canceled 
     * authentication, see jrAuthenticationDidNotComplete.
     **/
    void jrAuthenticationDidFailWithError(JREngageError error, String provider);

    /**
     * Sent after JREngage has successfully posted the token to your application's token_url, 
     * containing the body of the response from the server
     *
     * @param tokenUrl
     *   The URL on the server where the token was posted and server-side authentication was 
     *   completed
     *
     * @param tokenUrlPayload
     *   The response from the server
     *
     * @param provider
     *   The name of the provider on which the user authenticated.  For a list of possible strings, 
     *   please see the \ref basicProviders "List of Providers"
     * 
     * \warning This function may become deprecated in the future.
     *
     * \sa \ref tokenUrlReached "- (void)jrAuthenticationDidReachTokenUrl:withResponse:andPayload:forProvider:"
     **/
    void jrAuthenticationDidReachTokenUrl(String tokenUrl, String tokenUrlPayload, String provider);

    /**
     * \anchor tokenUrlReached
     *
     * Sent after JREngage has successfully posted the token to your application's token_url, containing 
     * the headers and body of the response from the server
     *
     * @param tokenUrl
     *   The URL on the server where the token was posted and server-side authentication was completed
     *
     * @param response
     *   The response headers returned from the server
     *
     * @param tokenUrlPayload
     *   The response from the server
     *
     * @param provider
     *   The name of the provider on which the user authenticated.  For a list of possible strings, 
     *   please see the \ref basicProviders "List of Providers"
     **/
    void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response,
                                          String tokenUrlPayload, String provider);

    /**
     * Sent when the call to the token URL has failed
     *
     * @param tokenUrl
     *   The URL on the server where the token was posted and server-side authentication was 
     *   completed
     *
     * @param error
     *   The error that occurred during server-side authentication
     *
     * @param provider
     *   The name of the provider on which the user authenticated.  For a list of possible strings, 
     *   please see the \ref basicProviders "List of Providers"
     **/
    void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider);
/*@}*/

/**
 * \name SocialPublishing
 * Messages sent by JREngage during social publishing
 **/
/*@{*/

    /**
     * Sent if social publishing was canceled for any reason other than an error.  For example, 
     * the user hits the "Cancel" button, any class (e.g., the JREngage delegate) calls the 
     * cancelPublishing message, or if configuration of the library is taking more than about 16 
     * seconds (rare) to download.
     **/
    void jrSocialDidNotCompletePublishing();
    
    /**
     * Sent after the social publishing dialog is closed (e.g., the user hits the "Close" button) 
     * and publishing is complete. You can receive multiple 
     * \ref didPublish "- (void)jrSocialDidPublishJRActivity:forProvider:"
     * messages before the dialog is closed and publishing is complete.
     **/
    void jrSocialDidCompletePublishing();

    /**
     * \anchor didPublish
     * Sent after the user successfully shares an activity on the given provider
     *
     * @param activity
     *   The shared activity
     *
     * @param provider
     *   The name of the provider on which the user published the activity.  For a list of possible
     *   strings, please see the \ref socialProviders "List of Social Providers"
     **/
    void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider);
    
    /**
     * Sent when publishing an activity failed and could not be recovered by the library
     *
     * @param activity
     *   The activity the user was trying to share
     *
     * @param error
     *   The error that occurred during publishing
     *
     * @param provider
     *   The name of the provider on which the user attempted to publish the activity.  For a list 
     *   of possible strings, please see the \ref socialProviders "List of Social Providers"
     **/
    void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider);
/*@}*/
}
