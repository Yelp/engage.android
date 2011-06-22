package com.janrain.android.engage.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 6/22/11
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class JRProviderListActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            JRProviderListFragment providerList = new JRProviderListFragment();

            // This line is not necessary for us since we don't use Intent arguments?
            // It's from Dianne's example
            providerList.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(
                    android.R.id.content, providerList).commit();
        }
    }
}