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
package com.janrain.android.engage.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Array.newInstance;

/**
 * @internal
 *
 * @class CollectionUtils
 **/
public final class CollectionUtils {
    public static boolean isEmpty(List<?> list) {
        return ((list == null) || (list.size() < 1));
    }

    public static interface Function<L, R> {
        L operate(R val);
    }

    /**
     * Maps a function f onto the values of a map map. Returns a new map with with the result.
     * @param map
     *      A map onto which to apply f to the values of
     * @param f
     *      A function mapped onto the values of map
     * @param <K>
     *      The key type for map
     * @param <L>
     *      The left hand side type of f
     * @param <R>
     *      The right hand side type of f
     * @return
     *      A new map&gt;K, L> with with the result.
     */
    public static <K, L, R> HashMap<K, L> map(Map<K, R> map, Function<L, R> f) {
        HashMap<K, L> retMap = new HashMap<K, L>();

        for (Map.Entry<K, R> e : map.entrySet()) retMap.put(e.getKey(), f.operate(e.getValue()));

        return retMap;
    }

    /**
     * Maps a function f onto the values of a list list. Returns a new list with with the result.
     * @param list
     *      A list onto which to apply f to the values of
     * @param f
     *      A function mapped onto the values of list
     * @param <L>
     *      The left hand side type of f
     * @param <R>
     *      The right hand side type of f
     * @return
     *      A new list&gt;L> with the result
     */
    public static <L, R> ArrayList<L> map(List<R> list, Function<L, R> f) {
        ArrayList<L> retList = new ArrayList<L>();

        for (R e : list) retList.add(f.operate(e));

        return retList;
    }

    public static <L, R> Collection<L> map(Collection<R> collection, Function<L, R> f) {
        Collection<L> retCollection;
        try {
            retCollection = collection.getClass().newInstance();
        } catch (InstantiationException ignore) {
            retCollection = new ArrayList<L>();
        } catch (IllegalAccessException ignore) {
            retCollection = new ArrayList<L>();
        }

        for (R e : collection) retCollection.add(f.operate(e));

        return retCollection;
    }

    /**
     * Maps a function f onto the values of a array array. Returns a new array of the same type
     * with with the result.
     * @param array
     *      A array onto which to apply f to the values of
     * @param f
     *      A function mapped onto the values of array
     * @param <T>
     *      The right and left hand side type of f
     * @return
     *      A new T[] with the result
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] map(T[] array, Function<T, T> f) {
        T[] retArray = (T[]) newInstance(array.getClass().getComponentType(), array.length);

        int i = 0;
        for (T e : array) retArray[i++] = (f.operate(e));

        return retArray;
    }
}