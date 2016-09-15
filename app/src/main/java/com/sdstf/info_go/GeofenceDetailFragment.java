package com.sdstf.info_go;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.sdstf.info_go.dummy.DummyContent;

import java.util.ArrayList;

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

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;

    public GeofenceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_geofence_detail, container, false);

        mGeofencePendingIntent = null;

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addConnectionCallbacks(this)
                //.addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
        }

        mGeofenceList = new ArrayList<>();
        mGeofenceList.add(new Geofence.Builder()
            .setRequestId("1") // set the request ID of the geofence. This is a string to identify this geofence.
            //.setCircularRegion(-37.635991, 144.930133, 100) // lat, long, radius in meters
                .setCircularRegion(-37.636704, 144.930706, 100) // lat, long, radius in meters
            .setExpirationDuration(1000) // in milliseconds
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build());



        return rootView;
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
            System.out.println("yay");
        }
        else {
            System.out.println("no");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("yo");
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent())
                    .setResultCallback(this);
        }
        catch (SecurityException se) {
            System.out.println("Please grant permission for location services!");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}