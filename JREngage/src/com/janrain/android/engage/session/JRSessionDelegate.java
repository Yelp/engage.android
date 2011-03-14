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
package com.janrain.android.engage.session;

import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

/**
 * TODO:DOC
 */
public interface JRSessionDelegate {

    void authenticationDidRestart();

    void authenticationDidCancel();

    // TODO: Wasn't this deprecated!?
    void authenticationDidComplete(String token, String provider);
    
    void authenticationDidComplete(JRDictionary profile, String provider);
    
    void authenticationDidFail(JREngageError error, String provider);
    
    void authenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, byte[] payload, String provider);
    
    void authenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider);
    
    void publishingDidRestart();
    
    void publishingDidCancel();
    
    void publishingDidComplete();
    
    void publishingActivityDidSucceed(JRActivityObject activity, String provider);

    void publishingActivityDidFail(JRDictionary activity, JREngageError error, String provider);
}

