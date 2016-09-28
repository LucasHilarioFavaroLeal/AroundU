package com.example.myfirstapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;

    private Circle radarCircle;

    private Marker myPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addApi(LocationServices.API)
                    .build();
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
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                updateMap();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick (Marker M){
                goToViewProfile();
                return true;
            }
        });

        myPosition = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Você está aqui!"));

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(0, 0))
                .radius(250) // In meters
                .strokeColor(0x7f000000);

// Get back the mutable Circle
        radarCircle = mMap.addCircle(circleOptions);

        // Add a marker in Sydney and move the camera
        LatLng test = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(test));

        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }

    public void updateMap() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng test = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        radarCircle.setCenter(test);
        myPosition.setPosition(test);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(test));

    }


    private void goToViewProfile() {
        Intent intent = new Intent(this, ViewProfile.class);
        /*EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(PROFILE_NAME, message);
        intent.putExtra(PROFILE_TEXT,message);*/
        startActivity(intent);
    }

}
