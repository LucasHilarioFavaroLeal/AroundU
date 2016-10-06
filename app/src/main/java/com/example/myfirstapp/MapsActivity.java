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
import android.widget.SimpleAdapter;

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
    private static String url_database = "http://192.168.0.100:54321/android/db_read_all.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USERS = "users";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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
        //LatLng test = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        LatLng test = new LatLng(0,0);
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

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllUsers extends AsyncTask<String, String, String> {

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
            JSONObject json = jParser.makeHttpRequest(url_database, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    users = json.getJSONArray(TAG_USERS);

                    // looping through All Products
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject c = users.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String positionX = c.getString(TAG_POSITIONX);
                        String positionY = c.getString(TAG_POSITIONY);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_POSITIONX, positionX);
                        map.put(TAG_POSITIONY, positionY);

                        // adding HashList to ArrayList
                        userList.add(map);
                        markerList.clear();
                        for(int j = 0; j < userList.size(); j++) {
                            markerList.add(mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(userList.get(j).get(TAG_POSITIONX)), Double.parseDouble(userList.get(j).get(TAG_POSITIONY))))
                                    .title(userList.get(j).get(TAG_NAME))));
                            Log.e("Is this really expected", userList.get(j).get(TAG_POSITIONX));
                        }
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    //Intent i = new Intent(getApplicationContext(),
                           // NewProductActivity.class);
                    // Closing all previous activities
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(i);
                }
            } catch (JSONException e) {
                Log.e("JSON Exception", e.toString());
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
            //runOnUiThread(new Runnable() {
            //    public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
            /*        ListAdapter adapter = new SimpleAdapter(
                            MapsActivity.this, productsList,
                            R.layout.list_item, new String[] { TAG_PID,
                            TAG_NAME},
                            new int[] { R.id.pid, R.id.name });
                    // updating listview
                    setListAdapter(adapter);
                }
            }); */

        }

    }
}
