package com.sdstf.info_go;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.sdstf.info_go.content.ListContent;
import com.sdstf.info_go.helpers.DBPlaceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class PlaceDetailFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ListContent.ListItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private ArrayList<String> mPlaceArray = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private DBPlaceHelper placedb;

    int index = 0;
    public PlaceDetailFragment() {
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates.
    }
    @Override
    public void onResume() {
        super.onResume();
        // Resuming the periodic location updates.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if (getArguments().containsKey(ARG_ITEM_ID)) {
        // Load the dummy content specified by the fragment
        // arguments. In a real-world scenario, use a Loader
        // to load content from a content provider.
        //mItem = ListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
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

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.PLACE_DETECTION_API )
                    .addApi(LocationServices.API)
                    .build();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            catch (SecurityException se){

            }
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
                catch (SecurityException se){

                }
                boolean savedPlace = false;
                if (mLastLocation != null) {
                    double latitude =  mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();

                    for(int i = 0; i < numberOfRows; i++){
                        //TO DO: check if there is a close element in array
                        Cursor res = placedb.getData(i);
                        res.moveToFirst();
                        double storedlat = res.getDouble(res.getColumnIndex("latitude"));
                        double storedlong = res.getDouble(res.getColumnIndex("longitude"));
                        float distance = distance(latitude,longitude,storedlat,storedlong);
                        System.out.println(latitude + " " + longitude + " " + storedlat+" " + storedlong + " " + distance);
                        if(distance < 10) {
                            savedPlace = true;
                            index = i;
                        }
                    }
                }
                else{
                    System.out.println("Location is null!");
                }
                //System.out.println(""+savedPlace);
                if(savedPlace) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Do you want to load from cache?");

                    alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            getPlace(index);
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
                                        try {
                                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                        }
                                        catch (SecurityException se){}

                                        savePlace(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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
                    //System.out.print("new");
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
                                try {
                                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                }
                                catch (SecurityException se){

                                }
                                savePlace(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            }
                        });
                    } else {
                        System.out.println("Failed to get place info!");
                    }
                }
            }
        });

        //btnPlaceInfo.callOnClick();

        return rootView;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(getActivity(), "Failed to connect...", Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onConnected(Bundle arg0) {
        //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //Toast.makeText(getActivity(), "Connected to Google Play Services.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(getActivity(), "Connection suspended...", Toast.LENGTH_SHORT).show();

    }
    //helper methods
    public void savePlace(double lat, double lon){
        placedb.insertPlace(lat,lon,convertListToString(mPlaceArray));
    }
    public void getPlace(int id){
        Cursor res = placedb.getData(id);
        res.moveToFirst();
        mPlaceArray.clear();
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
        //stringBuffer.delete(lastIndex, lastIndex + LIST_SEPARATOR.length());

        return stringBuffer.toString();
    }

    public static List<String> convertStringToList(String str) {
        return Arrays.asList(str.split(LIST_SEPARATOR));
    }
    public float distance(double lat1, double lng1, double lat2,double lng2) {
        Location loc1 = new Location("new");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("new");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float dist = loc1.distanceTo(loc2);
        return dist;
    }

}
