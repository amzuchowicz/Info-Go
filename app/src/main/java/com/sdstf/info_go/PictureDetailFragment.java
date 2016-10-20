package com.sdstf.info_go;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdstf.info_go.content.ListContent;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class PictureDetailFragment extends Fragment implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * The dummy content this fragment is presenting.
     */
    private ListContent.ListItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private MapFragment myMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


    public PictureDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMap();
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        //if (mMap == null) {
        // Try to obtain the map
        // note that because this class itself is a fragment,
        // to find the map fragment inside this fragment,
        // though it has unique id (i.e., R.id.map), you need to use the child's fragment manager.
        // Using getFragmentManager() instead of getChildFragmentManager() below will not work
        if (getFragmentManager().findFragmentById(R.id.PictureMap) != null) {
            // Our map fragment is inside an activity (pre 5.0)
            //myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.LocationMap);
        } else {
            // Our map fragment is inside a parent fragment (tablet) (post 5.0)
            myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.PictureMap);
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
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // text desc
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            System.out.println(mGoogleApiClient.isConnected());
            addMarker(mLastLocation, imageBitmap);
        }
    }

    public void addMarker(Location location, Bitmap imageBitmap) {
        MarkerOptions mo = new MarkerOptions();
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        mo.position(loc);
        mo.icon(BitmapDescriptorFactory.fromBitmap(imageBitmap));
        mo.title("Last Recorded Location");

        Marker newMarker = mMap.addMarker(mo);

        // move focus to where the marker is
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17)); // 10 is zoom level
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_picture_detail, container, false);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch (SecurityException se) {
            System.out.println("Please grant permission for location services!");
        }

        Button btnTakePicture = (Button) rootView.findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
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
                }
                else {
                    System.out.println("Unable to retrieve last known location!");
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("FAIL");
    }
}
