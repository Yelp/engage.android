/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
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

/* SetFromMap is:
*  Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  See the NOTICE file distributed with
*  this work for additional information regarding copyright ownership.
*  The ASF licenses this file to You under the Apache License, Version 2.0
*  (the "License"); you may not use this file except in compliance with
*  the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/


package com.janrain.android.engage.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import com.janrain.android.engage.JREngage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @internal
 *
 * @class AndroidUtils
 **/
public class AndroidUtils {
    public static final String TAG = AndroidUtils.class.getSimpleName();
    private AndroidUtils() {}

    public static boolean isSmallNormalOrLargeScreen() {
        int screenConfig = getScreenSize();

        // Galaxy Tab 7" (the first one) reports SCREENLAYOUT_SIZE_NORMAL
        // Motorola Xoom reports SCREENLAYOUT_SIZE_XLARGE
        // Nexus S reports SCREENLAYOUT_SIZE_NORMAL

        return screenConfig == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_SMALL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isCupcake() {
        return Build.VERSION.RELEASE.startsWith("1.5");
    }

    private static int getAndroidSdkInt() {
        try {
            return Build.VERSION.class.getField("SDK_INT").getInt(null);
        } catch (NoSuchFieldException e) {
            // Must be Cupcake
            return 3;
        } catch (IllegalAccessException e) {
            // Not expected
            throw new RuntimeException(e);
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final int SDK_INT = getAndroidSdkInt();

    public static ApplicationInfo getApplicationInfo() {
        String packageName = JREngage.getApplicationContext().getPackageName();
        try {
            return JREngage.getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static int scaleDipToPixels(int dip) {
        Context c = JREngage.getApplicationContext();
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (((float) dip) * scale);
    }

    private static int getScreenSize() {
        int screenConfig = JREngage.getApplicationContext().getResources().getConfiguration().screenLayout;
        screenConfig &= Configuration.SCREENLAYOUT_SIZE_MASK;
        return screenConfig;
    }

    public static boolean isXlarge() {
        return (getScreenSize() == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    public static boolean isLandscape() {
        return JREngage.getApplicationContext().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }

    //public static int getScreenWidth() {
    //    DisplayMetrics metrics = new DisplayMetrics();
    //    JREngage.getApplicationContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
    //    metrics.get
    //}

    /* XXX horrible hack importing the API 9 implementation for compatibility: */
    /*  This license is for the following class only:
     *  Licensed to the Apache Software Foundation (ASF) under one or more
     *  contributor license agreements.  See the NOTICE file distributed with
     *  this work for additional information regarding copyright ownership.
     *  The ASF licenses this file to You under the Apache License, Version 2.0
     *  (the "License"); you may not use this file except in compliance with
     *  the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    public static class SetFromMap<E> extends AbstractSet<E> implements Serializable {
        private Map<E, Boolean> m;

        private transient Set<E> backingSet;

        public SetFromMap(final Map<E, Boolean> map) {
            m = map;
            backingSet = map.keySet();
        }

        @Override public boolean equals(Object object) {
            return backingSet.equals(object);
        }

        @Override public int hashCode() {
            return backingSet.hashCode();
        }

        @Override public boolean add(E object) {
            return m.put(object, Boolean.TRUE) == null;
        }

        @Override public void clear() {
            m.clear();
        }

        @Override public String toString() {
            return backingSet.toString();
        }

        @Override public boolean contains(Object object) {
            return backingSet.contains(object);
        }
        @Override public boolean containsAll(Collection<?> collection) {
            return backingSet.containsAll(collection);
        }

        @Override public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override public boolean remove(Object object) {
            return m.remove(object) != null;
        }

        @Override public boolean retainAll(Collection<?> collection) {
            return backingSet.retainAll(collection);
        }

        @Override public Object[] toArray() {
            return backingSet.toArray();
        }

        @Override
        public <T> T[] toArray(T[] contents) {
            return backingSet.toArray(contents);
        }

        @Override public Iterator<E> iterator() {
            return backingSet.iterator();
        }

        @Override public int size() {
            return m.size();
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream stream)
                throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            backingSet = m.keySet();
        }
    }
}
