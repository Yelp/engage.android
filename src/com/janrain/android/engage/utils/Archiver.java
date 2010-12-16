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
package com.janrain.android.engage.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class used to archiveDictionary/unarchive objects.  This implementation serializes
 * the object to a binary format and saves to the protected storage area (disk).
 */
public final class Archiver {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	// tag used for logging
	private static final String TAG = Archiver.class.getSimpleName();
	// token used to separate parts of dictionary file name
	private static final String DICTIONARY_FILE_SEPARATOR = "~";
	// prefix + dictionary name
	private static final String DICTIONARY_BASE_FORMAT = "dict" + DICTIONARY_FILE_SEPARATOR + "%s";

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Saves (archives) the specified object to the local (protected) file system.
     *
     * @param context
     * 		The application context used for directory/file access.  This parameter cannot be null.
     *
     * @param name
     * 		The name the object will be saved as on disk.  This parameter cannot be null.
     *
     * @param object
     * 		The object to be saved.
     *
     * @return
     * 		Returns <code>true</code> if the save operation is successful, <code>false</code>
     * 		otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static boolean save(Context context, String name, Object object) {
        if (context == null) {
            throw new IllegalArgumentException("context parameter cannot be null");
        } else if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name parameter cannot be null");
        }

        String fileName = String.format(DICTIONARY_BASE_FORMAT, name);

        // purge existing file
        context.deleteFile(fileName);

        // then save current dictionary contents
        return saveObject(context, fileName, object);
    }

    /**
     * Loads (unarchives) the specified object from the local (protected) file system.
     *
     * @param context
     * 		The application context used for directory/file access.  This parameter cannot be null.
     *
     * @param name
     * 		The name of the object to be loaded from disk.  This parameter cannot be null.
     *
     * @return
     * 		The object if found and loaded, null otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static Object load(Context context, String name) {
        if (context == null) {
            throw new IllegalArgumentException("context parameter cannot be null");
        } else if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name parameter cannot be null");
        }

        String fileName = String.format(DICTIONARY_BASE_FORMAT, name);
        return loadObject(context, fileName);
    }

    /**
     * Loads (unarchives) the specified string object from the local (protected) file system.
     * 
     * @param context
     * 		The application context used for directory/file access.  This parameter cannot be null.
     *
     * @param name
     * 		The name of the string to be loaded from disk.  This parameter cannot be null.
     *
     * @return
     * 		The string if found and loaded, null otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static String loadString(Context context, String name) {
        Object obj = load(context, name);
        if ((obj != null) && (obj instanceof String)) {
            return (String)obj;
        }
        return null;
    }

    /**
     * Loads (unarchives) the specified integer object from the local (protected) file system.
     *
     * @param context
     * 		The application context used for directory/file access.  This parameter cannot be null.
     *
     * @param name
     * 		The name of the integer to be loaded from disk.  This parameter cannot be null.
     *
     * @return
     * 		The integer if found and loaded, null otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static Integer loadInteger(Context context, String name) {
        Object obj = load(context, name);
        if ((obj != null) && (obj instanceof Integer)) {
            return (Integer)obj;
        }
        return null;
    }

    /**
     * Loads (unarchives) the specified boolean object from the local (protected) file system.
     *
     * @param context
     * 		The application context used for directory/file access.  This parameter cannot be null.
     *
     * @param name
     * 		The name of the boolean to be loaded from disk.  This parameter cannot be null.
     *
     * @return
     * 		The boolean if found and loaded, null otherwise.
     *
     * @throws
     * 		IllegalArgumentException if the context or name parameters are null.
     */
    public static Boolean loadBoolean(Context context, String name) {
        Object obj = load(context, name);
        if ((obj != null) && (obj instanceof Boolean)) {
            return (Boolean)obj;
        }
        return null;
    }

    /*
     * Handles saving of the named dictionary to disk.
     */
    private static boolean saveObject(Context context, String fileName, Object object) {
        boolean retval = false;
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(IOUtils.objectToBytes(object));
            retval = true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "[saveFile] fnfe", e);
        } catch (IOException e) {
            Log.e(TAG, "[saveFile] ioe", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ignore) {
            }
        }
        return retval;
    }

    /*
     * Loads the named dictionary from disk.
     */
    private static Object loadObject(Context context, String fileName) {
        Object value = null;
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(fileName);
            byte[] bytes = IOUtils.readFromStream(fis);
            value = IOUtils.bytesToObject(bytes);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "[loadFile] fnfe", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
        return value;
    }

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

	/**
	 * Private default constructor, no instance.
	 */
    private Archiver() {
    }
}
