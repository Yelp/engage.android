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

package com.janrain.capture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CaptureJsonUtils {
    public static int jsonArrayCompareTo(JSONArray this_, Object other) {
        if (other instanceof JSONArray) {
            return jsonArrayCompareTo(this_, (JSONArray) other);
        } else return this_.getClass().getName().compareTo(other.getClass().getName());
    }

    public static int jsonArrayCompareTo(JSONArray this_, JSONArray otherArray) {
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
     *  If the types are JSONObject their fields are sorted by key and compared. The first non zero
     *    comparison is the return value, if all fields have a zero comparison then so do the JSONObjects
     *  If the types are JSONArray their elements are compared in order and the first non zero comparison
     *    is the comparison of the JSONArrays
     *  If the values are both JSONObject.NULL this method returns 0
     *  nulls are illegal
     *
     *  Note that number types aren't compared if their types are unequal, e.g. 1.0 the Double does not
     *  equal 1 the integer.
     *
     * @param thisVal
     * @param otherVal
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

    public static int jsonObjectCompareTo(JSONObject this_, Object other) {
        if (other instanceof JSONObject) {
            return jsonObjectCompareTo(this_, (JSONObject) other);
        } else return this_.getClass().getName().compareTo(other.getClass().getName());
    }

    public static int jsonObjectCompareTo(JSONObject this_, JSONObject other) {
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
     * @param source JSONObject to deep copy, assumed to be acyclic
     * @param dest JSONObject to deep copy source into, assumed to be empty
     */
    public static void deepCopy(JSONObject source, JSONObject dest) {
        // poor type safety here is fallout of JSONObject not having a copy constructor or supporting
        // clone

        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // copy each key by hand, copying mutable types, not copying immutable types :(
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

    public static <T> SortedSet<T> makeSortedSetFromIterator(Iterator<T> i) {
        SortedSet<T> retval = new TreeSet<T>();
        while (i.hasNext()) retval.add(i.next());
        return retval;
    }

    public static void deepArraySort(JSONArray original) {
        for (int i=0; i < original.length(); i++) {
            try {
                Object val = original.get(i);
                if (val instanceof  JSONObject) {
                    deepArraySort(((JSONObject) val));
                } else if (val instanceof JSONArray) {
                    deepArraySort(((JSONArray) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        try {
            mergeSort(original, 0, original.length());
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    private static void mergeSort(JSONArray original, int start, int len) throws JSONException {
        if (len <= 1) return;

        int halfLen = len / 2;
        int left = start;
        int right = start + halfLen;

        mergeSort(original, left, halfLen);
        mergeSort(original, right, len - halfLen);

        Object[] temp = new Object[len];
        int tempIndex = 0;

        while (left < start + halfLen || right < start + len) {
            Object leftVal = original.opt(left);
            Object rightVal = original.opt(right);
            if (left < start + halfLen && right < start + len) {
                if (compareJsonVals(leftVal, rightVal) < 0) {
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

        for (int i=0; i < temp.length; i++) original.put(start + i, temp[i]);
    }

    public static void deepArraySort(JSONObject original) {
        Iterator<String> keys = original.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object val = original.get(key);
                if (val instanceof JSONArray) {
                    deepArraySort(((JSONArray) val));
                } else if (val instanceof JSONObject) {
                    deepArraySort(((JSONObject) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    public static void deepArrayOrderRandomizer(JSONArray original) {
        for (int i=0; i < original.length(); i++) {
            try {
                Object val = original.get(i);
                if (val instanceof  JSONObject) {
                    deeplyRandomizeArrayElementOrder(((JSONObject) val));
                } else if (val instanceof JSONArray) {
                    deepArrayOrderRandomizer(((JSONArray) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        for (int i = 0; i < original.length(); i++) {
            int j = ((int) (Math.random() * original.length())) % original.length();
            Object iVal = null;
            try {
                iVal = original.get(i);
                original.put(i, original.get(j));
                original.put(j, iVal);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    public static void deeplyRandomizeArrayElementOrder(JSONObject original) {
        Iterator<String> keys = original.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object val = original.get(key);
                if (val instanceof JSONArray) {
                    deepArrayOrderRandomizer(((JSONArray) val));
                } else if (val instanceof JSONObject) {
                    deeplyRandomizeArrayElementOrder(((JSONObject) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    /**
     * Takes two JSONObjects, performs a deep deef, returning the result as a set of ApidChange instances
     * Assumes both JSON object hierarchies has sorted arrays
     * @param original
     * @param current
     * @return
     * @throws JRCapture.InvalidApidChangeException
     */
    public static Set<JRCapture.ApidChange> deepDiff(JSONObject original, JSONObject current)
            throws JRCapture.InvalidApidChangeException {
        return deepDiff(original, current, "/");
    }

    /**
     * @param original
     * @param current
     * @param arrayAttrPath has no trailing slash
     * @return
     * @throws JRCapture.InvalidApidChangeException
     */
    private static Set<JRCapture.ApidChange> deepDiff(JSONArray original, JSONArray current,
                                                      String arrayAttrPath)
            throws JRCapture.InvalidApidChangeException {
        if (hasIds(original)) {
            return deepDiffArrayWithIds(original, current, arrayAttrPath);
        } else {
            // original array must've been from a JSON blob
            deepArraySort(original);
            deepArraySort(current);
            Set<JRCapture.ApidChange> changeSet = new HashSet<JRCapture.ApidChange>();
            if (compareJsonVals(original, current) != 0) {
                changeSet.add(new JRCapture.ApidUpdate(current, arrayAttrPath));
            }
            return changeSet;
        }
    }

    private static boolean hasIds(JSONArray original) {
        for (int i=0; i < original.length(); i++) {
            try {
                Object o = original.get(i);
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

    private static Set<JRCapture.ApidChange> deepDiffArrayWithIds(JSONArray original, JSONArray current,
                                                      String arrayAttrPath)
            throws JRCapture.InvalidApidChangeException {
        Set<JRCapture.ApidChange> changeSet = new HashSet<JRCapture.ApidChange>();
        String arrayAttrName = getLastPathElement(arrayAttrPath);
        String relativePath = arrayAttrPath.substring(0, arrayAttrPath.length() - arrayAttrName.length());

        sortPlurEltsById(original);
        sortPlurEltsById(current);

        int originalIndex = 0;
        for (int currentIndex = 0; currentIndex < current.length(); currentIndex++) {
            Integer currentId = getIdForPlurEltAtIndex(current, currentIndex);
            final Object currentElt;
            try {
                currentElt = current.get(currentIndex);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }

            if (currentId == null) {
                // new element
                JSONArray wrapperA;
                JSONObject wrapperO;
                try {
                    wrapperA = new JSONArray(new Object[]{currentElt});
                    wrapperO = new JSONObject(((Map) new HashMap<String, JSONArray>().put(arrayAttrName,
                            wrapperA)));
                } catch (JSONException e) {
                    throw new RuntimeException("Unexpected", e);
                }
                changeSet.add(new JRCapture.ApidUpdate(wrapperO, relativePath));
            } else {
                // update to existing id
                Integer originalId = null;
                while (originalIndex < original.length()) { // try to find a matching id in original
                    originalId = getIdForPlurEltAtIndex(original, originalIndex);
                    if (currentId <= originalId) break;
                    changeSet.add(new JRCapture.ApidDelete(relativePath + "#" + originalId));
                    originalIndex++;
                }

                if (currentId.equals(originalId)) {
                    JSONObject originalElt;
                    try {
                        originalElt = (JSONObject) original.get(originalIndex++);
                    } catch (JSONException e) {
                        throw new RuntimeException("Unexpected", e);
                    }
                    changeSet.addAll(deepDiff(originalElt, (JSONObject) currentElt,
                            relativePath + "/" + arrayAttrName + "#" + currentId));
                } else {
                    throw new JRCapture.InvalidApidChangeException("Cannot assign ID to new plural elements");
                }
            }
        }

        while (originalIndex < original.length()) {
            Integer idForPlurEltAtIndex = getIdForPlurEltAtIndex(original, originalIndex);
            changeSet.add(new JRCapture.ApidDelete(relativePath + "#" + idForPlurEltAtIndex));
        }

        return changeSet;
    }

    private static void sortPlurEltsById(JSONArray original, int start, int len) {
        if (len <= 1) return;

        int halfLen = len / 2;
        int left = start;
        int right = start + halfLen;

        sortPlurEltsById(original, left, halfLen);
        sortPlurEltsById(original, right, len - halfLen);

        Object[] temp = new Object[len];
        int tempIndex = 0;

        while (left < start + halfLen || right < start + len) {
            Object leftVal = original.opt(left);
            Object rightVal = original.opt(right);
            Integer leftId = getIdForPlurEltAtIndex(original, left);
            Integer rightId = getIdForPlurEltAtIndex(original, right);
            if (left < start + halfLen && right < start + len) {
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

    private static void sortPlurEltsById(JSONArray original) {
        sortPlurEltsById(original, 0, original.length());
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

    /**
     * @param original
     * @param current
     * @param relativePath with a trailing slash
     * @return
     * @throws JRCapture.InvalidApidChangeException
     */
    private static Set<JRCapture.ApidChange> deepDiff(JSONObject original, JSONObject current,
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

        Set<JRCapture.ApidChange> changeSet = new HashSet<JRCapture.ApidChange>();
        for (String k : intersection) {
            try {
                Object curVal = current.get(k);
                Object oldVal = original.get(k);
                if (curVal instanceof JSONObject && oldVal instanceof JSONObject) {
                    changeSet.addAll(deepDiff(((JSONObject) oldVal), ((JSONObject) curVal),
                            relativePath + k + "/"));
                } else if (curVal instanceof JSONArray && oldVal instanceof JSONArray) {
                    changeSet.addAll(deepDiff(((JSONArray) oldVal), ((JSONArray) curVal), relativePath + k));
                } else if (curVal instanceof String && oldVal instanceof String) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Boolean && oldVal instanceof Boolean) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Double && oldVal instanceof Double) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Integer && oldVal instanceof Integer) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal instanceof Long && oldVal instanceof Long) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else if (curVal.equals(JSONObject.NULL)) {
                    maybeAddUpdate(relativePath + k, changeSet, curVal, oldVal);
                } else {
                    throw new JRCapture.InvalidApidChangeException("Unexpected type(s). Old type: " +
                            oldVal.getClass().getSimpleName() + " New type: " +
                            curVal.getClass().getSimpleName());
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected: ", e);
            }
        }

        //CaptureStringUtils.log("newKeys: " + newKeys.toString() + " removedKeys: " + removedKeys.toString() +
        //        " changedKeys: " + changedKeys.toString());

        if (newKeys.size() > 0) {
            throw new JRCapture.InvalidApidChangeException("Can't add new keys to JSONObjects");
        }
        // new leaf (illegal?), changed leaf, removed leaf (illegal?), new branch, removed branch, that's it?

        if (newKeys.size() > 0 || removedKeys.size() > 0) {
            throw new JRCapture.InvalidApidChangeException("Cannot introduce or delete leaf objects. New " +
                    "keys: " + newKeys.toString() + " Removed keys: " + removedKeys.toString());
        }

        return changeSet;
    }

    private static void maybeAddUpdate(String relativePath,
                                       Set<JRCapture.ApidChange> changeSet,
                                       Object curVal, Object oldVal) {
        if (compareJsonVals(curVal, oldVal) != 0) {
            changeSet.add(new JRCapture.ApidUpdate(curVal, relativePath));
        }
    }
}
