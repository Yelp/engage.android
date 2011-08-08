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

/*
 * XXX some of these getAs<T> methods return a copy of the value (getAsDictionary,
 * getAsProviderList) and some return a reference to the value.
 */

import android.text.TextUtils;
import android.util.Log;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.utils.Archiver;
import com.janrain.android.engage.utils.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @class JRDictionary
 * @brief maps string keys to object values
 *
 * @nosubgrouping
 *
 * @internal
 * iPhone dictionary work-alike class
 * @endinternal
 **/
public final class JRDictionary extends HashMap<String,Object> {

	public static final String DEFAULT_VALUE_STRING = "";
	public static final int DEFAULT_VALUE_INT = -1;
	public static final boolean DEFAULT_VALUE_BOOLEAN = false;

	/* Required for unique object identification. */
	private static final long serialVersionUID = 3798456277521362434L;

    /* Tag used for logging. */
    private static final String TAG = JRDictionary.class.getSimpleName();

/**
 * @name Constructors
 * Methods that manage authenticated users remembered by the library
 **/
/*@{*/
    /**
     * Default constructor.
     **/
    public JRDictionary() {
        super();
    }

    /**
     * Initializing constructor.  Creates instance of JRDictionary with the specified
     * initial size/capacity.
     *
     * @param capacity
     *      Initial size/capacity of JRDictionary instance
     **/
    public JRDictionary(int capacity) {
        super(capacity);
    }

    /**
     * Copy constructor (for base type).
     *
     * @param map
     *      The Map to copy
     **/
    public JRDictionary(Map map) {
        if (map != null) {
            for (Object k : map.keySet()) {
                assert k instanceof String;
                put((String) k, map.get(k));
            }
        }
    }

    /**
     * Copy constructor.
     *
     * @param dictionary
     *      Dictionary instance to clone
     **/
    public JRDictionary(JRDictionary dictionary) {
        if (!JRDictionary.isEmpty(dictionary)) {
            putAll(dictionary);
        }
    }
/*@}*/

/**
 * @name Archving
 * Methods that manage archiving/unarchiving of JRDictionaary
 **/
/*@{*/
    /**
     * Archives the specified JRDictionary object to disk.
     *
     * @param name
     *      The name the JRDictionary will be saved as on disk.  This parameter cannot be null
     *
     * @param dictionary
     *      The dictionary object to be saved
     *
     * @return
     *      \c true if the save operation is successful, \c false otherwise
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null
     **/
    public static boolean archive(String name, JRDictionary dictionary) {
        return Archiver.save(name, dictionary);
    }

    /**
     * Loads (unarchives) the specified JRDictionary object from the local (protected) file system.
     *
     * @param name
     * 		The name of the JRDictionary to be loaded from disk.  This parameter cannot be null
     *
     * @return
     * 		The JRDictionary if found and loaded, new (empty) JRDictionary otherwise
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null
     **/
    public static JRDictionary unarchive(String name) {
        Object obj = Archiver.load(name);
        if ((obj != null) && (obj instanceof JRDictionary)) {
            return (JRDictionary)obj;
        }
        return new JRDictionary();
    }
/*@}*/

/**
 * @name JSON Seririalization
 * Methods that serialize/deserialize the JRDictionary to/from JSON
 **/
/*@{*/
    /**
     * Serializes the specified dictionary object to a JSON string.
     *
     * @return
     *      JSON representation of the specified JRDictionary object
     **/
    public String toJSON() {
        String retval = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            retval = mapper.writeValueAsString(this);
        } catch (IOException e) {
            Log.w(TAG, "[toJSON] problem serializing JSON string: ", e);
        }

        return retval;
    }

    /**
     * Deserializes the specified JSON string to a JRDictionary instance.
     *
     * @param json
     *      The JSON string to be deserialized.
     *
     * @return
     *      A JRDictionary object representation of the JSON string
     **/
    public static JRDictionary fromJSON(String json) {
        JRDictionary retVal = null;

        try {
            ObjectMapper mapper = new ObjectMapper();

            retVal = mapper.readValue(json, JRDictionary.class);
        } catch (IOException e) {
            Log.w(TAG, "[fromJSON] problem deserializing JSON string: ", e);
        }

        return retVal;
    }
