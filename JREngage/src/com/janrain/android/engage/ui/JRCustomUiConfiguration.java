/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2012, Janrain, Inc.
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

package com.janrain.android.engage.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

public class JRCustomUiConfiguration extends JRUiCustomization {
    public Integer mAuthenticationBackgroundColor;
    public Drawable mAuthenticationBackgroundDrawable;

    public Integer mSharingBackgroundColor;
    public Drawable mSharingBackgroundDrawable;

    public String mProviderListTitle;
    public String mSharingTitle;

    public JRCustomUiView mProviderListHeader;
    public JRCustomUiView mProviderListFooter;

    // Would be nice to do something analogous to this for modal dialogs on Android tablets
//    #define 	kJRPopoverPresentationFrameValue
//    #define 	kJRPopoverPresentationBarButtonItem
//    #define 	kJRPopoverPresentationArrowDirection

//    TODO address parity of this vs existing implementation... support both or not?
//    #define 	kJRRemoveProvidersFromAuthentication
    
    private JRCustomUiConfiguration() {}
    
    public JRCustomUiConfiguration(Context c) {}

    public void onProviderListViewCreate(ListView providerListView) {}

    /*package*/ final void onResume() {
        for (JRCustomUiView v : getNonNullCustomViews()) v.onResume();
    }

    /*package*/ final void onPause() {
        for (JRCustomUiView v : getNonNullCustomViews()) v.onPause();
    }

    /*package*/ final void onSaveInstanceState(Bundle outState) {
        for (JRCustomUiView v : getNonNullCustomViews()) v.onSaveInstanceState(outState);
    }

    /*package*/ final void onDestroy() {
        for (JRCustomUiView v : getNonNullCustomViews()) v.onDestroy();
    }

    private Set<JRCustomUiView> getNonNullCustomViews() {
        Set<JRCustomUiView> customUiViews = new HashSet<JRCustomUiView>();
//        addIfNotNullAndViewCreated(customUiViews, mAuthenticationBackgroundDrawable);
//        addIfNotNullAndViewCreated(customUiViews, mSharingBackgroundDrawable);
        addIfNotNullAndViewCreated(customUiViews, mProviderListFooter);
        addIfNotNullAndViewCreated(customUiViews, mProviderListHeader);
        return customUiViews;
    }
    
    private void addIfNotNullAndViewCreated(Set<JRCustomUiView> set, JRCustomUiView v) {
        if (v != null && v.mViewCreated) set.add(v);
    }
}
