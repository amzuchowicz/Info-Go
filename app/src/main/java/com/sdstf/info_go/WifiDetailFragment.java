package com.sdstf.info_go;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.sdstf.info_go.content.WifiContent;
import com.sdstf.info_go.helpers.DBWifiHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WifiDetailFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private WifiContent.WifiItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private WifiManager wifi;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> results = new ArrayList<>();
    private int count = 0;
    private DBWifiHelper wifidb;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public WifiDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = WifiContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

        wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_detail, container, false);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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
        final ListView list = (ListView) rootView.findViewById(R.id.wifi_list2);
        wifidb = new DBWifiHelper(getActivity());

        getData();

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, title);
        list.setAdapter(adapter);

        list.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                //System.out.println(results.get(position));

                alertDialogBuilder.setMessage(results.get(position));
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
        Button btnScanWifi = (Button) rootView.findViewById(R.id.btnScanWifi);
        btnScanWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title.add("Scan "+count);
                String res = "";
                List<ScanResult> scanResults = wifi.getScanResults();
                for(ScanResult sr : scanResults) {
                    res += "SSID: " + sr.SSID + ", BSSID: " +sr.BSSID + ", Level: " + sr.level + "dB \n";
                }
                //add to database
                try {mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);}
                catch (SecurityException se){}
                double latitude =  mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                //timestamp
                Date anotherCurDate = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm a");
                String timestamp = formatter.format(anotherCurDate);
                wifidb.insertWifi("Scan "+count, res, latitude, longitude, timestamp);
                //add to reslts
                results.add("Latitude: " + latitude
                        + ", Longitude: " + longitude
                        + ", TimeStamp: " + timestamp
                        + ", \nResults: \n" + res);
                count++;
                adapter.notifyDataSetChanged();

            }
        });

        return rootView;
    }
    public void getData(){
        int numberOfRows = wifidb.numberOfRows();
        //System.out.println(numberOfRows);
        Cursor res =  wifidb.getAll();
        res.moveToFirst();
        while(numberOfRows > count){
            title.add(res.getString(res.getColumnIndex("title")));
            results.add("Latitude: " + res.getString(res.getColumnIndex("latitude"))
                    + ", Longitude: " + res.getString(res.getColumnIndex("longitude"))
                    + ", TimeStamp: " + res.getString(res.getColumnIndex("timestamp"))
                    + ",\nResults: \n" + res.getString(res.getColumnIndex("results")));
            count++;
            res.moveToNext();
        }
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
}