/*@}*/

/**
 * @name Getting Dictionary Content
 * Methods that return typed values given a String key
 **/
/*@{*/
	/**
	 * Convenience method used to retrieve a named value as a \e String object.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @return
	 * 		\e String object if found, empty string otherwise
	 **/
	public String getAsString(String key) {
		return getAsString(key, DEFAULT_VALUE_STRING);
	}

	/**
	 * Convenience method used to retrieve a named value as a \e String object.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @param defaultValue
	 * 		The value to be returned if the key is not found
	 *
	 * @return
	 * 		\e String value if found, value of 'defaultValue' otherwise
	 **/
	public String getAsString(String key, String defaultValue) {
		return (containsKey(key)) ? (String)get(key) : defaultValue;
	}

	/**
	 * Convenience method used to retrieve a named value as an \e int.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @return
	 * 		\e integer value if found, \c -1 otherwise
	 **/
	public int getAsInt(String key) {
		return getAsInt(key, DEFAULT_VALUE_INT);
	}

	/**
	 * Convenience method used to retrieve a named value as an \e int.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @param defaultValue
	 * 		The value to be returned if the key is not found
	 *
	 * @return
	 * 		\e integer value if found, value of 'defaultValue' otherwise
	 **/
	public int getAsInt(String key, int defaultValue) {
		int retval = defaultValue;
		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
			Object value = get(key);
			if (value instanceof Integer) {
				retval = (Integer) value;
			} else if (value instanceof String) {
				String strValue = (String)value;
				if (!TextUtils.isEmpty(strValue)) {
					try {
						retval = Integer.parseInt(strValue);
					} catch (Exception ignore) {
						// string value is not an integer...return default value...
					}
				}
			}
		}
		return retval;
	}

	/**
	 * Convenience method used to retrieve a named value as a \e boolean.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @return
	 * 		\e boolean value if found, \c false otherwise
	 **/
	public boolean getAsBoolean(String key) {
		return getAsBoolean(key, DEFAULT_VALUE_BOOLEAN);
	}

	/**
	 * Convenience method used to retrieve a named value as a \e boolean.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @param defaultValue
	 * 		The value to be returned if the key is not found
	 *
	 * @return
	 * 		\e boolean value if found, value of 'defaultValue' otherwise
	 **/
	public boolean getAsBoolean(String key, boolean defaultValue) {
		boolean retval = defaultValue;
		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
			Object value = get(key);
			if (value instanceof Boolean) {
				retval = (Boolean) value;
			} else if (value instanceof String) {
				String strValue = (String)value;
                retval = StringUtils.stringToBoolean(strValue, false);
			}
		}
		return retval;
	}

	/**
	 * Convenience method used to retrieve a named value as a JRDictionary.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @return
	 * 		The JRDictionary value if key is found, null otherwise
	 **/
	public JRDictionary getAsDictionary(String key) {
		return getAsDictionary(key, false);
	}

	/**
	 * Convenience method used to retrieve a named value as a JRDictionary.
	 *
	 * @param key
	 * 		The key of the value to be retrieved
	 *
	 * @param shouldCreateIfNotFound
	 * 		Flag indicating whether or not a new JRDictionary object should be created if the
	 * 		specified key does not exist
	 *
	 * @return
	 * 		The JRDictionary value if key is found, empty object or null otherwise (based on value
	 *      of the 'shouldCreateIfNotFound' flag)
	 **/
	public JRDictionary getAsDictionary(String key, boolean shouldCreateIfNotFound) {
		JRDictionary retval = null;
		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
			Object value = get(key);
			if (value instanceof JRDictionary) {
				retval = (JRDictionary)value;
			} else if (value instanceof Map) {
                retval = new JRDictionary((Map) value);
            }
		}

		return ((retval == null) && shouldCreateIfNotFound)
			? new JRDictionary()
			: retval;
	}

    /**
     * Convenience method used to retrieve a named value as an array of strings.
     *
     * @param key
     * 		The key of the value to be retrieved
     *
     * @return
     * 		The \e ArrayList<String> value if key is found, null otherwise
     **/
    public ArrayList<String> getAsListOfStrings(String key) {
        return getAsListOfStrings(key, false);
    }

    /**
     * Convenience method used to retrieve a named value as an array of strings.
     *
     * @param key
     * 		The key of the value to be retrieved
     *
     * @param shouldCreateIfNotFound
     * 		Flag indicating whether or not a new \e ArrayList<String> object should be created
     * 		if the specified key does not exist
     *
     * @return
     * 		The \e ArrayList<String> value if key is found, null otherwise
     **/

    // We runtime type check the return value so we can safely ignore this unchecked
    // assignment error.
    @SuppressWarnings("unchecked")
    public ArrayList<String> getAsListOfStrings(String key, boolean shouldCreateIfNotFound) {
        ArrayList<String> retval = null;
        if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
            Object value = get(key);
            if (value instanceof ArrayList) {
                for (Object v : (ArrayList) value) assert v instanceof String;
                retval = (ArrayList<String>)value;
            }
        }

        return ((retval == null) && shouldCreateIfNotFound)
            ? new ArrayList<String>()
            : retval;
    }
