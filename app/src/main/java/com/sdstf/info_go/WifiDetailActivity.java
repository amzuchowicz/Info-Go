package com.sdstf.info_go;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.sdstf.info_go.dummy.DummyContent;
import com.sdstf.info_go.dummy.WifiContent;

public class WifiDetailActivity extends AppCompatActivity {

    private WifiContent.WifiItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_detail);

        String id = getIntent().getStringExtra("id");
        mItem = WifiContent.ITEM_MAP.get(id);

        if (savedInstanceState == null) {
            Fragment fragment = mItem.getFragment();

            //Bundle arguments = new Bundle();
            //arguments.putString(LocationDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));

            //fragment.setArguments(arguments);
            getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();
        }

        setTitle(mItem.toString());
    }
}

