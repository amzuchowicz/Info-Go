package com.sdstf.info_go.content;

import android.app.Fragment;
import android.os.Bundle;

import com.sdstf.info_go.BluetoothDetailFragment;
import com.sdstf.info_go.LocationDetailFragment;
import com.sdstf.info_go.PictureDetailFragment;
import com.sdstf.info_go.PlaceDetailFragment;
import com.sdstf.info_go.TrackingDetailFragment;
import com.sdstf.info_go.WifiDetailFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ListContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<ListItem> ITEMS = new ArrayList<ListItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, ListItem> ITEM_MAP = new HashMap<String, ListItem>();

    static {
        // Add 3 sample items.
        addItem(new ListItem("1", "Record & View Locations", new LocationDetailFragment()));
        addItem(new ListItem("2", "View Place Information", new PlaceDetailFragment()));
        //addItem(new ListItem("3", "Geofencing Around Hot Spots", new GeofenceDetailFragment()));
        addItem(new ListItem("5", "Record and View WiFi Scans", new WifiDetailFragment()));
        addItem(new ListItem("6", "Record and View Bluetooth Scans", new BluetoothDetailFragment()));
        addItem(new ListItem("7", "Take and View Pictures", new PictureDetailFragment()));
        addItem(new ListItem("10", "Activity Recognition", new TrackingDetailFragment()));
    }

    private static void addItem(ListItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class ListItem {
        public String id;
        public String content;
        public Fragment fragment;

        public ListItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public ListItem(String id, String content, Fragment fragment) {
            this.id = id;
            this.content = content;
            this.fragment = fragment;

            Bundle arguments = new Bundle();
            arguments.putString("item_id", id);
            this.fragment.setArguments(arguments); // can only be done once
        }

        public Fragment getFragment() {
           return fragment;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
