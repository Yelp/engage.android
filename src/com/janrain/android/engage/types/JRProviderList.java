package com.janrain.android.engage.types;

import android.content.Context;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.utils.Archiver;

import java.util.ArrayList;

/**
 */
public class JRProviderList extends ArrayList<JRProvider> {

    public static boolean isEmpty(JRProviderList list) {
        return ((list == null) || (list.size() == 0));
    }

    public static boolean archive(Context context, String name, JRProviderList list) {
        return Archiver.save(context, name, list);
    }

    public static JRProviderList unarchive(Context context, String name) {
        Object obj = Archiver.load(context, name);
        if ((obj != null) && (obj instanceof JRProviderList)) {
            return (JRProviderList)obj;
        }
        return new JRProviderList();
    }


    public JRProviderList() {
        super();
    }

    public JRProviderList(int capacity) {
        super(capacity);
    }

    public JRProviderList(JRProviderList providerList) {
        super();
        if (!isEmpty(providerList)) {
            addAll(providerList);
        }
    }
}
