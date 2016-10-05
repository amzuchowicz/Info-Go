package com.sdstf.info_go;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sdstf.info_go.dummy.DummyContent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.LocationListener;
import java.util.ArrayList;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class TrackingDetailFragment extends Fragment implements ConnectionCallbacks, LocationListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    private static View rootView;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment myMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private PolylineOptions po;
    private Polyline polyline;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<LatLng> points;

    private boolean tracking;

    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackingDetailFragment() {

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
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mBroadcastReceiver, new IntentFilter("com.sdstf.info_go.BROADCAST_ACTION"));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void setUpMap() {
        // Try to obtain the map
        // note that because this class itself is a fragment,
        // to find the map fragment inside this fragment,
        // though it has unique id (i.e., R.id.map), you need to use the child's fragment manager.
        // Using getFragmentManager() instead of getChildFragmentManager() below will not work
        myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.TrackingMap);


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
        rootView = inflater.inflate(R.layout.fragment_tracking_detail, container, false);

        // Show the dummy content as text in a TextView.
     //   if (mItem != null) {
       //     ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.content);
       // }
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(ActivityRecognition.API)
                    .build();
        }

        tracking = false;
        final Button btnRecordLocation = (Button) rootView.findViewById(R.id.btnRecordLocation);
        btnRecordLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tracking) {
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(5000); // polling interval
                    mLocationRequest.setFastestInterval(2500); // if another app requests faster then this is fastest rate
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setSmallestDisplacement(1); // default 0. smallest distance in m user must move between loc updates

                    try {
                        LocationServices.FusedLocationApi
                                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, TrackingDetailFragment.this);
                    } catch (SecurityException se) {
                        System.out.println("Please grant permission for location services!");
                    }

                    ActivityRecognition.ActivityRecognitionApi
                            .requestActivityUpdates(mGoogleApiClient, 0, getActivityDetectionPendingIntent());

                    points = new ArrayList<>();

                    btnRecordLocation.setText("Stop Tracking");
                    tracking = true;
                }
                else {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, TrackingDetailFragment.this);
                    btnRecordLocation.setText("Start Tracking");
                    tracking = false;
                }
            }
        });

        return rootView;
    }

    /*
    @Override
    public void onDestroyView() {
        super.onDestroy();
        // Map fragment needs to be manually destroyed here for pre 5.0.
        // Otherwise causes duplicate fragment to be recreated with same id and crashes.
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.TrackingMap);
        getFragmentManager().beginTransaction().remove(mf).commit();
    }
    */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLastLocation != null) {
            System.out.println(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
        Toast.makeText(getActivity(), "Location Update", Toast.LENGTH_SHORT).show();

        if(points.size() == 1) {
            // First point. Need to init line with first point.
            po = new PolylineOptions();
            po.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        else if(points.size() == 2) {
            // Second point. A line can be added to map.
            po.add(new LatLng(location.getLatitude(), location.getLongitude()));
            polyline = mMap.addPolyline(po);
        }
        else {
            polyline.setPoints(points);
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(getActivity(), DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> updatedActivities = intent.getParcelableArrayListExtra("com.sdstf.info_go.ACTIVITY_EXTRA");
            for (DetectedActivity da : updatedActivities) {
                System.out.println(getActivityString(da.getType()) + " " + da.getConfidence());
            }
        }
    }

    public static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unidentifiable Activity!";
        }
    }
}
