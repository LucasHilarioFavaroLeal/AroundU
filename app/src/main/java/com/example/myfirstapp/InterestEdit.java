package com.example.myfirstapp;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InterestEdit extends AppCompatActivity {

    String user_gid;
    String interest_id;
    String interest_title;
    String interest_description;

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();

    private static String url_update_interest = "http://191.189.96.55:54321/android/db_update_interest.php";
    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_edit);

        final TextView titleView = (TextView) findViewById(R.id.INTEREST_EDIT_TITLE);

        final TextView descView = (TextView) findViewById(R.id.INTEREST_EDIT_DESCRIPTION);

        user_gid = getIntent().getExtras().getString("INTEREST_USER_ID");
        interest_id = getIntent().getExtras().getString("INTEREST_ID");

        if (interest_id.equalsIgnoreCase("new") == false) {
            interest_title = getIntent().getExtras().getString("INTEREST_TITLE");
            interest_description = getIntent().getExtras().getString("INTEREST_DESCRIPTION");
            titleView.setText(interest_title);
            descView.setText(interest_description);
        } else {
            interest_title = null;
            interest_description = null;
        }


        Button buttonOne = (Button) findViewById(R.id.INTEREST_EDIT_BUTTON_CANCEL);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Button buttonTwo = (Button) findViewById(R.id.INTEREST_EDIT_BUTTON_OK);
        buttonTwo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                interest_title = titleView.getText().toString();
                interest_description = descView.getText().toString();

                new InterestEdit.UpdateInterest().execute();
            }
        });
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class UpdateInterest extends AsyncTask<String, String, String> {

        String CustomMessage = "";
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InterestEdit.this);
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

            params.put("googleid", user_gid);
            params.put("id", interest_id);
            params.put("title", interest_title);
            params.put("description", interest_description);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_update_interest, params);

            // Check your log cat for JSON response
            //Log.d("All Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {  }

                else { CustomMessage = "Data upload error."; }
            } catch (Exception e) {
                //Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_update_interest;
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
                        finish();
                    }
                }
            });

        }

    }
}
