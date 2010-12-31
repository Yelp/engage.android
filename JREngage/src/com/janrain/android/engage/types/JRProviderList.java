package com.janrain.android.engage.types;

import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.utils.Archiver;

import java.util.ArrayList;

/**
 * List of JRProvider objects.  This class is provided for simplicity and readability, especially
 * in the case of storing lists of providers within a JRDictionary.
 */
public class JRProviderList extends ArrayList<JRProvider> {

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    /**
     * Checks to see if the specified list is null or empty.
     *
     * @param list
     *      The list instance to be checked.
     *
     * @return
     *      <code>true</code> if the list is null or empty, <code>false</code> otherwise.
     */
    public static boolean isEmpty(JRProviderList list) {
        return ((list == null) || (list.size() == 0));
    }

    /**
     * Saves (archives) the provider list to local disk using the specified name.
     *
     * @param name
     *      The name of the list of providers to be saved.
     *
     * @param list
     *      The list of providers to be saved.
     *
     * @return
     *      <code>true</code> if the list is saved, <code>false</code> otherwise.
     */
    public static boolean archive(String name, JRProviderList list) {
        return Archiver.save(name, list);
    }

    /**
     * Loads (unarchives) the specified provider list from disk.
     *
     * @param name
     *      The name of the list of providers to be loaded.
     *
     * @return
     *      The contents of the archived list if found, null otherwise.
     */
    public static JRProviderList unarchive(String name) {
        Object obj = Archiver.load(name);
        if ((obj != null) && (obj instanceof JRProviderList)) {
            return (JRProviderList)obj;
        }
        return new JRProviderList();
    }


    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Creates a new empty list of JRProvider objects.
     */
    public JRProviderList() {
        super();
    }

    /**
     * Creates a new empty list of JRProvider objects with the specified initial capacity.
     */
    public JRProviderList(int capacity) {
        super(capacity);
    }

    /**
     * Creates a new list with the contents of the specified list (base type) cloned.
     */
    public JRProviderList(ArrayList<JRProvider> list) {
        super();
        if (list != null) {
            addAll(list);
        }
    }

    /**
     * Creates a new list with the contents of the specified list cloned.
     */
    public JRProviderList(JRProviderList providerList) {
        super();
        if (!isEmpty(providerList)) {
            addAll(providerList);
        }
    }
}
