package com.sdstf.info_go;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */
public class LocationDetailActivity extends AppCompatActivity   {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment myMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Show the Up button in the action bar.
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(LocationDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));
            LocationDetailFragment mapfragment = new LocationDetailFragment();
            mapfragment.setArguments(arguments);
            getFragmentManager().beginTransaction().add(R.id.item_detail_container, mapfragment).commit();
       }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map

            myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            myMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // Map is ready! Add stuff to it like markers.
                    mMap = googleMap;
                    addMyMarker(mMap);
                }
            });

        }
    }
    public void addMyMarker(GoogleMap map) {
        MarkerOptions mo = new MarkerOptions();
        LatLng loc = new LatLng(-37.0, 147.0);
        mo.position(loc);
        mo.title("Marker");
        map.addMarker(mo);

        // move focus to where the marker is
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10)); // 10 is zoom level
    }
}
