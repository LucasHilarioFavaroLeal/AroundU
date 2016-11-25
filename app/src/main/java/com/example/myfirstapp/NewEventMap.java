package com.example.myfirstapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NewEventMap extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Circle radarCircle;
    private Marker myPosition;
    private Marker eventPosition;

    private SeekBar zoomBar;

    double my_positionx;
    double my_positiony;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event_map);

        zoomBar = (SeekBar) findViewById(R.id.Map_Zoom);

        my_positionx = getIntent().getExtras().getDouble("YOUR_POSITIONX");
        my_positiony = getIntent().getExtras().getDouble("YOUR_POSITIONY");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        finish();
    }

    @Override
    public void onConnectionSuspended(int result) {
        finish();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                eventPosition.setPosition(point);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick (Marker M){

                if(M.getTag().equals(0)) return true;

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewEventMap.this);

                final TextView et = new TextView(NewEventMap.this);

                et.setText("Deseja colocar o evento nesta posição?");
                et.setPadding(20,20,20,20);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(et);

                alertDialogBuilder.setCancelable(true);

                // set dialog message
                alertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goToNewEvent();
                    }
                });

                alertDialogBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button_id) {
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();

                return true;
            }
        });

        LatLng test = new LatLng(my_positionx,my_positiony);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(test));

        myPosition = mMap.addMarker(new MarkerOptions()
                .position(test)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.defaultmapicon))
                .alpha(0.5f)
        );

        myPosition.setTag(0);

        eventPosition = mMap.addMarker(new MarkerOptions()
                .position(test)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.defaultevent))
        );

        eventPosition.setTag(1);

        CircleOptions circleOptions = new CircleOptions()
                .center(test)
                .radius(250) // In meters
                .strokeColor(0x7f000000);

        radarCircle = mMap.addCircle(circleOptions);

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int ZoomLevel;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ZoomLevel = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Adjusting zoom", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo( (float) (16 + (ZoomLevel*0.2)) ));
                Toast.makeText(getApplicationContext(), "Zoom adjusted to " + ZoomLevel + " out of " + seekBar.getMax(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToNewEvent() {
        Intent intent = new Intent(this, EventEdit.class);

        double X = eventPosition.getPosition().latitude;
        double Y = eventPosition.getPosition().longitude;

        intent.putExtra("EVENT_USER_ID",getIntent().getExtras().getString("EVENT_USER_ID"));
        intent.putExtra("EVENT_ID",getIntent().getExtras().getString("EVENT_ID"));
        intent.putExtra("EVENT_CATEGORY","0");
        intent.putExtra("EVENT_IMAGE","null");
        intent.putExtra("EVENT_POSITIONX",X);
        intent.putExtra("EVENT_POSITIONY",Y);

        startActivity(intent);
        finish();
    }

}
