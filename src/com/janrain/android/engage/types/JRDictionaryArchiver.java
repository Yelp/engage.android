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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.janrain.android.engage.utils.IOUtils;


/**
 * Utility class used to archive/unarchive JRDictionary objects.  This implementation serializes
 * the JRDictionary to a binary format and saves to the protected storage area (disk).
 */
public final class JRDictionaryArchiver {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

	// tag used for logging
	private static final String TAG = JRDictionaryArchiver.class.getSimpleName();
	// token used to separate parts of dictionary file name
	private static final String DICTIONARY_FILE_SEPARATOR = "~";
	// prefix + dictionary name
	private static final String DICTIONARY_BASE_FORMAT = "dict" + DICTIONARY_FILE_SEPARATOR + "%s";

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

	/**
	 * Saves (archives) the specified dictionary object to the local (protected) file system.
	 *  
	 * @param context
	 * 		The application context used for directory/file access.  This parameter cannot be null.
	 * 
	 * @param name
	 * 		The name the dictionary will be saved as on disk.  This parameter cannot be null.
	 * 
	 * @param dictionary
	 * 		The JRDictionary object to be saved.
	 *  
	 * @return
	 * 		Returns <code>true</code> if the save operation is successful, <code>false</code>
	 * 		otherwise.
	 * 
	 * @throws
	 * 		IllegalArgumentException if the context or name parameters are null.
	 */
	public static boolean save(Context context, String name, JRDictionary dictionary) {
		if (context == null) {
			throw new IllegalArgumentException("context parameter cannot be null");
		} else if (TextUtils.isEmpty(name)) {
			throw new IllegalArgumentException("name parameter cannot be null");
		}
		
		String fileName = String.format(DICTIONARY_BASE_FORMAT, name);
		
		// purge existing file
		context.deleteFile(fileName);
		
		// then save current dictionary contents
		return saveDictionary(context, fileName, dictionary);
	}
	
	/**
	 * Loads (unarchives) the specified dictionary object from the local (protected) file system.
	 *  
	 * @param context
	 * 		The application context used for directory/file access.  This parameter cannot be null.
	 * 
	 * @param name
	 * 		The name of the dictionary to be loaded from disk.  This parameter cannot be null.
	 * 
	 * @return
	 * 		The JRDictionary object if found and loaded, empty JRDictionary otherwise.
	 * 
	 * @throws
	 * 		IllegalArgumentException if the context or name parameters are null.
	 */
	public static JRDictionary load(Context context, String name) {
		if (context == null) {
			throw new IllegalArgumentException("context parameter cannot be null");
		} else if (TextUtils.isEmpty(name)) {
			throw new IllegalArgumentException("name parameter cannot be null");
		}
		
		JRDictionary dictionary = null;
		
		String fileName = String.format(DICTIONARY_BASE_FORMAT, name);

		// load the dictionary file
		Object obj = loadDictionary(context, fileName);
		if ((obj != null) && (obj instanceof JRDictionary)) {
			// and if it's a JRDictionary, we'll set our internal ref to it
			dictionary = (JRDictionary) obj;
		} else {
			// otherwise log a warning and set to empty
			Log.w(TAG, "[load] A file that should be a dictionary is not -> " + fileName);
			dictionary = new JRDictionary();
		}
		
		return dictionary;
	}
	
	/*
	 * Handles saving of the named dictionary to disk.
	 */
	private static boolean saveDictionary(Context context, String fileName, JRDictionary dict) {
		boolean retval = false;
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(IOUtils.objectToBytes(dict));
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
	private static Object loadDictionary(Context context, String fileName) {
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
	private JRDictionaryArchiver() {
	}

}