/*@}*/

    /**
     * @internal
     * Convenience method used to retrieve a named value as a JRProvider
     *
     * @param key
     *      The key of the value to be retrieved
     *
     * @return
     *      The JRProvider object if found, null otherwise
     **/
    public JRProvider getAsProvider(String key) {
        JRProvider retval = null;
        if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
            Object value = get(key);
            if (value instanceof JRProvider) {
                retval = (JRProvider) value;
            }
        }

        return retval;
    }

    /**
     * @internal
     * Convenience method used to retrieve a named value as a JRProviderList
     *
     * @param key
     * 		The key of the value to be retrieved
     *
     * @return
     * 		The JRProviderList value if key is found, null otherwise
     **/
    public JRProviderList getAsProviderList(String key) {
        return getAsProviderList(key, false);
    }

    /**
     * @internal
     * Convenience method used to retrieve a named value as a JRProviderList
     *
     * @param key
     * 		The key of the value to be retrieved
     *
     * @param shouldCreateIfNotFound
     * 		Flag indicating whether or not a new JRProviderList object should be created if the
     * 		specified key does not exist
     *
     * @return
     * 		The JRProviderList value if key is found, null otherwise
     **/


    // We runtime type check the return value so we can safely ignore this unchecked
    // assignment error.
    @SuppressWarnings({ "unchecked" })
	public JRProviderList getAsProviderList(String key, boolean shouldCreateIfNotFound) {
        JRProviderList retval = null;
        if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
            Object value = get(key);
            if (value instanceof JRProviderList) {
                retval = (JRProviderList)value;
            } else if (value instanceof ArrayList) {
                for (Object v : (ArrayList) value) assert v instanceof JRProvider;
            	retval = new JRProviderList((ArrayList<JRProvider>)value);
            }
        }

        return ((retval == null) && shouldCreateIfNotFound)
            ? new JRProviderList()
            : retval;
    }

/**
 * @name Miscellaneous
 * Miscellanous methods
 **/
/*@{*/
    /**
     * Utility method used to check if a dictionary object is "empty", that is, it is null or
     * contains zero items
     *
     * @param dictionary
     *      The dictionary object to be tested
     *
     * @return
     *      <code>true</code> if the dictionary is null or contains zero items, <code>false</code>
     *      otherwise
     **/
    public static boolean isEmpty(JRDictionary dictionary) {
        return ((dictionary == null) || (dictionary.size() == 0));
    }
/*@}*/
}
