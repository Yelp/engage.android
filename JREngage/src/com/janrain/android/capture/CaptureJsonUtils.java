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

package com.janrain.android.capture;

import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.utils.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.janrain.android.engage.utils.CollectionUtils.makeSortedSetFromIterator;

public class CaptureJsonUtils {
    public static Object connectionManagerGetJsonContent(HttpResponseHeaders headers,
                                                         byte[] payload) {
        String json = null;
        try {
            json = new String(payload, "UTF-8");
            if (headers.getContentType().toLowerCase().startsWith("application/json")) {
                return new JSONTokener(json).nextValue();
            }
            JREngage.logd("unrecognized content type: " + headers.getContentType());
            JREngage.logd(json);
            return null;
        } catch (IOException e) {
            JREngage.logd(e.toString());
            return null;
        } catch (JSONException ignore) {
            return json;
        }
    }

    //public static Object urlConnectionGetJsonContent(URLConnection uConn) {
    //    String json = null;
    //    try {
    //        if (uConn.getContentType().toLowerCase().equals("application/json")) {
    //            json = CaptureStringUtils.readFully(uConn.getInputStream());
    //            return new JSONTokener(json).nextValue();
    //        }
    //        JREngage.logd("content type: " + uConn.getContentType());
    //        JREngage.logd(CaptureStringUtils.readFully(uConn.getInputStream()));
    //        return null;
    //    } catch (IOException e) {
    //        JREngage.logd(e.toString());
    //        return null;
    //    } catch (JSONException ignore) {
    //        return json;
    //    }
    //}

    private static int jsonArrayCompareTo(JSONArray this_, Object other) {
        if (other instanceof JSONArray) {
            return jsonArrayCompareTo(this_, (JSONArray) other);
        } else return this_.getClass().getName().compareTo(other.getClass().getName());
    }

    private static int jsonArrayCompareTo(JSONArray this_, JSONArray otherArray) {
        for (int index = 0; index < this_.length(); index++) {
            Object thisValue;
            try {
                thisValue = this_.get(index);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }

            Object otherVal;
            try {
                otherVal = otherArray.get(index);
            } catch (JSONException e) {
                // out of bounds
                return 1;
            }

            int comparison = compareJsonVals(thisValue, otherVal);

            if (comparison != 0) return comparison;
        }

        if (otherArray.length() > this_.length()) return -1;

        return 0;
    }

    /**
     * Compares two values of types found in the set of possible types for JSON values
     * Uses a standard ordering of such values where:
     *  If the types are mismatched the values are compared by type class name string
     *  If the types are primitives of matching type their values are compared with compareTo
     *  If the types are JSONObject their fields are iterated over by the union of the key set and compared
     *    recursively. The first non zero comparison is the return value, or zero is returned if all fields
     *    are equal
     *  If the types are JSONArray their elements are compared in order and the first non zero comparison
     *    is returned, or zero if all fields are equal
     *  If the values are both JSONObject.NULL this method returns 0
     *
     *  Null arguments are not supported.
     *
     *  Note that number types aren't compared if their types are unequal, e.g. 1.0 the Double does not
     *  equal 1 the Integer.
     *
     * @param thisVal one JSON value
     * @param otherVal another JSON value
     * @return 0 if they are equal
     */
    public static int compareJsonVals(Object thisVal, Object otherVal) {
        Integer comparison = null;
        if (thisVal instanceof JSONArray) {
            comparison = jsonArrayCompareTo((JSONArray) thisVal, otherVal);
        } else if (thisVal instanceof JSONObject) {
            comparison = jsonObjectCompareTo((JSONObject) thisVal, otherVal);
        } else if (thisVal instanceof Double) {
            if (otherVal instanceof Double) comparison = ((Double) thisVal).compareTo((Double) otherVal);
        } else if (thisVal instanceof Long) {
            if (otherVal instanceof Long) comparison = ((Long) thisVal).compareTo((Long) otherVal);
        } else if (thisVal instanceof Integer) {
            if (otherVal instanceof Integer) comparison = ((Integer) thisVal).compareTo((Integer) otherVal);
        } else if (thisVal instanceof Byte) {
            if (otherVal instanceof Byte) comparison = ((Byte) thisVal).compareTo((Byte) otherVal);
        } else if (thisVal instanceof Boolean) {
            if (otherVal instanceof Boolean) comparison = ((Boolean) thisVal).compareTo((Boolean) otherVal);
        } else if (thisVal instanceof String) {
            if (otherVal instanceof String) comparison = ((String) thisVal).compareTo((String) otherVal);
        } else if (thisVal.equals(JSONObject.NULL)) {
            if (otherVal.equals(JSONObject.NULL)) comparison = 0;
        } else {
            throw new RuntimeException("Unexpected type in compareTo for: " + thisVal);
        }

        if (comparison == null) {
            comparison = thisVal.getClass().getName().compareTo(otherVal.getClass().getName());
        }
        return comparison;
    }

