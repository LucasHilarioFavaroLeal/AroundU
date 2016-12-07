package com.example.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class EventEdit extends AppCompatActivity {

    String user_gid;
    String event_id;
    String event_description;

    Double event_positionx;
    Double event_positiony;

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();

    private static String url_update_event = "http://lasdpc.icmc.usp.br:54321/android/db_update_event.php";
    private static final String TAG_SUCCESS = "success";

    int event_categoryID = 0;
    String event_image;

    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        final TextView descView = (TextView) findViewById(R.id.EVENT_EDIT_DESCRIPTION);

        user_gid = getIntent().getExtras().getString("EVENT_USER_ID");
        event_id = getIntent().getExtras().getString("EVENT_ID");
        event_image = getIntent().getExtras().getString("EVENT_IMAGE");
        event_categoryID = Integer.parseInt(getIntent().getExtras().getString("EVENT_CATEGORY"));

        if (!event_id.equalsIgnoreCase("new")){
            event_description = getIntent().getExtras().getString("EVENT_DESCRIPTION");
            event_positionx = 0.0;
            event_positiony = 0.0;
            descView.setText(event_description);
        } else {
            event_description = null;
            event_positionx = getIntent().getExtras().getDouble("EVENT_POSITIONX");
            event_positiony = getIntent().getExtras().getDouble("EVENT_POSITIONY");

        }

        if(!event_image.equalsIgnoreCase("NULL")){
            ByteArrayInputStream BAIS = new ByteArrayInputStream( Base64.decode(event_image, Base64.DEFAULT));

            Bitmap bMap = BitmapFactory.decodeStream(BAIS);

            ImageButton buttonFour = (ImageButton) findViewById(R.id.EVENT_EDIT_IMAGE);
            buttonFour.setImageBitmap(bMap);
        }

        TextView categoryName = (TextView) findViewById(R.id.EVENT_EDIT_CATEGORIA);
        ImageView categoryIcon = (ImageView) findViewById(R.id.EVENT_EDIT_IMAGE_ICON);
        switch(event_categoryID) {

            case 0:
                categoryName.setText("AVISO");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.defaultevent));
                break;
            case 1:
                categoryName.setText("PERIGO");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.dangerevent));
                break;
            case 2:
                categoryName.setText("OFERTA");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.saleevent));
                break;
            case 3:
                categoryName.setText("INFO");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.infoevent));
                break;
            case 4:
                categoryName.setText("PEDIDO");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.requestevent));
                break;
            case 5:
                categoryName.setText("MENSAGEM");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.messageevent));
                break;
            case 6:
                categoryName.setText("CULTURA");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.cultureevent));
                break;
            case 7:
                categoryName.setText("ANIMAIS");
                categoryIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.animalevent));
                break;

        }


        Button buttonOne = (Button) findViewById(R.id.EVENT_EDIT_BUTTON_CANCEL);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Button buttonTwo = (Button) findViewById(R.id.EVENT_EDIT_BUTTON_OK);
        buttonTwo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                event_description = descView.getText().toString();

                new EventEdit.UpdateEvent().execute();
            }
        });

        final ImageButton buttonThree = (ImageButton) findViewById(R.id.EVENT_EDIT_CATEGORY_BUTTON);
        buttonThree.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(EventEdit.this, buttonThree);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.event_popupmenu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        TextView categoryName = (TextView) findViewById(R.id.EVENT_EDIT_CATEGORIA);
                        categoryName.setText(item.getTitle());

                        ImageView categoryIcon = (ImageView) findViewById(R.id.EVENT_EDIT_IMAGE_ICON);
                        categoryIcon.setImageDrawable(item.getIcon());

                        event_categoryID = item.getItemId() - R.id.CATEGORY_A;
                        //Toast.makeText(getApplicationContext(), "Valor: " + event_categoryID, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu

            }
        });

        ImageButton buttonFour = (ImageButton) findViewById(R.id.EVENT_EDIT_IMAGE);
        buttonFour.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {openGallery();
            }
        });
    }


    private void openGallery() {
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageToSave = data.getData();

            try {

                Bitmap bMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageToSave);
                int NewWidth = 280;
                int NewHeight = (int) ((double) bMap.getHeight()*((double) NewWidth/(double) bMap.getWidth()));
                Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, NewWidth, NewHeight, true);
                ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                bMapScaled.compress(Bitmap.CompressFormat.PNG, 0, BAOS);
                byte[] encodedImg = BAOS.toByteArray();
                event_image = Base64.encodeToString(encodedImg, Base64.NO_WRAP);

                ImageButton buttonFour = (ImageButton) findViewById(R.id.EVENT_EDIT_IMAGE);
                buttonFour.setImageBitmap(bMapScaled);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class UpdateEvent extends AsyncTask<String, String, String> {

        String CustomMessage = "";
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventEdit.this);
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
            params.put("id", event_id);
            params.put("description", event_description);
            params.put("positionx", event_positionx);
            params.put("positiony", event_positiony);
            params.put("category", event_categoryID);

            if(!event_image.equalsIgnoreCase("NULL"))
            params.put("image", event_image);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_update_event, params);

            // Check your log cat for JSON response
            //Log.d("All Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {  }

                else { CustomMessage = "Data upload error."; }
            } catch (Exception e) {
                //Log.e("JSON Exception", e.toString());
                CustomMessage = "Error connecting to " + url_update_event;
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
