package com.example.myfirstapp;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserIBSPage extends AppCompatActivity {

    // This is the Adapter being used to display the list's data
    ArrayAdapter mAdapter;
    SQLiteHelper database;

    // Progress Dialog
    private ProgressDialog pDialog;

    String user_gid;


    JSONParser jParser = new JSONParser();
    private static String url_read_user = "http://191.189.96.55:54321/android/db_read_profile.php";
    // products JSONArray
    JSONArray users = null;
    JSONArray interests = null;

    ArrayList<HashMap<String, String>> interestList;
    ArrayList<String> interestTitle;
    ArrayList<String> interestDescription;

    String USER_NAME;
    String USER_DESCRIPTION;
    byte[] USER_AVATAR;
    byte[] USER_BANNER;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_USERS = "user";
    private static final String TAG_NAME = "name";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_AVATAR = "avatar";
    private static final String TAG_BANNER = "banner";
    private static final String TAG_INTERESTS = "interest";
    private static final String TAG_INTEREST_TITLE = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ibspage);

        Intent intent = getIntent();
        user_gid = intent.getExtras().getString("PROFILE_ID");

        //interestList = new ArrayList<HashMap<String,String> >();
        interestTitle = new ArrayList<String>();
        interestDescription = new ArrayList<String>();

        // Loading products in Background Thread
        new UserIBSPage.LoadUser().execute();
        ImageView image = (ImageView) findViewById(R.id.header_imageview);
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultbanner);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 168, 150, true);
        image.setImageBitmap(bMapScaled);

        ImageView image2 = (ImageView) findViewById(R.id.avatar_imageview);
        bMap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultavatar);
        bMapScaled = Bitmap.createScaledBitmap(bMap, 120, 120, true);
        image2.setImageBitmap(bMapScaled);

        final TextView abouttv = (TextView)findViewById(R.id.about_textview);
        abouttv.setText("Era uma vez uma descrição de um wallaby que adorava brincar com outros wallabies e era muito feliz. And that's the end.");
        abouttv.setVisibility(View.GONE);


        final ListView lv = (ListView)findViewById(R.id.listo);

        // Create a progress bar to display while the list loads
        /*ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        lv.setEmptyView(progressBar);*/

        // Must add the progress bar to the root of the layout
        /*ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);*/

        // For the cursor adapter, specify which columns go into which views
        //int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()

        mAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                interestTitle);
        lv.setAdapter(mAdapter);
        setListViewHeightBasedOnChildren(lv);

        TextView dynamicTextView = new TextView(this);
        dynamicTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dynamicTextView.setText("Lista Vazia");
        lv.setEmptyView(dynamicTextView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v,int position, long id)
            {

            }
        });

        Button buttonOne = (Button) findViewById(R.id.interesses_button);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                lv.setVisibility(View.VISIBLE);
                abouttv.setVisibility(View.GONE);
            }
        });

        Button buttonTwo = (Button) findViewById(R.id.about_button);
        buttonTwo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                lv.setVisibility(View.GONE);
                abouttv.setVisibility(View.VISIBLE);
            }
        });

        Button buttonThree = (Button) findViewById(R.id.notinteresses_button);
        buttonThree.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        //getLoaderManager().initLoader(0, null, this);
    }

    // Called when a new Loader needs to be created
    /*public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        /*return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }*/

    // Called when a previously created loader has finished loading
    /*public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        //mAdapter.swapCursor(data);
    }*/

    // Called when a previously created loader is reset, making the data unavailable
    /*public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        //mAdapter.swapCursor(null);
    }*/

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    class LoadUser extends AsyncTask<String, String, String> {

        String CustomMessage = "";

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UserIBSPage.this);
            pDialog.setMessage("Loading users. Please wait...");
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
            JSONObject json = jParser.makeHttpRequest(url_read_user, params);

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
                    USER_NAME = json.getString(TAG_NAME);
                    USER_DESCRIPTION = json.getString(TAG_DESCRIPTION);
                    if((json.getString(TAG_AVATAR) != null)) USER_AVATAR = Base64.decode(json.getString(TAG_AVATAR), Base64.NO_WRAP);
                    else USER_AVATAR = null;
                    if((json.getString(TAG_BANNER) != null)) USER_BANNER = Base64.decode(json.getString(TAG_BANNER), Base64.NO_WRAP);
                    else USER_BANNER = null;
                    interests = json.getJSONArray(TAG_INTERESTS);

                    // looping through All Products
                    for (int i = 0; i < interests.length(); i++) {
                        JSONObject c = interests.getJSONObject(i);
                        // creating new HashMap
                        //HashMap<String, String> map = new HashMap<>();

                        String interest_title = c.getString(TAG_INTEREST_TITLE);
                        String interest_description = c.getString(TAG_DESCRIPTION);

                        interestTitle.add(interest_title);
                        interestDescription.add(interest_description);

                        // adding HashList to ArrayList
                        //interestList.add(map);
                    }
                } else {
                    String message = json.getString(TAG_MESSAGE);
                    CustomMessage = "A problem occured: " + message;
                }

            } catch (Exception e) {
                Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_read_user;
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

                        if(USER_BANNER != null) {

                            ImageView image = (ImageView) findViewById(R.id.header_imageview);
                            Bitmap bMap = BitmapFactory.decodeByteArray(USER_BANNER,0,USER_BANNER.length);
                            image.setImageBitmap(bMap);
                        }

                        if(USER_AVATAR != null) {
                            //ByteArrayInputStream BAIS = new ByteArrayInputStream(USER_AVATAR);

                            ImageView image2 = (ImageView) findViewById(R.id.avatar_imageview);
                            Bitmap bMap = BitmapFactory.decodeByteArray(USER_AVATAR,0,USER_AVATAR.length);
                            //Bitmap bMap = BitmapFactory.decodeStream(BAIS);
                            image2.setImageBitmap(bMap);
                        }

                        final TextView abouttv = (TextView) findViewById(R.id.about_textview);
                        abouttv.setText(USER_DESCRIPTION);
                        abouttv.setVisibility(View.GONE);

                        final TextView name = (TextView) findViewById(R.id.profile_name);
                        name.setText(USER_NAME);


                    }
                }
            });

        }
    }
}
