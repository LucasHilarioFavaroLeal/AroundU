package com.example.myfirstapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventList extends AppCompatActivity {

    String user_gid;

    SQLiteHelper database;

    private ProgressDialog pDialog;

    ArrayAdapter mAdapter;

    JSONParser jParser = new JSONParser();
    private static String url_read_events = "http://lasdpc.icmc.usp.br:54321/android/db_read_events.php";
    private static String url_delete_event = "http://lasdpc.icmc.usp.br:54321/android/db_delete_event.php";
    // products JSONArray
    JSONArray events = null;

    ArrayList<String> eventId;
    ArrayList<String> eventDescription;
    ArrayList<String> eventCategory;
    ArrayList<String> eventImage;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_EVENTS = "event";
    private static final String TAG_EVENT_ID = "id";

    String delete_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        database = new SQLiteHelper(this);

        Cursor Myself = database.getProfile(1);
        if(Myself.getCount() != 0) {
            Myself.moveToFirst();
            user_gid = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID));
        }

        eventId = new ArrayList<String>();
        eventDescription = new ArrayList<String>();
        eventCategory = new ArrayList<String>();
        eventImage = new ArrayList<String>();

        final ListView lv = (ListView)findViewById(R.id.EVENT_LIST);
        new EventList.GetEvents().execute();

        /*mAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                interestTitle);*/
        //lv.setAdapter(mAdapter);

        TextView dynamicTextView = new TextView(this);
        dynamicTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dynamicTextView.setText("Lista Vazia");
        lv.setEmptyView(dynamicTextView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EventList.this);

                final TextView et = new TextView(EventList.this);
                final int identifier = (int) id;

                et.setText(eventDescription.get(identifier));
                et.setPadding(20,20,20,20);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(et);

                alertDialogBuilder.setCancelable(true);

                // set dialog message
                alertDialogBuilder.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        delete_id = eventId.get(identifier);
                        areYouSure();
                    }
                });

                alertDialogBuilder.setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button_id) {
                        Intent intent = new Intent(EventList.this, EventEdit.class);
                        intent.putExtra("EVENT_USER_ID",user_gid);
                        intent.putExtra("EVENT_ID",eventId.get(identifier));
                        intent.putExtra("EVENT_DESCRIPTION",eventDescription.get(identifier));
                        intent.putExtra("EVENT_CATEGORY",eventCategory.get(identifier));
                        intent.putExtra("EVENT_IMAGE",eventImage.get(identifier));
                        startActivity(intent);
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });

        Button buttonAdd = (Button) findViewById(R.id.EVENT_LIST_BUTTON_NEW);
        buttonAdd.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(EventList.this, NewEventMap.class);
                intent.putExtra("EVENT_USER_ID",user_gid);
                intent.putExtra("EVENT_ID", "new");
                intent.putExtra("YOUR_POSITIONX",getIntent().getExtras().getDouble("YOUR_POSITIONX"));
                intent.putExtra("YOUR_POSITIONY",getIntent().getExtras().getDouble("YOUR_POSITIONY"));

                startActivity(intent);

            }
        });

    }

    @Override
    protected void onRestart(){
        super.onRestart();
        final ListView lv = (ListView)findViewById(R.id.EVENT_LIST);
        lv.setAdapter(null);
        new EventList.GetEvents().execute();
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.interest_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.INTEREST_LIST_NEW:
                Intent intent = new Intent(InterestList.this, InterestEdit.class);
                intent.putExtra("INTEREST_USER_ID",user_gid);
                intent.putExtra("INTEREST_ID", "new");
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }*/

    void areYouSure(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EventList.this);

        final TextView et = new TextView(EventList.this);

        et.setText("Deseja mesmo deletar este evento?\nEsta ação não pode ser desfeita!");
        et.setPadding(20,20,20,20);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);

        alertDialogBuilder.setCancelable(true);

        // set dialog message
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button_id) {
                new EventList.DeleteEvent().execute();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button_id) {
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    class GetEvents extends AsyncTask<String, String, String> {

        String CustomMessage = "";

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventList.this);
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters

            Map<String, Object> params = new LinkedHashMap<>();

            params.put("googleid", user_gid);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_read_events, params);

            // Check your log cat for JSON response
            //Log.d("All Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    /* users = json.getJSONArray(TAG_USERS);

                    JSONObject c = users.getJSONObject(0);*/

                    // Storing each json item in variable
                    events = json.getJSONArray(TAG_EVENTS);

                    eventId.clear();
                    eventDescription.clear();
                    eventCategory.clear();
                    eventImage.clear();

                    // looping through All Products
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject c = events.getJSONObject(i);
                        // creating new HashMap
                        //HashMap<String, String> map = new HashMap<>();

                        String event_id = c.getString(TAG_EVENT_ID);
                        String event_description = c.getString(TAG_DESCRIPTION);
                        String event_category = c.getString(TAG_CATEGORY);
                        String event_image = c.getString(TAG_IMAGE);

                        eventId.add(event_id);
                        eventDescription.add(event_description);
                        eventCategory.add(event_category);
                        eventImage.add(event_image);

                        // adding HashList to ArrayList
                        //interestList.add(map);
                    }

                } else {
                    String message = json.getString(TAG_MESSAGE);
                    CustomMessage = "A problem occured: " + message;
                }

            } catch (Exception e) {
                Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_read_events;
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    if (CustomMessage != "")
                        Toast.makeText(getApplicationContext(), CustomMessage, Toast.LENGTH_SHORT).show();
                    else {

                        final ListView lv = (ListView)findViewById(R.id.EVENT_LIST);

                        mAdapter = new ArrayAdapter(EventList.this,
                                android.R.layout.simple_list_item_1,
                                eventDescription);
                        lv.setAdapter(mAdapter);

                    }
                }
            });

        }
    }


    class DeleteEvent extends AsyncTask<String, String, String> {

        String CustomMessage = "";
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventList.this);
            pDialog.setMessage("Connecting. Please wait...");
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

            params.put("id", delete_id);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_delete_event, params);

            // Check your log cat for JSON response
            //Log.d("All Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {  }

                else { CustomMessage = "Data upload error."; }
            } catch (Exception e) {
                //Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_delete_event;
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
                    if(CustomMessage != ""){
                        Toast.makeText(getApplicationContext(), CustomMessage, Toast.LENGTH_SHORT).show();
                    }

                    else {

                        final ListView lv = (ListView)findViewById(R.id.EVENT_LIST);
                        lv.setAdapter(null);

                        new EventList.GetEvents().execute();
                    }

                }
            });

        }

    }

}
