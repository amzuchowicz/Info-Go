package com.sdstf.info_go;

import android.*;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.sdstf.info_go.dummy.DummyContent;
import com.sdstf.info_go.dummy.WifiContent;

public class WifiListActivity extends AppCompatActivity implements WifiListFragment.WifiCallbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((WifiListFragment) getFragmentManager().findFragmentById(R.id.wifi_list)).setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            WifiContent.WifiItem mItem = WifiContent.ITEM_MAP.get(id);
            Fragment fragment = mItem.getFragment();

            getFragmentManager().beginTransaction().replace(R.id.item_detail_container, fragment).commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("item_id", id);
            startActivity(intent);
        }
    }
}
