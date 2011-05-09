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
package com.janrain.android.engage.types;

import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.utils.Archiver;

import java.util.ArrayList;

/**
 * @internal
 *
 * @class JRProviderList
 * List of JRProvider objects.  This class is provided for simplicity and readability, especially
 * in the case of storing lists of providers within a JRDictionary.
 **/
public class JRProviderList extends ArrayList<JRProvider> {

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Checks to see if the specified list is null or empty.
     *
     * @param list
     *      The list instance to be checked.
     *
     * @return
     *      <code>true</code> if the list is null or empty, <code>false</code> otherwise.
     */
    public static boolean isEmpty(JRProviderList list) {
        return ((list == null) || (list.size() == 0));
    }

    /**
     * Saves (archives) the provider list to local disk using the specified name.
     *
     * @param name
     *      The name of the list of providers to be saved.
     *
     * @param list
     *      The list of providers to be saved.
     *
     * @return
     *      <code>true</code> if the list is saved, <code>false</code> otherwise.
     */
    public static boolean archive(String name, JRProviderList list) {
        return Archiver.save(name, list);
    }

    /**
     * Loads (unarchives) the specified provider list from disk.
     *
     * @param name
     *      The name of the list of providers to be loaded.
     *
     * @return
     *      The contents of the archived list if found, null otherwise.
     */
    public static JRProviderList unarchive(String name) {
        Object obj = Archiver.load(name);
        if ((obj != null) && (obj instanceof JRProviderList)) {
            return (JRProviderList)obj;
        }
        return new JRProviderList();
    }


    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Creates a new empty list of JRProvider objects.
     */
    public JRProviderList() {
        super();
    }

    /**
     * Creates a new empty list of JRProvider objects with the specified initial capacity.
     */
    public JRProviderList(int capacity) {
        super(capacity);
    }

    /**
     * Creates a new list with the contents of the specified list (base type) cloned.
     */
    public JRProviderList(ArrayList<JRProvider> list) {
        super();
        if (list != null) {
            addAll(list);
        }
    }

    /**
     * Creates a new list with the contents of the specified list cloned.
     */
    public JRProviderList(JRProviderList providerList) {
        super();
        if (!isEmpty(providerList)) {
            addAll(providerList);
        }
    }
}
/**
 * @endinternal
 **/
