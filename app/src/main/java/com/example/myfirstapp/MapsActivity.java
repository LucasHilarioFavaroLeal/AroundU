package com.example.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> userList;
    private static String url_database = "http://lasdpc.icmc.usp.br:54321/android/db_read_all.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USERS = "user";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_POSITIONX = "positionX";
    private static final String TAG_POSITIONY = "positionY";

    // products JSONArray
    JSONArray users = null;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Circle radarCircle;
    private Marker myPosition;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();

    private SeekBar zoomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        zoomBar = (SeekBar) findViewById(R.id.Map_Zoom);

        // Hashmap for ListView
        userList = new ArrayList<HashMap<String,String> >();

        // Loading products in Background Thread
        new LoadAllUsers().execute();

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
                int Identifier = (int) M.getTag();
                if(Identifier != -1) goToViewProfile(Identifier);
                else goToEditProfile();
                return true;
            }
        });

        myPosition = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(-22.0078723,-47.8963472))
                .title("Você está aqui!"));

        myPosition.setTag(-1);

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(-22.0078723,-47.8963472))
                .radius(250) // In meters
                .strokeColor(0x7f000000);

        radarCircle = mMap.addCircle(circleOptions);

        LatLng test = new LatLng(-22.0078723,-47.8963472);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(test));

        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

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

    public void updateMap() {

        // Loading products in Background Thread
        new LoadAllUsers().execute();

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //LatLng test = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        LatLng test = new LatLng(-22.0078723,-47.8963472);
        radarCircle.setCenter(test);
        myPosition.setPosition(test);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(test));

    }


    private void goToViewProfile(int Id) {
        Intent intent = new Intent(this, ViewProfile.class);
        String name = userList.get(Id).get(TAG_NAME);
        String desc = userList.get(Id).get(TAG_DESCRIPTION);
        intent.putExtra("PROFILE_NAME",name);
        intent.putExtra("PROFILE_TEXT",desc);
        startActivity(intent);
    }


    private void goToEditProfile() {
        Intent intent = new Intent(this, MainMap.class);
        startActivity(intent);
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllUsers extends AsyncTask<String, String, String> {

        String CustomMessage = "";
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Loading users. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            Map<String,Object> params = new LinkedHashMap<>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_database, params);

            // Check your log cat for JSON response
            //Log.d("All Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    users = json.getJSONArray(TAG_USERS);


                    userList.clear();

                    // looping through All Products
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject c = users.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String description = c.getString(TAG_DESCRIPTION);
                        String positionX = c.getString(TAG_POSITIONX);
                        String positionY = c.getString(TAG_POSITIONY);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_DESCRIPTION, description);
                        map.put(TAG_POSITIONX, positionX);
                        map.put(TAG_POSITIONY, positionY);

                        // adding HashList to ArrayList
                        userList.add(map);
                    }
                } else {
                    CustomMessage = "No data received from " + url_database;
                }
            } catch (Exception e) {
                //Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_database;
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    if(CustomMessage != "")
                        Toast.makeText(getApplicationContext(), CustomMessage, Toast.LENGTH_SHORT).show();
                    else {
                        for(int j = 0; j < markerList.size(); j++) {
                            markerList.get(j).remove();
                        }

                        markerList.clear();
                        for(int j = 0; j < userList.size(); j++) {
                            markerList.add(mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(userList.get(j).get(TAG_POSITIONX)), Double.parseDouble(userList.get(j).get(TAG_POSITIONY))))
                                    .title(userList.get(j).get(TAG_NAME))));
                            markerList.get(j).setTag(j);
                        }
                    }
                }
            });

        }

    }
}
