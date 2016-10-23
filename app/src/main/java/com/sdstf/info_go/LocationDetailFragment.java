package com.sdstf.info_go;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
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
import com.sdstf.info_go.content.ListContent;
import com.sdstf.info_go.helpers.DBLocationHelper;
import com.sdstf.info_go.services.GeofenceTransitionsIntentService;

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
    //private static View rootView;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment myMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private ArrayList<Marker> markers = new ArrayList<>();
    private Button btnRecordLocation;
    private boolean addGeofence = false;
    private Marker selectedMarker;

    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;
    private int mIdCount = 0;

    private DBLocationHelper locationDB;

    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ListContent.ListItem mItem;

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
        setUpMap();
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        //if (mMap == null) {
        // Try to obtain the map
        // note that because this class itself is a fragment,
        // to find the map fragment inside this fragment,
        // though it has unique id (i.e., R.id.map), you need to use the child's fragment manager.
        // Using getFragmentManager() instead of getChildFragmentManager() below will not work
        if (getFragmentManager().findFragmentById(R.id.LocationMap) != null) {
            // Our map fragment is inside an activity (pre 5.0)
            //myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.LocationMap);
        } else {
            // Our map fragment is inside a parent fragment (tablet) (post 5.0)
            myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.LocationMap);
        }

        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                try {
                    mMap.setMyLocationEnabled(true); // Enable the map's location dot
                } catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }

                //LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());

                int numberOfRows = locationDB.numberOfRows();
                if(numberOfRows > 0) {
                    Cursor res = locationDB.getAll();
                    res.moveToFirst();
                    while (numberOfRows > 0) {
                        Location loc = new Location("");
                        loc.setLatitude(res.getDouble(res.getColumnIndex("latitude")));
                        loc.setLongitude(res.getDouble(res.getColumnIndex("longitude")));
                        System.out.println(res.getDouble(res.getColumnIndex("latitude")) + " "+ res.getDouble(res.getColumnIndex("longitude")));
                        addMarker(loc);
                        if(res.getString(res.getColumnIndex("geofence")).equals("true")) {

                            CircleOptions co = new CircleOptions();
                            co.fillColor(Color.parseColor("#3F536DFE"));
                            co.strokeColor(Color.parseColor("#FF536DFE"));
                            co.strokeWidth(5);
                            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                            co.center(latLng);
                            co.radius(100);
                            Circle circle = mMap.addCircle(co);
                        }
                        res.moveToNext();
                        numberOfRows--;
                    }
                }
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        selectedMarker = marker;
                        addGeofence = true;
                        btnRecordLocation.setText("Mark Location as Hot");
                        return false;
                    }
                });
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        addGeofence = false;
                        btnRecordLocation.setText("Record Current Location");
                    }
                });
            }
        });
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
            int oldMarkerIndex = markers.size() - 2;
            Marker updateMarker = markers.get(oldMarkerIndex);
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
        //   mItem = ListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
       //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);

        locationDB = new DBLocationHelper(getActivity());
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        btnRecordLocation = (Button) rootView.findViewById(R.id.btnRecordLocation);
        btnRecordLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!addGeofence) {
                    try {
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    } catch (SecurityException se) {
                        System.out.println("Please grant permission for location services!");
                    }

                    if (mLastLocation != null) {
                        // We have connected as last location was retrieved
                        System.out.println(mLastLocation);
                        locationDB.insertLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), true, "false");
                        addMarker(mLastLocation);
                    } else {
                        Toast.makeText(getActivity(), "Unable to retrieve last known location!", Toast.LENGTH_LONG).show();
                    }
                } else {

                    int id = markers.indexOf(selectedMarker) + 1;
                    Cursor res = locationDB.getData(id);
                    res.moveToFirst();
                    String result = res.getString(res.getColumnIndex("geofence"));
                    System.out.println("id = " + id);
                    System.out.println("result = " + result);

                    if(result.equals("false")) {
                        mGeofenceList.add(new Geofence.Builder()
                                .setRequestId(mIdCount + "") // set the request ID of the geofence. This is a string to identify this geofence.
                                .setCircularRegion(selectedMarker.getPosition().latitude, selectedMarker.getPosition().longitude, 100) // lat, long, radius in meters
                                .setExpirationDuration(Geofence.NEVER_EXPIRE) // in milliseconds
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .build());
                        mIdCount++;
                        addGeofences();

                        CircleOptions co = new CircleOptions();
                        co.fillColor(Color.parseColor("#3F536DFE"));
                        co.strokeColor(Color.parseColor("#FF536DFE"));
                        co.strokeWidth(5);
                        co.center(selectedMarker.getPosition());
                        co.radius(100);
                        Circle circle = mMap.addCircle(co);

                        locationDB.updateLocation(id, "true");
                    }

                }
            }
        });
        return rootView;
    }

    public void addGeofences() {
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent());
        }
        catch (SecurityException se) {
            System.out.println("Please grant permission for location services!");
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
    public void onConnected(@Nullable Bundle bundle) {
        if (mLastLocation != null) {
            System.out.println(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
