package com.sdstf.info_go;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sdstf.info_go.dummy.DummyContent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailMapFragment extends Fragment
 {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment myMapFragment;

    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailMapFragment() {

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
             myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);

             myMapFragment.getMapAsync(new OnMapReadyCallback() {
                 @Override
                 public void onMapReady(GoogleMap googleMap) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       // if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
         //   mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_item_detail, container, false);

        // Show the dummy content as text in a TextView.
     //   if (mItem != null) {
       //     ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.content);
       // }

        return rootView;
    }

}
