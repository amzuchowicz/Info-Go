package com.sdstf.info_go;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.sdstf.info_go.content.ListContent;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    private ListContent.ListItem mItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        String id = getIntent().getStringExtra("id");
        mItem = ListContent.ITEM_MAP.get(id);

        if(savedInstanceState == null) {
            Fragment fragment = mItem.getFragment();

            //Bundle arguments = new Bundle();
            //arguments.putString(LocationDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));

            //fragment.setArguments(arguments);
            getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();
        }

        setTitle(mItem.toString());
    }
}
