package com.janrain.android.engage.ui;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;

import java.util.ArrayList;

/**
 * TODO:  Javadoc
 */
public class JRProvidersActivity extends ListActivity {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    private class ProviderAdapter extends ArrayAdapter<JRProvider> {

        private ArrayList<JRProvider> mItems;

        public ProviderAdapter(Context context, int resId, ArrayList<JRProvider> items) {
            super(context, resId, items);
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.provider_listview_row, null);
            }

            JRProvider provider = mItems.get(position);
            if (provider != null) {
                ImageView icon = (ImageView)v.findViewById(R.id.icon);
                if (icon != null) {
                    icon.setImageResource(providerNameToIconResource(provider.getName()));
                }

                TextView label = (TextView)v.findViewById(R.id.label);
                if (label != null) {
                    label.setText(provider.getFriendlyName());
                }
            }

            return v;
        }

        private int providerNameToIconResource(String providerName) {
            if ("aol".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_aol_30x30;
            } else if ("blogger".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_blogger_30x30;
            } else if ("facebook".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_facebook_30x30;
            } else if ("flickr".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_flickr_30x30;
            } else if ("google".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_google_30x30;
            } else if ("hyves".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_hyves_30x30;
            } else if ("linkedin".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_linkedin_30x30;
            } else if ("live".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_live_id_30x30;
            } else if ("livejournal".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_livejournal_30x30;
            } else if ("myopenid".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_myopenid_30x30;
            } else if ("myspace".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_myspace_30x30;
            } else if ("netlog".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_netlog_30x30;
            } else if ("openid".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_openid_30x30;
            } else if ("paypal".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_paypal_30x30;
            } else if ("twitter".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_twitter_30x30;
            } else if ("verisign".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_verisign_30x30;
            } else if ("wordpress".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_wordpress_30x30;
            } else if ("yahoo".equalsIgnoreCase(providerName)) {
                return R.drawable.icon_yahoo_30x30;
            }
            return R.drawable.icon_unknown;
        }

    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private ArrayList<String> mProviderList;
    private ProviderAdapter mAdapter;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public JRProvidersActivity() {
        JRSessionData sessionData = JRSessionData.getInstance();
        if (sessionData != null) {
            mProviderList = sessionData.getBasicProviders();
        }
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_listview);
    }
}