    private static int jsonObjectCompareTo(JSONObject this_, Object other) {
        if (other instanceof JSONObject) {
            return jsonObjectCompareTo(this_, (JSONObject) other);
        } else return this_.getClass().getName().compareTo(other.getClass().getName());
    }

    private static int jsonObjectCompareTo(JSONObject this_, JSONObject other) {
        SortedSet<String> this_Keys = makeSortedSetFromIterator((Iterator<String>) this_.keys());
        SortedSet<String> otherKeys = makeSortedSetFromIterator((Iterator<String>) other.keys());

        SortedSet<String> temp = new TreeSet<String>(this_Keys);
        temp.addAll(otherKeys);
        SortedSet<String> union = temp;

        for (String k : union) {
            Object this_Val, otherVal;
            try {
                this_Val = this_.get(k);
            } catch (JSONException e) {
                return -1;
            }

            try {
                otherVal = other.get(k);
            } catch (JSONException e) {
                return 1;
            }

            int comparison = compareJsonVals(this_Val, otherVal);
            if (comparison != 0) return comparison;
        }

        return 0;
    }

    /**
     * Copies source into dest.
     * @param source JSONObject to deep copy, assumed to be acyclic
     * @param dest JSONObject to deep copy source into, assumed to be empty
     */
    public static void deepCopy(JSONObject source, JSONObject dest) {
        // poor type safety here is fallout of JSONObject not having a copy constructor or supporting
        // clone

        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // copy each key by hand, copying mutable types, not copying immutable types
            try {
                Object val = source.get(key);
                Object val_;
                if (val instanceof JSONArray) {
                    val_ = new JSONArray(val.toString());
                } else if (val instanceof JSONObject) {
                    val_ = new JSONObject(val.toString());
                } else {
                    val_ = val;
                }
                dest.put(key, val_);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    //public static void deepArraySort(JSONArray original) {
    //    for (int i=0; i < original.length(); i++) {
    //        try {
    //            Object val = original.get(i);
    //            if (val instanceof  JSONObject) {
    //                deepArraySort(((JSONObject) val));
    //            } else if (val instanceof JSONArray) {
    //                deepArraySort(((JSONArray) val));
    //            }
    //        } catch (JSONException e) {
    //            throw new RuntimeException("Unexpected", e);
    //        }
    //    }
    //
    //    try {
    //        mergeSort(original, 0, original.length());
    //    } catch (JSONException e) {
    //        throw new RuntimeException("Unexpected", e);
    //    }
    //}

    //private static void mergeSort(JSONArray original, int start, int len) throws JSONException {
    //    if (len <= 1) return;
    //
    //    int halfLen = len / 2;
    //    int left = start;
    //    int right = start + halfLen;
    //
    //    mergeSort(original, left, halfLen);
    //    mergeSort(original, right, len - halfLen);
    //
    //    Object[] temp = new Object[len];
    //    int tempIndex = 0;
    //
    //    while (left < start + halfLen || right < start + len) {
    //        Object leftVal = original.opt(left);
    //        Object rightVal = original.opt(right);
    //        if (left < start + halfLen && right < start + len) {
    //            if (compareJsonVals(leftVal, rightVal) < 0) {
    //                temp[tempIndex++] = leftVal;
    //                left++;
    //            } else  {
    //                temp[tempIndex++] = rightVal;
    //                right++;
    //            }
    //        } else if (left < start + halfLen) {
    //            temp[tempIndex++] = leftVal;
    //            left++;
    //        } else {
    //            temp[tempIndex++] = rightVal;
    //            right++;
    //        }
    //    }
    //
    //    for (int i=0; i < temp.length; i++) original.put(start + i, temp[i]);
    //}

    //public static void deepArraySort(JSONObject original) {
    //    Iterator<String> keys = original.keys();
    //    while (keys.hasNext()) {
    //        String key = keys.next();
    //        try {
    //            Object val = original.get(key);
    //            if (val instanceof JSONArray) {
    //                deepArraySort(((JSONArray) val));
    //            } else if (val instanceof JSONObject) {
    //                deepArraySort(((JSONObject) val));
    //            }
    //        } catch (JSONException e) {
    //            throw new RuntimeException("Unexpected", e);
    //        }
    //    }
    //}

    //public static void deeplyRandomizeArrayElementOrder(JSONArray original) {
    //    for (int i=0; i < original.length(); i++) {
    //        try {
    //            Object val = original.get(i);
    //            if (val instanceof  JSONObject) {
    //                deeplyRandomizeArrayElementOrder(((JSONObject) val));
    //            } else if (val instanceof JSONArray) {
    //                deeplyRandomizeArrayElementOrder(((JSONArray) val));
    //            }
    //        } catch (JSONException e) {
    //            throw new RuntimeException("Unexpected", e);
    //        }
    //    }
    //
    //    for (int i = 0; i < original.length(); i++) {
    //        int j = ((int) (Math.random() * original.length())) % original.length();
    //        try {
    //            Object iVal = original.get(i);
    //            original.put(i, original.get(j));
    //            original.put(j, iVal);
    //        } catch (JSONException e) {
    //            throw new RuntimeException("Unexpected", e);
    //        }
    //    }
    //}

    //public static void deeplyRandomizeArrayElementOrder(JSONObject original) {
    //    Iterator<String> keys = original.keys();
    //    while (keys.hasNext()) {
    //        String key = keys.next();
    //        try {
    //            Object val = original.get(key);
    //            if (val instanceof JSONArray) {
    //                deeplyRandomizeArrayElementOrder(((JSONArray) val));
    //            } else if (val instanceof JSONObject) {
    //                deeplyRandomizeArrayElementOrder(((JSONObject) val));
    //            }
    //        } catch (JSONException e) {
    //            throw new RuntimeException("Unexpected", e);
    //        }
    //    }
    //}

    /**
     * Takes two JSONObjects representing Capture records, performs a deep 'diff', returning the result as a
     * set of ApidChanges to convert the original into the current.
     *
     * @param original the original copy of the record
     * @param current the current version of the record
     * @return A set of ApidChanges to effect the diff
     * @throws JRCapture.InvalidApidChangeException
     *  If JSON value types mismatch between original and current (I.e. if current is updated in a way that
     *      is not supported by the record schema)
     *  If ids are assigned to new plural elements
     */
    public static Set<ApidChange> compileChangeSet(JSONObject original, JSONObject current)
            throws JRCapture.InvalidApidChangeException {
        return compileChangeSet(original, current, "/");
    }

    private static Set<ApidChange> compileChangeSet(JSONArray original, JSONArray current,
                                                              String arrayAttrPath)
            throws JRCapture.InvalidApidChangeException {
        if (hasIds(original)) {
            return compileChangeSetForArrayWithIds(original, current, arrayAttrPath);
        } else {
            // original array must've been from a JSON blob
            Set<ApidChange> changeSet = new HashSet<ApidChange>();
            if (compareJsonVals(original, current) != 0) {
                changeSet.add(new ApidUpdate(current, arrayAttrPath));
            }
            return changeSet;
        }
    }

    /**
     * Inspects a JSONArray for Capture plural element IDs
     * @param array the array to inspect
     * @return true if the array has elements which are objects which have attributes of name "id"
     */
    public static boolean hasIds(JSONArray array) {
        for (int i=0; i < array.length(); i++) {
            try {
                Object o = array.get(i);
                if (o instanceof JSONObject) {
                    Object maybeId = ((JSONObject) o).opt("id");
                    if (maybeId instanceof Integer || maybeId instanceof Long) return true;
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
        return false;
    }

    private static Set<ApidChange> compileChangeSetForArrayWithIds(JSONArray original,
                                                                             JSONArray current,
                                                                             String arrayAttrPath)
            throws JRCapture.InvalidApidChangeException {
        Set<ApidChange> changeSet = new HashSet<ApidChange>();
        String arrayAttrName = getLastPathElement(arrayAttrPath);
        String relativePath = arrayAttrPath.substring(0, arrayAttrPath.length() - arrayAttrName.length());

        JSONArray sortedOriginal = sortPlurEltsById(original);
        JSONArray sortedCurrent = sortPlurEltsById(current);

        int originalIndex = 0;
        for (int currentIndex = 0; currentIndex < sortedCurrent.length(); currentIndex++) {
            Integer currentId = getIdForPlurEltAtIndex(sortedCurrent, currentIndex);
            final Object currentElt;
            try {
                currentElt = sortedCurrent.get(currentIndex);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }

            if (currentId == null) {
                // new element
                JSONArray wrapperA;
                JSONObject wrapperO;
                try {
                    wrapperA = new JSONArray(Arrays.asList(new Object[]{currentElt}));
                    wrapperO = new JSONObject();
                    wrapperO.put(arrayAttrName, wrapperA);
                } catch (JSONException e) {
                    throw new RuntimeException("Unexpected", e);
                }
                changeSet.add(new ApidUpdate(wrapperO, relativePath));
            } else {
                // update to existing id
                Integer originalId = null;
                while (originalIndex < sortedOriginal.length()) { // try to find a matching id in original
                    originalId = getIdForPlurEltAtIndex(sortedOriginal, originalIndex);
                    if (currentId <= originalId) break;
                    changeSet.add(new ApidDelete(relativePath + "#" + originalId));
                    originalIndex++;
                }

                if (currentId.equals(originalId)) {
                    JSONObject originalElt;
                    try {
                        originalElt = (JSONObject) sortedOriginal.get(originalIndex++);
                    } catch (JSONException e) {
                        throw new RuntimeException("Unexpected", e);
                    }
                    changeSet.addAll(compileChangeSet(originalElt, (JSONObject) currentElt,
                            relativePath + "/" + arrayAttrName + "#" + currentId));
                } else {
                    throw new JRCapture.InvalidApidChangeException("Cannot assign ID to new plural elements");
                }
            }
        }

        while (originalIndex < sortedOriginal.length()) {
            Integer idForPlurEltAtIndex = getIdForPlurEltAtIndex(sortedOriginal, originalIndex++);
            changeSet.add(new ApidDelete(relativePath + "#" + idForPlurEltAtIndex));
        }

        return changeSet;
    }

    private static void inPlaceSortByEltIds(JSONArray original, int start, int len) {
        if (len <= 1) return;

        int halfLen = len / 2;
        int left = start;
        int right = start + halfLen;

        inPlaceSortByEltIds(original, left, halfLen);
        inPlaceSortByEltIds(original, right, len - halfLen);

        Object[] temp = new Object[len];
        int tempIndex = 0;

        while (left < start + halfLen || right < start + len) {
            Object leftVal = original.opt(left);
            Object rightVal = original.opt(right);
            if (left < start + halfLen && right < start + len) {
                Integer leftId = getIdForPlurEltAtIndex(original, left);
                Integer rightId = getIdForPlurEltAtIndex(original, right);
                boolean bothIdsNull = leftId == null && rightId == null;
                leftId = leftId == null ? 0 : leftId;
                rightId = rightId == null ? 0 : rightId;
                if (bothIdsNull && compareJsonVals(leftVal, rightVal) < 0 || leftId < rightId) {
                    temp[tempIndex++] = leftVal;
                    left++;
                } else  {
                    temp[tempIndex++] = rightVal;
                    right++;
                }
            } else if (left < start + halfLen) {
                temp[tempIndex++] = leftVal;
                left++;
            } else {
                temp[tempIndex++] = rightVal;
                right++;
            }
        }

        for (int i=0; i < temp.length; i++)
            try {
                original.put(start + i, temp[i]);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
    }

    private static JSONArray sortPlurEltsById(JSONArray original) {
        try {
            JSONArray copy = (JSONArray) copyJsonVal(original);
            inPlaceSortByEltIds(copy, 0, original.length());
            return copy;
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    private static String getLastPathElement(String relativePath) {
        String[] pathComponents = relativePath.split("/");
        if (pathComponents.length == 0) return null;
        return pathComponents[pathComponents.length - 1];
    }

    private static Integer getIdForPlurEltAtIndex(JSONArray array, int index) {
        try {
            Object o = array.get(index);
            if (o instanceof JSONObject) return ((Integer) ((JSONObject) o).opt("id"));
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected", e);
        }
        return null;
    }

    private static Set<ApidChange> compileChangeSet(JSONObject original, JSONObject current,
                                                              String relativePath)
            throws JRCapture.InvalidApidChangeException {
        SortedSet<String> origKeys = makeSortedSetFromIterator((Iterator<String>) original.keys());
        SortedSet<String> currentKeys = makeSortedSetFromIterator((Iterator<String>) current.keys());

        TreeSet<String> temp = new TreeSet<String>(currentKeys);
        temp.removeAll(origKeys);
        SortedSet<String> newKeys = temp;

        temp = new TreeSet<String>(origKeys);
        temp.removeAll(currentKeys);
        SortedSet<String> removedKeys = temp;

        temp = new TreeSet<String>(origKeys);
        temp.removeAll(removedKeys);

        SortedSet<String> intersection = temp;

        SortedSet<String> changedKeys = new TreeSet<String>();
        for (String k : intersection) {
            try {
                // todo, what if types mismatch between curVal and corresponding val from original?
                Object curVal = current.get(k);
                Object oldVal = original.get(k);

                if (CaptureJsonUtils.compareJsonVals(curVal, oldVal) != 0) changedKeys.add(k);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected JSONException", e);
            }
        }

        Set<ApidChange> changeSet = new HashSet<ApidChange>();
        for (String k : intersection) {
            try {
                Object curVal = current.get(k);
                Object oldVal = original.get(k);
                if (curVal instanceof JSONObject && oldVal instanceof JSONObject) {
                    changeSet.addAll(compileChangeSet(((JSONObject) oldVal), ((JSONObject) curVal),
                            relativePath + k + "/"));
                } else if (curVal instanceof JSONArray && oldVal instanceof JSONArray) {
                    changeSet.addAll(compileChangeSet(((JSONArray) oldVal), ((JSONArray) curVal),
                            relativePath + k));
                } else if (curVal instanceof String) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Boolean) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Double) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Integer) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Long) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal.equals(JSONObject.NULL)) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else {
                    throw createInvalidTypeException(curVal, oldVal);
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected: ", e);
            }
        }

        if (newKeys.size() > 0) {
            throw new JRCapture.InvalidApidChangeException("Can't add new keys to JSONObjects. New keys: " +
                    newKeys.toString());
        }

        if (removedKeys.size() > 0) {
            throw new JRCapture.InvalidApidChangeException("Cannot delete keys from JSONObjects. Removed " +
                    "keys: " + removedKeys.toString());
        }

        return changeSet;
    }

    private static JRCapture.InvalidApidChangeException createInvalidTypeException(Object curVal,
                                                                                   Object oldVal) {
        return new JRCapture.InvalidApidChangeException("Unexpected type(s). Old type: " +
                oldVal.getClass().getSimpleName() + " New type: " +
                curVal.getClass().getSimpleName());
    }

    /**
     * Adds an update to changeSet if curVal.compareTo(oldVal) != 0
     * @param relativePath the relative path for the update
     * @param changeSet the change set to maybe add to
     * @param curVal a value
     * @param oldVal another value
     * @throws JRCapture.InvalidApidChangeException
     */
    private static void maybeAddUpdate(String relativePath, Set<ApidChange> changeSet,
                                       Object curVal, Object oldVal)
            throws JRCapture.InvalidApidChangeException {
        if (!JSONObject.NULL.equals(oldVal) && !oldVal.getClass().isAssignableFrom(curVal.getClass())) {
            throw createInvalidTypeException(curVal, oldVal);
        }

        if (compareJsonVals(curVal, oldVal) != 0) {
            changeSet.add(new ApidUpdate(curVal, relativePath));
        }
    }

    /**
     * "Zippers" up the left and right params, merging fields by key names.
     * Array fields are concatenated. (Left, then right)
     * Object fields are recursively merged.
     * If primitive fields are contained in both left and right then the value from left
     * is taken and the value from right is discarded.
     *
     * e.g.: {'a':1, 'c':[2]} collapsed with {'b':null, 'c':[1]} yields {'a':1, 'b':null, 'c':[2,1]}
     * @param left an object
     * @param right another object
     * @return the merged contents of left and right
     */
    public static JSONObject collapseJsonObjects(JSONObject left, JSONObject right) {
        try {
            if (left == null) return (JSONObject) copyJsonVal(right);
            // copy left
            JSONObject retVal = (JSONObject) copyJsonVal(left);
            if (right == null) return retVal;

            // merge every field from right into the copy
            for (String key : makeSortedSetFromIterator((Iterator<String>) right.keys())) {
                Object rightVal = right.get(key);
                Object leftVal = retVal.opt(key);
                if (leftVal == null) retVal.put(key, copyJsonVal(rightVal));
                if (leftVal instanceof JSONObject) retVal.put(key, collapseJsonObjects(((JSONObject) leftVal),
                        (JSONObject) rightVal));
                if (leftVal instanceof JSONArray) jsonArrayAddAll(((JSONArray) leftVal),
                        (JSONArray) rightVal);
            }

            return retVal;
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    /**
     * An addAll variant for JSONArray
     * @param destArray an array to add elements to
     * @param sourceArray an array with elements to be added
     */
    public static void jsonArrayAddAll(JSONArray destArray, JSONArray sourceArray) {
        for (int i = 0; i < sourceArray.length(); i++) {
            try {
                destArray.put(sourceArray.get(i));
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    /**
     * Copies a JSON value
     * @param val a JSON value
     * @return a copy of val
     * @throws JSONException
     */
    public static Object copyJsonVal(Object val) throws JSONException {
        if (val instanceof JSONObject) {
            return new JSONObject(val.toString());
        } else if (val instanceof JSONArray) {
            return new JSONArray(val.toString());
        } else {
            //everything else is^H^H had better be immutable
            return val;
        }
    }
}
