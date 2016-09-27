package com.sdstf.info_go;

import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.sdstf.info_go.dummy.DummyContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class PlaceDetailFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
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

        final TextView tvPlaceInfo = (TextView) rootView.findViewById(R.id.tvPlaceInfo);
        final TextView tcGPS = (TextView) rootView.findViewById(R.id.tvGPS);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getActivity())
                    //.addConnectionCallbacks(this)
                    //.addApi(LocationServices.API)
                    //.addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    //.enableAutoManage(getActivity(), this)
                    .build();
        }
        Button btnPlaceInfo = (Button) rootView.findViewById(R.id.btnPlaceInfo);
        btnPlaceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingResult<PlaceLikelihoodBuffer> currentPlace = null;
                try {
                    currentPlace = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                }
                catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }

                if (currentPlace != null) {
                    currentPlace.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                            String placeInfo = "";
                            for(PlaceLikelihood placeLikelihood : placeLikelihoods) {
                                //System.out.println(placeLikelihood.getPlace().getName());
                                placeInfo = placeInfo + "> " + placeLikelihood.getPlace().getName() + "\n";
                            }
                            placeInfo = placeInfo.substring(0, placeInfo.length() - 1); // remove last \n
                            tvPlaceInfo.setText(placeInfo);
                            placeLikelihoods.release();
                        }
                    });
                }
                else {
                    System.out.println("Failed to get place info!");
                }
            }
        });
        return rootView;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
