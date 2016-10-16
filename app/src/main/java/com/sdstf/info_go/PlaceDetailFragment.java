package com.sdstf.info_go;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
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
import java.util.Arrays;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class PlaceDetailFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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
    private ArrayList<String> mPlaceArray = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private DBPlaceHelper placedb;
    private Location mLastLocation;

    public PlaceDetailFragment() {
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
    public void onConnected(Bundle connectionHint) {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_detail, container, false);

        // Show the dummy content as text in a TextView.
        //if (mItem != null) {
            //((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.content);
        //}
        //set up database
        placedb = new DBPlaceHelper(getActivity());

        final ListView list = (ListView) rootView.findViewById(R.id.list);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    //.addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    //.enableAutoManage(getActivity(), this)
                    .build();
        }

        Button btnPlaceInfo = (Button) rootView.findViewById(R.id.btnPlaceInfo);
        btnPlaceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfRows = placedb.numberOfRows();

                //TO DO: check if there is a saved location that is close enough
                try {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }
                boolean savedPlace = true;
                if (mLastLocation != null) {
                    double latitude =  mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();
                    for(int i = 0; i < numberOfRows; i++){
                        //TO DO: check if there is a close element in array
                        Cursor res = placedb.getData(i);
                        res.moveToFirst();
                        double storedlat = res.getDouble(res.getColumnIndex("latitude"));
                        double storedlong = res.getDouble(res.getColumnIndex("latitude"));
                        if(distance(latitude,longitude,storedlat,storedlong)< 0){
                            savedPlace = true;
                        }
                    }
                }
                System.out.println(""+savedPlace);
                if(savedPlace) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Do you want to load from cache?");

                    alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            getPlace(1);
                            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mPlaceArray);
                            list.setAdapter(adapter);
                        }
                    });

                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.print("new");
                            PendingResult<PlaceLikelihoodBuffer> currentPlace = null;
                            try {
                                currentPlace = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                            } catch (SecurityException se) {
                                System.out.println("Please grant permission for location services!");
                            }

                            if (currentPlace != null) {
                                currentPlace.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                                    @Override
                                    public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                                        mPlaceArray.clear();
                                        for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                                            //System.out.println(placeLikelihood.getPlace().getName());
                                            mPlaceArray.add(placeLikelihood.getPlace().getName() + "");
                                        }
                                        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mPlaceArray);
                                        list.setAdapter(adapter);

                                        placeLikelihoods.release();
                                        //TO DO: save location
                                       // savePlace(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                    }
                                });
                            } else {
                                System.out.println("Failed to get place info!");
                            }

                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else {
                    System.out.print("new");
                    PendingResult<PlaceLikelihoodBuffer> currentPlace = null;
                    try {
                        currentPlace = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                    } catch (SecurityException se) {
                        System.out.println("Please grant permission for location services!");
                    }

                    if (currentPlace != null) {
                        currentPlace.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                                mPlaceArray.clear();
                                for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                                    //System.out.println(placeLikelihood.getPlace().getName());
                                    mPlaceArray.add(placeLikelihood.getPlace().getName() + "");
                                }
                                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mPlaceArray);
                                list.setAdapter(adapter);

                                placeLikelihoods.release();
                               // savePlace(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            }
                        });
                    } else {
                        System.out.println("Failed to get place info!");
                    }
                }
            }
        });

        btnPlaceInfo.callOnClick();

        return rootView;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onConnectionSuspended(int i) {}
    //helper methods
    public void savePlace(double lat, double lon){
        placedb.insertPlace(lat,lon,convertListToString(mPlaceArray));
    }
    public void getPlace(int id){
        Cursor res = placedb.getData(id);
        res.moveToFirst();
        String placeArray = res.getString(res.getColumnIndex("placeArray"));
        List<String> array = convertStringToList(placeArray);
        for(String place: array){
            mPlaceArray.add(place);
        }
    }
    private static String LIST_SEPARATOR = "__,__";

    public static String convertListToString(List<String> stringList) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : stringList) {
            stringBuffer.append(str).append(LIST_SEPARATOR);
        }

        // Remove last separator
        int lastIndex = stringBuffer.lastIndexOf(LIST_SEPARATOR);
        stringBuffer.delete(lastIndex, lastIndex + LIST_SEPARATOR.length() + 1);

        return stringBuffer.toString();
    }

    public static List<String> convertStringToList(String str) {
        return Arrays.asList(str.split(LIST_SEPARATOR));
    }
    public double distance(double lat1, double lng1, double lat2,double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = (earthRadius * c);
        System.out.println(dist);
        return dist;
    }

}
