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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA. User: nathan Date: 2/5/13 Time: 6:18 PM To change this template use File |
 * Settings | File Templates.
 */
public class CaptureJsonUtils {
    public static int jsonArrayCompareTo(JSONArray this_, Object other) {
        if (other instanceof JSONArray) {
            return jsonArrayCompareTo(this_, (JSONArray) other);
        } else if (other == null) {
            return 1;
        } else return this_.getClass().getName().compareTo(other.getClass().getName());
    }

    public static int jsonArrayCompareTo(JSONArray this_, JSONArray otherArray) {
        if (otherArray == null) return 1;
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

        return 0;
    }

    private static int compareJsonVals(Object thisValue, Object otherVal) {
        Integer comparison = null;
        if (thisValue instanceof JSONArray) {
            comparison = jsonArrayCompareTo((JSONArray) thisValue, otherVal);
        } else if (thisValue instanceof JSONObject) {
            comparison = jsonObjectCompareTo((JSONObject) thisValue, otherVal);
        } else if (thisValue instanceof Double) {
            if (otherVal instanceof Double) {
                comparison = ((Double) thisValue).compareTo((Double) otherVal);
            }
        } else if (thisValue instanceof Long) {
            if (otherVal instanceof Long) {
                comparison = ((Long) thisValue).compareTo((Long) otherVal);
            }
        } else if (thisValue instanceof Integer) {
            if (otherVal instanceof Integer) {
                comparison = ((Integer) thisValue).compareTo((Integer) otherVal);
            }
        } else if (thisValue instanceof Byte) {
            if (otherVal instanceof Byte) {
                comparison = ((Byte) thisValue).compareTo((Byte) otherVal);
            }
        } else if (thisValue instanceof Boolean) {
            if (otherVal instanceof Boolean) {
                comparison = ((Boolean) thisValue).compareTo((Boolean) otherVal);
            }
        } else if (thisValue instanceof String) {
            if (otherVal instanceof String) {
                comparison = ((String) thisValue).compareTo((String) otherVal);
            }
        } else if (thisValue.equals(JSONObject.NULL)) {
            if (otherVal.equals(JSONObject.NULL)) comparison = 0;
        } else {
            throw new RuntimeException("Unexpected type in compareTo for: " + thisValue);
        }

        if (comparison == null) {
            comparison = thisValue.getClass().getName().compareTo(otherVal.getClass().getName());
        }
        return comparison;
    }

    public static int jsonObjectCompareTo(JSONObject this_, Object other) {
        if (other instanceof JSONObject) {
            return jsonObjectCompareTo(this_, (JSONObject) other);
        } else if (other == null) {
            return 1;
        } else return this_.getClass().getName().compareTo(other.getClass().getName());
    }

    public static int jsonObjectCompareTo(JSONObject this_, JSONObject other) {
        SortedSet<String> this_Keys = sortedSetFromIterator((Iterator<String>) this_.keys());
        SortedSet<String> otherKeys = sortedSetFromIterator((Iterator<String>) other.keys());

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

    // this is not generic because of type erasure and array covariance
    //public static String[] iteratorAsArray(Iterator<String> names) {
    //    ArrayList<String> temp = new ArrayList<String>();
    //    while (names.hasNext()) temp.add(names.next());
    //    return temp.toArray(new String[temp.size()]);
    //}

    public static <T> SortedSet<T> sortedSetFromIterator(Iterator<T> i) {
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
                if (val instanceof JSONArray) deepArraySort(((JSONArray) val));
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }
}
