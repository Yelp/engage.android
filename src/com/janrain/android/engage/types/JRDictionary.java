package com.janrain.android.engage.types;

import java.util.HashMap;

import android.text.TextUtils;

public final class JRDictionary extends HashMap<String,Object> {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	public static final String DEFAULT_VALUE_STRING = "";
	public static final int DEFAULT_VALUE_INT = -1;
	public static final boolean DEFAULT_VALUE_BOOLEAN = false;
	
	/* Required for unique object identification. */
	private static final long serialVersionUID = 3798456277521362434L;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
	
	public String getAsString(String key) {
		return getAsString(key, DEFAULT_VALUE_STRING);
	}
	
	public String getAsString(String key, String defaultValue) {
		return (containsKey(key)) ? (String)get(key) : defaultValue;
	}
	
	public int getAsInt(String key) {
		return getAsInt(key, DEFAULT_VALUE_INT);
	}
	
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
	
	public boolean getAsBoolean(String key) {
		return getAsBoolean(key, DEFAULT_VALUE_BOOLEAN);
	}
	
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

	public JRDictionary getAsDictionary(String key) {
		return getAsDictionary(key, false);
	}
	
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
