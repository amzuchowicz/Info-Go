package com.sdstf.info_go;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdstf.info_go.content.ListContent;
import com.sdstf.info_go.helpers.DBPictureHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class PictureDetailFragment extends Fragment implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * The dummy content this fragment is presenting.
     */
    private ListContent.ListItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    String mCurrentPhotoPath;
    private int count = 0;
    String filename;

    private MapFragment myMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private DBPictureHelper picturedb;

    public PictureDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMap();
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        //if (mMap == null) {
        // Try to obtain the map
        // note that because this class itself is a fragment,
        // to find the map fragment inside this fragment,
        // though it has unique id (i.e., R.id.map), you need to use the child's fragment manager.
        // Using getFragmentManager() instead of getChildFragmentManager() below will not work
        if (getFragmentManager().findFragmentById(R.id.PictureMap) != null) {
            // Our map fragment is inside an activity (pre 5.0)
            //myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.LocationMap);
        } else {
            // Our map fragment is inside a parent fragment (tablet) (post 5.0)
            myMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.PictureMap);
        }

        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                System.out.println("reloaded");
                count = 0;
                getPictures();
                try {
                    mMap.setMyLocationEnabled(true); // Enable the map's location dot
                } catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //final Bitmap imageBitmap = (Bitmap) extras.get("data");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText textField = new EditText(getActivity());
            builder.setTitle("Enter a description");
            builder.setView(textField);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm a");
                    String timestamp = formatter.format(date);
                    String description = textField.getText().toString();
                    String title = timestamp + " " + description;
                    picturedb.insertPicture(description, timestamp, filename, mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mCurrentPhotoPath = "/storage/emulated/0/Android/data/com.sdstf.info_go/files/Pictures/" + filename +".jpg";

                    addMarker(mLastLocation, title);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }

    public void addMarker(Location location, String description) {


        MarkerOptions mo = new MarkerOptions();
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
       // System.out.println(loc +"\n"+description);
        mo.position(loc);
        mo.icon(BitmapDescriptorFactory.fromBitmap(getPic()));
        mo.title(description);
        Marker newMarker = mMap.addMarker(mo);


        // move focus to where the marker is
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17)); // 10 is zoom level
    }

    private File createImageFile(String imageFileName) throws IOException {
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();
        //System.out.println(mCurrentPhotoPath);
        return image;
    }

    private Bitmap getPic() {
        // Get the dimensions of the View
        int targetW = 150;
        int targetH = 150;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        //System.out.println(mCurrentPhotoPath);
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_picture_detail, container, false);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        picturedb = new DBPictureHelper(getActivity());

        //getPictures();
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch (SecurityException se) {
            System.out.println("Please grant permission for location services!");
        }

        Button btnTakePicture = (Button) rootView.findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                catch (SecurityException se) {
                    System.out.println("Please grant permission for location services!");
                }
                if(mLastLocation != null) {
                    // We have connected as last location was retrieved
                    System.out.println(mLastLocation);
                }
                else {
                    System.out.println("Unable to retrieve last known location!");
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create an image file name
                    filename = "picture_" + count;
                    count++;

                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile(filename);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.sdstf.info_go.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        return rootView;
    }
    public void getPictures(){
        int numberOfRows = picturedb.numberOfRows();
        //System.out.println(numberOfRows);
        Cursor res =  picturedb.getAll();
        res.moveToFirst();
        while(numberOfRows > count){
           // picturedb.deletePicture(res.getInt(res.getColumnIndex("id")));
            System.out.println("Latitude: " + res.getString(res.getColumnIndex("latitude"))
                    + ", Longitude: " + res.getString(res.getColumnIndex("longitude"))
                    + ", TimeStamp: " + res.getString(res.getColumnIndex("timestamp"))
                    + ", Description: " + res.getString(res.getColumnIndex("description"))
                    + ", Filename: " + res.getString(res.getColumnIndex("filename")));
            //set current photo path
            mCurrentPhotoPath = "/storage/emulated/0/Android/data/com.sdstf.info_go/files/Pictures/" + res.getString(res.getColumnIndex("filename"))+".jpg";

            //location
            Location newLocation = new Location("marker");
            newLocation.setLatitude(res.getDouble(res.getColumnIndex("latitude")));
            newLocation.setLongitude(res.getDouble(res.getColumnIndex("longitude")));
            String title = res.getString(res.getColumnIndex("timestamp")) + " " + res.getString(res.getColumnIndex("description"));
            //add marker
            addMarker(newLocation, title);
            count++;
            res.moveToNext();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("FAIL");
    }
}
