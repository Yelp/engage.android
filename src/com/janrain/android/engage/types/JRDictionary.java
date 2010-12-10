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

import java.util.HashMap;

import android.text.TextUtils;

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
}
