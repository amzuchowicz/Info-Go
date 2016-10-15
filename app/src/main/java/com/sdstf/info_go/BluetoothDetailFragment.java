package com.sdstf.info_go;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sdstf.info_go.dummy.WifiContent;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDetailFragment extends Fragment {

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
    private ArrayList<BluetoothDevice> mBluetoothDeviceList;
    private ArrayAdapter<BluetoothDevice> adapter;
    private ArrayAdapter adapter2;

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(!mBluetoothDeviceList.contains(device)) {
                    mBluetoothDeviceList.add(device);
                    adapter2.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getActivity(), "Started scan", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getActivity(), "Finished scan", Toast.LENGTH_SHORT).show();
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

        // Show the dummy content as text in a TextView.
        if (mItem != null) {

        }
        final ListView list = (ListView) rootView.findViewById(R.id.bluetooth_list);

        mBluetoothDeviceList = new ArrayList<>();

        //BluetoothLeScanner.startScan();
        Button btnBluetoothScan = (Button) rootView.findViewById(R.id.btnBluetoothScan);
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

                    adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mBluetoothDeviceList);

                    adapter2 = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, mBluetoothDeviceList) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView tv1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView tv2 = (TextView) view.findViewById(android.R.id.text2);

                            tv1.setText(mBluetoothDeviceList.get(position).getName());
                            tv2.setText(mBluetoothDeviceList.get(position).getAddress() + "\n" + mBluetoothDeviceList.get(position).getBluetoothClass().toString());
                            return view;
                        }
                    };

                    list.setAdapter(adapter2);
                }
                else{
                    System.out.println("null");
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(bReceiver);
    }
}
