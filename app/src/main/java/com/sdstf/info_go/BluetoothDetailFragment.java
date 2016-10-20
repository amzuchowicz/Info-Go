package com.sdstf.info_go;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
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
import com.google.android.gms.location.LocationServices;
import com.sdstf.info_go.content.WifiContent;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.sdstf.info_go.helpers.DBBluetoothHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BluetoothDetailFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener{

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

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> results = new ArrayList<>();
    private int count = 0;
    private String currentScan = "";
    private DBBluetoothHelper bluetoothdb;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Button btnBluetoothScan;

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                currentScan += "Device name: " + device.getName() + ", address:" + device.getAddress() + ", class:"  + device.getBluetoothClass() + "\n";


            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getActivity(), "Started scan", Toast.LENGTH_SHORT).show();
                btnBluetoothScan.setClickable(false);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getActivity(), "Finished scan", Toast.LENGTH_SHORT).show();
                btnBluetoothScan.setClickable(true);

                try {mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);}
                catch (SecurityException se){}
                double latitude =  mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                //timestamp
                Date anotherCurDate = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm a");
                String timestamp = formatter.format(anotherCurDate);
                if(currentScan != "") {
                    title.add("Scan "+count);
                    bluetoothdb.insertBluetooth("Scan " + count, currentScan, latitude, longitude, timestamp);
                    currentScan = "Latitude: " + latitude
                            + ", Longitude: " + longitude
                            + ", TimeStamp: " + timestamp
                            + ", \nResults: \n" + currentScan;
                    results.add(currentScan);
                    count++;
                    currentScan = "";
                }
                else{
                    System.out.println("No results");
                }


                adapter.notifyDataSetChanged();
            }
        }
    };

    public BluetoothDetailFragment() {
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(bReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth_detail, container, false);
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
        bluetoothdb = new DBBluetoothHelper(getActivity());
        final ListView list = (ListView) rootView.findViewById(R.id.bluetooth_list);
        //bluetoothdb.deleteBluetooth(1);


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
        //BluetoothLeScanner.startScan();
        btnBluetoothScan = (Button) rootView.findViewById(R.id.btnBluetoothScan);
        btnBluetoothScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ask to enable bluetooth if it isn't already on.
                if(mBluetoothAdapter!= null) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 0);
                    }

                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                        //System.out.println("b");
                    }
                    mBluetoothAdapter.startDiscovery();
                }
                else{
                    System.out.println("null");
                }
            }
        });

        return rootView;
    }
    public void getData(){
        int numberOfRows = bluetoothdb.numberOfRows();
        //System.out.println(numberOfRows);
        Cursor res =  bluetoothdb.getAll();
        res.moveToFirst();
        while(numberOfRows > count){
            title.add(res.getString(res.getColumnIndex("title")));
            //System.out.println(" "+  res.getString(res.getColumnIndex("results")));
            results.add("Latitude: " + res.getString(res.getColumnIndex("latitude"))
                    + ", Longitude: " + res.getString(res.getColumnIndex("longitude"))
                    + ", TimeStamp: " + res.getString(res.getColumnIndex("timestamp"))
                    + ",\nResults: \n" + res.getString(res.getColumnIndex("results")));
            count++;
            res.moveToNext();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(bReceiver);
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
