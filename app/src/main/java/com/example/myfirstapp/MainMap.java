package com.example.myfirstapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainMap extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    SQLiteHelper database;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> userList;
    ArrayList<HashMap<String, String>> eventList;
    private static String url_map_refresh = "http://143.107.232.254:9070/html/db_read_area.php";
    private static String url_register = "http://143.107.232.254:9070/html/db_register.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_USERS = "user";
    private static final String TAG_EVENTS = "event";
    private static final String TAG_PID = "pid";
    private static final String TAG_USERID = "user_id";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_AVATAR = "avatar";
    private static final String TAG_GOOGLEID = "googleid";
    private static final String TAG_POSITIONX = "positionX";
    private static final String TAG_POSITIONY = "positionY";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_IMAGE = "image";

    // products JSONArray
    JSONArray users = null;
    JSONArray events = null;

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
        setContentView(R.layout.activity_main_map);

        database = new SQLiteHelper(this);

        zoomBar = (SeekBar) findViewById(R.id.Map_Zoom);

        // Hashmap for ListView
        userList = new ArrayList<HashMap<String,String> >();
        eventList = new ArrayList<HashMap<String,String> >();

        // Loading products in Background Thread
        new MainMap.LoadAllUsers().execute();

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            /*ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();*/
            drawer.openDrawer(Gravity.LEFT);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, InterestList.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, EventList.class);
            intent.putExtra("YOUR_POSITIONX",myPosition.getPosition().latitude);
            intent.putExtra("YOUR_POSITIONY",myPosition.getPosition().longitude);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            /*Intent intent = new Intent(this, AboutPage.class);
            startActivity(intent);*/

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }

        catch (SecurityException e){
            errorMessage("Around U precisa de sua localização via GPS para o devido funcionamento, por favor cheque suas conexões.");
        }

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
                .position(new LatLng(-22.0078723,-47.8963472)));

        myPosition.setTag(-1);

        Cursor Myself = database.getProfile(1);

        if(Myself.getCount() != 0){
            String MyAva;
            Myself.moveToFirst();
            MyAva = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_AVATAR));

            ByteArrayInputStream BAIS = new ByteArrayInputStream( Base64.decode(MyAva, Base64.DEFAULT) );

            Bitmap bMap = BitmapFactory.decodeStream(BAIS);
            bMap = Bitmap.createScaledBitmap(bMap, 50, 50, true);
            myPosition.setIcon(BitmapDescriptorFactory.fromBitmap(bMap));
        }

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
                //Toast.makeText(getApplicationContext(), "Zoom adjusted to " + ZoomLevel + " out of " + seekBar.getMax(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateMap() {


        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng test = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //LatLng test = new LatLng(-22.0078723,-47.8963472);
            radarCircle.setCenter(test);
            myPosition.setPosition(test);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(test));
            // Loading products in Background Thread
            new MainMap.LoadAllUsers().execute();
        }

        catch (SecurityException e){
            errorMessage("Around U precisa de sua localização via GPS para o devido funcionamento, por favor cheque suas conexões.");
        }

        catch (Exception e){
            errorMessage("Around U precisa de conexão à internet para seu devido funcionamento assim como sua posição de GPS, por favor cheque suas conexões.");
        }

    }


    private void goToViewProfile(int Id) {

        if(Id >= userList.size() ) {

            Id -= userList.size();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            LinearLayout myLayout = new LinearLayout(this);
            myLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
            myLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0);
            a.weight = 1;

            final TextView et = new TextView(this);

            et.setText(eventList.get(Id).get(TAG_DESCRIPTION));
            et.setPadding(20,20,20,20);
            et.setLayoutParams(a);

            myLayout.addView(et);

            if(!eventList.get(Id).get(TAG_IMAGE).equalsIgnoreCase("NULL")){
                final ImageView im = new ImageView(this);

                ByteArrayInputStream BAIS = new ByteArrayInputStream( Base64.decode(eventList.get(Id).get(TAG_IMAGE), Base64.DEFAULT));

                Bitmap bMap = BitmapFactory.decodeStream(BAIS);

                im.setImageBitmap(bMap);
                im.setLayoutParams(a);
                myLayout.addView(im);
            }

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(myLayout);

            // set dialog message
            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }

        else {
            Intent intent = new Intent(this, UserIBSPage.class);
            String googleid = userList.get(Id).get(TAG_GOOGLEID);
            intent.putExtra("PROFILE_ID", googleid);
            startActivity(intent);
        }
    }


    private void goToEditProfile() {
        //Intent intent = new Intent(this, EditProfile.class);
        Intent intent = new Intent(this, UserIBSPage.class);
        Cursor Myself = database.getProfile(1);
        if(Myself.getCount() != 0) {
            Myself.moveToFirst();
            String googleid = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID));
            intent.putExtra("PROFILE_ID",googleid);
            startActivity(intent);
        }
    }

    void errorMessage(String s){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        final TextView et = new TextView(this);

        et.setText(s);
        et.setPadding(20,20,20,20);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

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
            pDialog = new ProgressDialog(MainMap.this);
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

            Cursor Myself = database.getProfile(1);

            String MyGID;
            String MyName;
            String MyAva;
            String MyBann;
            String MyDesc;
            double MyPosX;
            double MyPosY;
            if(mLastLocation != null) {
                MyPosX = mLastLocation.getLatitude();
                MyPosY = mLastLocation.getLongitude();
            }
            else{
                MyPosX = 0;
                MyPosY = 0;
            }

            if(Myself.getCount() != 0){
                Myself.moveToFirst();

                MyGID = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID));
                MyName = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_NAME));
                MyAva = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_AVATAR));
                MyBann = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_BANNER));
                MyDesc = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_DESCRIPTION));
            }

            else{
                return null;
            }

            Map<String,Object> params = new LinkedHashMap<>();

            params.put("googleid", MyGID);
            params.put("name", MyName);
            params.put("avatar", MyAva);
            params.put("banner", MyBann);
            params.put("description", MyDesc);
            params.put("positionX", MyPosX);
            params.put("positionY", MyPosY);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_map_refresh, params);

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
                        String googleid = c.getString(TAG_GOOGLEID);
                        String avatar = c.getString(TAG_AVATAR);
                        String positionX = c.getString(TAG_POSITIONX);
                        String positionY = c.getString(TAG_POSITIONY);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_GOOGLEID, googleid);
                        map.put(TAG_AVATAR, avatar);
                        map.put(TAG_POSITIONX, positionX);
                        map.put(TAG_POSITIONY, positionY);

                        // adding HashList to ArrayList
                        userList.add(map);
                    }

                    // Getting Array of Products
                    events = json.getJSONArray(TAG_EVENTS);

                    eventList.clear();

                    //Toast.makeText(getApplicationContext(), "Events: " + events.length(), Toast.LENGTH_SHORT).show();
                    // looping through All Products
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject c = events.getJSONObject(i);

                        // Storing each json item in variable
                        String googleid = c.getString(TAG_USERID);
                        String description = c.getString(TAG_DESCRIPTION);
                        String positionX = c.getString(TAG_POSITIONX);
                        String positionY = c.getString(TAG_POSITIONY);
                        String category = c.getString(TAG_CATEGORY);
                        String image = c.getString(TAG_IMAGE);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_USERID, googleid);
                        map.put(TAG_DESCRIPTION, description);
                        map.put(TAG_POSITIONX, positionX);
                        map.put(TAG_POSITIONY, positionY);
                        map.put(TAG_CATEGORY, category);
                        map.put(TAG_IMAGE, image);

                        // adding HashList to ArrayList
                        eventList.add(map);
                    }

                } else {
                    String message = json.getString(TAG_MESSAGE);
                    if(message.compareToIgnoreCase("register") == 0) {
                        try {

                            params = new LinkedHashMap<>();

                            params.put("googleid", MyGID);
                            params.put("name", MyName);
                            params.put("avatar", MyAva);
                            params.put("banner", MyBann);
                            params.put("description", MyDesc);
                            params.put("positionX", MyPosX);
                            params.put("positionY", MyPosY);

                            json = jParser.makeHttpRequest(url_register, params);

                            success = json.getInt(TAG_SUCCESS);

                            if (success != 1)
                                CustomMessage = "Register failure! " + message;

                            else
                                CustomMessage = "Register successful, please refresh your map!";

                        } catch (Exception e) {
                            CustomMessage = "Error connecting to " + url_register;
                        }
                    }
                    else {
                        CustomMessage = "No data received from " + url_map_refresh + " Message: " + message;
                    }
                }
            } catch (Exception e) {
                //Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_map_refresh;
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
                        int currentMarker = 0;
                        for(int j = 0; j < userList.size(); j++) {

                            MarkerOptions mo = new MarkerOptions();

                            mo.position(new LatLng(Double.parseDouble(userList.get(j).get(TAG_POSITIONX)), Double.parseDouble(userList.get(j).get(TAG_POSITIONY))));

                            if((userList.get(j).get(TAG_AVATAR) == null) || (userList.get(j).get(TAG_AVATAR).compareToIgnoreCase("null") == 0))
                                mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.defaultmapicon));
                            else {
                                ByteArrayInputStream BAIS = new ByteArrayInputStream( Base64.decode(userList.get(j).get(TAG_AVATAR), Base64.DEFAULT) );

                                Bitmap bMap = BitmapFactory.decodeStream(BAIS);
                                Bitmap mapIcon = Bitmap.createScaledBitmap(bMap, 50, 50, true);

                                mo.icon(BitmapDescriptorFactory.fromBitmap(mapIcon));

                            }

                            markerList.add(mMap.addMarker(mo));
                            markerList.get(currentMarker).setTag(currentMarker);
                            currentMarker += 1;
                        }

                        for(int j = 0; j < eventList.size(); j++) {
                            MarkerOptions mo = new MarkerOptions().position(new LatLng(Double.parseDouble(eventList.get(j).get(TAG_POSITIONX)), Double.parseDouble(eventList.get(j).get(TAG_POSITIONY))))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.defaultevent));

                            switch(Integer.parseInt(eventList.get(j).get(TAG_CATEGORY))) {

                                case 1:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.dangerevent));
                                    break;
                                case 2:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.saleevent));
                                    break;
                                case 3:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.infoevent));
                                    break;
                                case 4:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.requestevent));
                                    break;
                                case 5:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.messageevent));
                                    break;
                                case 6:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.cultureevent));
                                    break;
                                case 7:
                                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.animalevent));
                                    break;

                            }

                            markerList.add(mMap.addMarker(mo));
                            markerList.get(currentMarker).setTag(currentMarker);
                            currentMarker += 1;
                        }

                        //Toast.makeText(getApplicationContext(), "Users: " + userList.size() + " Events: " + eventList.size(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }
}
