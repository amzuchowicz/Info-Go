package com.sdstf.info_go;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.sdstf.info_go.dummy.DummyContent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.common.api.GoogleApiClient.*;

import java.util.ArrayList;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class LocationDetailFragment extends Fragment implements ConnectionCallbacks {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment myMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private ArrayList<Marker> markers = new ArrayList<>();

    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationDetailFragment() {

    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map
            // note that because this class itself is a fragment,
            // to find the map fragment inside this fragment,
            // though it has unique id (i.e., R.id.map), you need to use the child's fragment manager.
            // Using getFragmentManager() instead of getChildFragmentManager() below will not work
            if (getFragmentManager().findFragmentById(R.id.map) != null) {
                // Our map fragment is inside an activity
                //myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            } else {
                // Our map fragment is inside a parent fragment (tablet)
                myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            }

            myMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    try {
                        mMap.setMyLocationEnabled(true); // Enable the map's location dot
                    }
                    catch (SecurityException se) {
                        System.out.println("Please grant permission for location services!");
                    }
                }
            });
        }
    }

    public void addMarker(Location newMarkerLocation) {
        if(mMap != null) {
            // Map has loaded and we can add marker
            MarkerOptions mo = new MarkerOptions();
            LatLng loc = new LatLng(newMarkerLocation.getLatitude(), newMarkerLocation.getLongitude());
            mo.position(loc);
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mo.title("Last Recorded Location");

            Marker newMarker = mMap.addMarker(mo);
            markers.add(newMarker);
            updateOlderMarker();

            // move focus to where the marker is
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17)); // 10 is zoom level
        }
    }

    public void updateOlderMarker() {
        if(markers.size() > 1) {
            // If there is an older marker update it so it isn't the "last recorded location"
            Marker updateMarker = markers.get(markers.size() - 2);
            updateMarker.setTitle("A Recorded Location");
            updateMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       //if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
        //   mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
       //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);

        // Show the dummy content as text in a TextView.
     //   if (mItem != null) {
       //     ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.content);
       // }

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Button btnRecordLocation = (Button) rootView.findViewById(R.id.btnRecordLocation);
        btnRecordLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }

                if(mLastLocation != null) {
                    // We have connected as last location was retrieved
                    System.out.println(mLastLocation);
                    addMarker(mLastLocation);
                }
                else {
                    Toast.makeText(getActivity(), "Unable to retrieve last known location!", Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLastLocation != null) {
            System.out.println(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
