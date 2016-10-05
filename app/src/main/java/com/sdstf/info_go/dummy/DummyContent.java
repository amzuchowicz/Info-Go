package com.sdstf.info_go.dummy;

import android.app.Fragment;
import android.os.Bundle;

import com.sdstf.info_go.GeofenceDetailFragment;
import com.sdstf.info_go.ItemDetailFragment;
import com.sdstf.info_go.LocationDetailFragment;
import com.sdstf.info_go.PlaceDetailFragment;
import com.sdstf.info_go.TrackingDetailFragment;

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
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        // Add 3 sample items.
        addItem(new DummyItem("1", "Record & View Locations", new LocationDetailFragment()));
        addItem(new DummyItem("2", "View Place Information", new PlaceDetailFragment()));
        addItem(new DummyItem("3", "Geofencing Around Hot Spots", new GeofenceDetailFragment()));
        addItem(new DummyItem("10", "Activity Recognition", new TrackingDetailFragment()));
        addItem(new DummyItem("4", "Item 4", new ItemDetailFragment()));
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;
        public Fragment fragment;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public DummyItem(String id, String content, Fragment fragment) {
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
