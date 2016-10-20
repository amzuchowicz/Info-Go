package com.sdstf.info_go;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sdstf.info_go.content.ListContent;

public class DetailActivity extends AppCompatActivity {

    private ListContent.ListItem mItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        String id = getIntent().getStringExtra("id");
        mItem = ListContent.ITEM_MAP.get(id);

        if(savedInstanceState == null) {
            Fragment fragment = mItem.getFragment();

            //Bundle arguments = new Bundle();
            //arguments.putString(LocationDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));

            //fragment.setArguments(arguments);
            getFragmentManager().beginTransaction().add(R.id.item_detail_container, fragment).commit();
        }

        setTitle(mItem.toString());
    }
}