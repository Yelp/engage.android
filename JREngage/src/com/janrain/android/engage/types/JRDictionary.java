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

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.utils.Archiver;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * iPhone dictionary work-alike class.  Maps string keys to object values.
 */
public final class JRDictionary extends HashMap<String,Object> {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	public static final String DEFAULT_VALUE_STRING = "";
	public static final int DEFAULT_VALUE_INT = -1;
	public static final boolean DEFAULT_VALUE_BOOLEAN = false;
	
	/* Required for unique object identification. */
	private static final long serialVersionUID = 3798456277521362434L;

    /* Type identifier used by GSON library for [de]serialization. */
    private static Type sJRDictionaryType = null;


    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Utility method used to check if a dictionary object is "empty", that is, it is null or
     * contains zero items.
     *
     * @param dictionary
     *      The dictionary object to be tested.
     *
     * @return
     *      <code>true</code> if the dictionary is null or contains zero items, <code>false</code>
     *      otherwise.
     */
    public static boolean isEmpty(JRDictionary dictionary) {
        return ((dictionary == null) || (dictionary.size() == 0));
    }

    /**
     * Archives the specified JRDictionary object to disk.
     *
     * @param name
     *      The name the JRDictionary will be saved as on disk.  This parameter cannot be null.
     *
     * @param dictionary
     *      The dictionary object to be saved.
     *
     * 		Returns <code>true</code> if the save operation is successful, <code>false</code>
     * 		otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static boolean archive(String name, JRDictionary dictionary) {
        return Archiver.save(name, dictionary);
    }

    /**
     * Loads (unarchives) the specified JRDictionary object from the local (protected) file system.
     *
     * @param name
     * 		The name of the JRDictionary to be loaded from disk.  This parameter cannot be null.
     *
     * @return
     * 		The JRDictionary if found and loaded, new (empty) JRDictionary otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static JRDictionary unarchive(String name) {
        Object obj = Archiver.load(name);
        if ((obj != null) && (obj instanceof JRDictionary)) {
            return (JRDictionary)obj;
        }
        return new JRDictionary();
    }

    /**
     * Serializes the specified dictionary object to a JSON string.
     *
     * @param dictionary
     *      The JRDictionary object to serialize.
     *
     * @return
     *      JSON representation of the specified JRDictionary object.
     */
    public static String toJSON(JRDictionary dictionary) {
        return new Gson().toJson(dictionary, getTypeForGSON());
    }

    /**
     * Deserializes the specified JSON string to a JRDictionary instance.
     *
     * @param json
     *      The JSON string to be deserialized.
     *
     * @return
     *      A JRDictionary object representation of the JSON string.
     */
    public static JRDictionary fromJSON(String json) {
        return new Gson().fromJson(json, getTypeForGSON());
    }

    /**
     * Lazily initializes the type object instance used for [de]serialization.
     *
     * @return
     *      The type object used for GSON [de]serialization.
     */
    private static Type getTypeForGSON() {
        if (sJRDictionaryType == null) {
            sJRDictionaryType = new TypeToken<JRDictionary>(){}.getType();
        }
        return sJRDictionaryType;
    }

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    public JRDictionary() {
        super();
    }

    /**
     * Initializing constructor.  Creates instance of JRDictionary with the specified
     * initial size/capacity.
     *
     * @param capacity
     *      Initial size/capacity of JRDictionary instance.
     */
    public JRDictionary(int capacity) {
        super(capacity);
    }

