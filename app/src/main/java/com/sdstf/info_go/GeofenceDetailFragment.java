package com.sdstf.info_go;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdstf.info_go.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Map;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class GeofenceDetailFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, ResultCallback<Status> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private static View rootView;

    private GoogleApiClient mGoogleApiClient;

    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment myMapFragment;
    private Location mLastLocation;
    private int mIdCount = 0;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Circle> circles = new ArrayList<>();

    public GeofenceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        //}
    }

    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMap();
    }

    private void setUpMap() {
        // Try to obtain the map
        // note that because this class itself is a fragment,
        // to find the map fragment inside this fragment,
        // though it has unique id (i.e., R.id.map), you need to use the child's fragment manager.
        // Using getFragmentManager() instead of getChildFragmentManager() below will not work
        myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.GeofenceMap);

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

                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {}

                    @Override
                    public void onMarkerDrag(Marker marker) {
                        circles.get(markers.indexOf(marker)).setCenter(marker.getPosition());
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());

                        mGeofenceList.set(markers.indexOf(marker), new Geofence.Builder()
                                .setRequestId(markers.indexOf(marker) + "") // This is a string to identify a geofence.
                                .setCircularRegion(marker.getPosition().latitude, marker.getPosition().longitude, 100) // lat, long, radius in meters
                                .setExpirationDuration(Geofence.NEVER_EXPIRE) // in milliseconds
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .build());

                        addGeofences();
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_geofence_detail, container, false);

        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addConnectionCallbacks(this)
                //.addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
        }

        Button btnAddGeofence = (Button) rootView.findViewById(R.id.btnAddGeofence);
        btnAddGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }

                addMarker(mLastLocation);

                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(mIdCount + "") // set the request ID of the geofence. This is a string to identify this geofence.
                        .setCircularRegion(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 100) // lat, long, radius in meters
                        .setExpirationDuration(Geofence.NEVER_EXPIRE) // in milliseconds
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build());

                mIdCount++;
                addGeofences();

                Toast.makeText(getActivity(), "Tap and hold marker to change hot spot location!", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    /*
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.GeofenceMap);
        getFragmentManager().beginTransaction().remove(mf).commit();
    }
    */

    public void addGeofences() {
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent());
        }
        catch (SecurityException se) {
            System.out.println("Please grant permission for location services!");
        }
    }

    public void addMarker(Location newMarkerLocation) {
        if(mMap != null) {
            // Map has loaded and we can add marker
            MarkerOptions mo = new MarkerOptions();
            LatLng loc = new LatLng(newMarkerLocation.getLatitude(), newMarkerLocation.getLongitude());
            mo.position(loc);
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mo.title("Hot Spot");
            mo.draggable(true);

            Marker newMarker = mMap.addMarker(mo);
            markers.add(newMarker);

            CircleOptions co = new CircleOptions();
            co.fillColor(Color.parseColor("#3F536DFE"));
            co.strokeColor(Color.parseColor("#FF536DFE"));
            co.strokeWidth(5);
            co.center(loc);
            co.radius(100);
            Circle circle = mMap.addCircle(co);
            circles.add(circle);

            // move focus to where the marker is
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17)); // 10 is zoom level
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if(mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()) {
            System.out.println("Successfully added Geofence");
        }
        else {
            System.out.println("Error adding Geofence!");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("Connected to Google Play Services");
    }

    @Override
    public void onConnectionSuspended(int i) {}
}