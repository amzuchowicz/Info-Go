package com.sdstf.info_go;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.sdstf.info_go.dummy.DummyContent;
import com.sdstf.info_go.dummy.WifiContent;

import java.util.ArrayList;
import java.util.List;

public class WifiDetailFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {

        }
        final ListView list = (ListView) rootView.findViewById(R.id.wifi_list2);

        Button btnScanWifi = (Button) rootView.findViewById(R.id.btnScanWifi);
        btnScanWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title.add("Scan "+count);
                String res = "";
                ArrayAdapter<String> adapter;
                List<ScanResult> scanResults = wifi.getScanResults();
                for(ScanResult sr : scanResults) {
                    res += "BSSID: " + sr.BSSID + ", SSID: " +sr.SSID + ", Frequency: " + sr.frequency + "\n";
                    System.out.println(sr.BSSID);
                    System.out.println(sr.SSID);
                    System.out.println(sr.capabilities);
                    System.out.println(sr.frequency);
                    System.out.println(sr.level);
                    System.out.println(sr.channelWidth);
                    System.out.println(" ");
                    results.add(res);
                    count++;
                }
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, title);
                list.setAdapter(adapter);
                list.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        System.out.println(position);
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
            }
        });

        return rootView;
    }
}
