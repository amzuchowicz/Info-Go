package com.sdstf.info_go;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends AppCompatActivity implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, 1);
        }
        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            if (id.equals("1")) { // for option item 2, use a map fragment
                LocationDetailFragment fragmentWithMap = new LocationDetailFragment();
               // mapfragment.setArguments(arguments); // not used in this example
                getFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragmentWithMap)
                        .commit();
            }
            else if(id.equals("2")) {
                PlaceDetailFragment placeFragment = new PlaceDetailFragment();
                getFragmentManager().beginTransaction().replace(R.id.item_detail_container, placeFragment).commit();
            }
            else if(id.equals("3")) {
                GeofenceDetailFragment geofenceFragment = new GeofenceDetailFragment();
                getFragmentManager().beginTransaction().replace(R.id.item_detail_container, geofenceFragment).commit();
            }
            else { // options other than 1
                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            }

        } else {
            if (id.equals("1")) { // for option item 2, use a map fragment
                // In single-pane mode, simply start the detail activity
                // for the selected item ID.
                Intent mapDetailIntent = new Intent(this, LocationDetailActivity.class);
                mapDetailIntent.putExtra(LocationDetailFragment.ARG_ITEM_ID, id);
                startActivity(mapDetailIntent);
            }
            else if (id.equals("2")) {
                Intent placeDetailIntent = new Intent(this, PlaceDetailActivity.class);
                placeDetailIntent.putExtra(LocationDetailFragment.ARG_ITEM_ID, id);
                startActivity(placeDetailIntent);
            }
            else if (id.equals("3")) {
                Intent geofenceDetailIntent = new Intent(this, GeofenceDetailActivity.class);
                geofenceDetailIntent.putExtra(LocationDetailFragment.ARG_ITEM_ID, id);
                startActivity(geofenceDetailIntent);
            }
            else { //options other than 1
                Intent detailIntent = new Intent(this, ItemDetailActivity.class);
                detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
                startActivity(detailIntent);
            }
        }
    }
}