    /**
     * Copy constructor.
     *
     * @param dictionary
     *      Dictionary instance to clone.
     */
    public JRDictionary(JRDictionary dictionary) {
        if (!JRDictionary.isEmpty(dictionary)) {
            putAll(dictionary);
        }
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
	
	/**
	 * Convenience method used to retrieve a named value as a String object.
	 *  
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @return
	 * 		String object if found, empty string otherwise.
	 */
	public String getAsString(String key) {
		return getAsString(key, DEFAULT_VALUE_STRING);
	}
	
	/**
	 * Convenience method used to retrieve a named value as a String object.
	 *  
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @param defaultValue
	 * 		The value to be returned if the key is not found.
	 * 
	 * @return
	 * 		String value if found, value of 'defaultValue' otherwise.
	 */
	public String getAsString(String key, String defaultValue) {
		return (containsKey(key)) ? (String)get(key) : defaultValue;
	}
	
	/**
	 * Convenience method used to retrieve a named value as an int.
	 *  
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @return
	 * 		integer value if found, -1 otherwise.
	 */
	public int getAsInt(String key) {
		return getAsInt(key, DEFAULT_VALUE_INT);
	}
	
	/**
	 * Convenience method used to retrieve a named value as an int.
	 *  
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @param defaultValue
	 * 		The value to be returned if the key is not found.
	 *
	 * @return
	 * 		integer value if found, value of 'defaultValue' otherwise.
	 */
	public int getAsInt(String key, int defaultValue) {
		int retval = defaultValue;
		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
			Object value = get(key);
			if (value instanceof Integer) {
				retval = ((Integer)value).intValue();
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
	 * Convenience method used to retrieve a named value as a boolean.
	 *  
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @return
	 * 		boolean value if found, false otherwise.
	 */
	public boolean getAsBoolean(String key) {
		return getAsBoolean(key, DEFAULT_VALUE_BOOLEAN);
	}
	
	/**
	 * Convenience method used to retrieve a named value as a boolean.
	 *  
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @param defaultValue
	 * 		The value to be returned if the key is not found.
	 *
	 * @return
	 * 		boolean value if found, value of 'defaultValue' otherwise.
	 */
	public boolean getAsBoolean(String key, boolean defaultValue) {
		boolean retval = defaultValue;
		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
			Object value = get(key);
			if (value instanceof Boolean) {
				retval = ((Boolean)value).booleanValue();
			} else if (value instanceof String) {
				String strValue = (String)value;
				if (!TextUtils.isEmpty(strValue)) {
					try {
						retval = Boolean.parseBoolean(strValue);
					} catch (Exception e) {
						// string does not contain true|false (case-insensitive), try yes|no
						if ("yes".equalsIgnoreCase(strValue)) {
							retval = true;
						} else if ("no".equalsIgnoreCase(strValue)) {
							retval = false;
						}
					}
				}
			}
		}
		return retval;
	}

	/**
	 * Convenience method used to retrieve a named value as a JRDictionary.
	 * 
	 * @param key
	 * 		The key of the value to be retrieved.
	 *
	 * @return
	 * 		The JRDictionary value if key is found, null otherwise.
	 */
	public JRDictionary getAsDictionary(String key) {
		return getAsDictionary(key, false);
	}
	
	/**
	 * Convenience method used to retrieve a named value as a JRDictionary.
	 * 
	 * @param key
	 * 		The key of the value to be retrieved.
	 * 
	 * @param shouldCreateIfNotFound
	 * 		Flag indicating whether or not a new JRDictionary object should be created if the 
	 * 		specified key does not exist.
	 * 
	 * @return
	 * 		The JRDictionary value if key is found, empty object or null otherwise (based on value
	 *      of the 'shouldCreateIfNotFound' flag).
	 */
	public JRDictionary getAsDictionary(String key, boolean shouldCreateIfNotFound) {
		JRDictionary retval = null;
		if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
			Object value = get(key);
			if (value instanceof JRDictionary) {
				retval = (JRDictionary)value;
			}
		}
		
		return ((retval == null) && shouldCreateIfNotFound)
			? new JRDictionary()
			: retval;
	}

    /**
     * Convenience method used to retrieve a named value as a JRProvider
     *
     * @param key
     *      The key of the value to be retrieved.
     *
     * @return
     *      The JRProvider object if found, null otherwise.
     */
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
     * Convenience method used to retrieve a named value as a JRProviderList.
     *
     * @param key
     * 		The key of the value to be retrieved.
     *
     * @return
     * 		The JRProviderList value if key is found, null otherwise.
     */
    public JRProviderList getAsProviderList(String key) {
        return getAsProviderList(key, false);
    }

    /**
     * Convenience method used to retrieve a named value as a JRProviderList.
     *
     * @param key
     * 		The key of the value to be retrieved.
     *
     * @param shouldCreateIfNotFound
     * 		Flag indicating whether or not a new JRProviderList object should be created if the
     * 		specified key does not exist.
     *
     * @return
     * 		The JRProviderList value if key is found, null otherwise.
     */
    public JRProviderList getAsProviderList(String key, boolean shouldCreateIfNotFound) {
        JRProviderList retval = null;
        if ((!TextUtils.isEmpty(key)) && (containsKey(key))) {
            Object value = get(key);
            if (value instanceof JRProviderList) {
                retval = (JRProviderList)value;
            }
        }

        return ((retval == null) && shouldCreateIfNotFound)
            ? new JRProviderList()
            : retval;
    }
}
