package com.sdstf.info_go;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.places.Place;
import com.sdstf.info_go.dummy.DummyContent;

import static com.sdstf.info_go.ItemDetailFragment.ARG_ITEM_ID;

public class DetailActivity extends AppCompatActivity {

    private DummyContent.DummyItem mItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        String id = getIntent().getStringExtra("id");
        mItem = DummyContent.ITEM_MAP.get(id);

        if(savedInstanceState == null) {
            Fragment fragment = mItem.getFragment();

            //Bundle arguments = new Bundle();
            //arguments.putString(LocationDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));

            //fragment.setArguments(arguments);
            getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();
        }

        setTitle(mItem.toString());

        /*
        switch (id) {
            case "1": { // for option item 2, use a map fragment
                // In single-pane mode, simply start the detail activity
                // for the selected item ID.
                Bundle arguments = new Bundle();
                arguments.putString(LocationDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));

                LocationDetailFragment fragment = new LocationDetailFragment();
                fragment.setArguments(arguments);

                getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();

                setTitle(mItem.toString());
                break;
            }
            case "2": {
                Bundle arguments = new Bundle();
                arguments.putString(PlaceDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(PlaceDetailFragment.ARG_ITEM_ID));

                PlaceDetailFragment fragment = new PlaceDetailFragment();
                fragment.setArguments(arguments);

                getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();

                setTitle(mItem.toString());
                break;
            }
            case "3": {
                Bundle arguments = new Bundle();
                arguments.putString(GeofenceDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(GeofenceDetailFragment.ARG_ITEM_ID));

                GeofenceDetailFragment fragment = new GeofenceDetailFragment();
                fragment.setArguments(arguments);

                getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();

                setTitle(mItem.toString());
                break;
            }
            case "10": {
                Bundle arguments = new Bundle();
                arguments.putString(TrackingDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(TrackingDetailFragment.ARG_ITEM_ID));

                TrackingDetailFragment fragment = new TrackingDetailFragment();
                fragment.setArguments(arguments);

                getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();

                setTitle(mItem.toString());
                break;
            }
            default: { //options other than 1
                Bundle arguments = new Bundle();
                arguments.putString(ItemDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));

                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);

                getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();

                setTitle(mItem.toString());
                break;
            }
        }
        */
    }
}