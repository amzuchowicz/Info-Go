package com.sdstf.info_go.content;

import android.app.Fragment;
import android.os.Bundle;

import com.sdstf.info_go.WifiDetailFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<WifiContent.WifiItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, WifiContent.WifiItem> ITEM_MAP = new HashMap<>();

    static {
        // Add 3 sample items.
        addItem(new WifiContent.WifiItem("1", "Scan 1", new WifiDetailFragment()));

    }

    private static void addItem(WifiContent.WifiItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class WifiItem {
        public String id;
        public String content;
        public Fragment fragment;

        public WifiItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public WifiItem(String id, String content, Fragment fragment) {
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
