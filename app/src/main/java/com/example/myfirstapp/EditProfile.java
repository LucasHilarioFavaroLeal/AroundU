package com.example.myfirstapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {

    SQLiteHelper database;
    private static final int PICK_IMAGE = 100;
    private ImageView imageView;
    String imageToSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editpage);
        TextView Name = (TextView) findViewById(R.id.edit_username);
        TextView Description = (TextView) findViewById(R.id.edit_about);
        imageView = (ImageView) findViewById(R.id.avatar_im);

        String NameToSave = "";
        String TextToSave = "";

        database = new SQLiteHelper(this);
        Cursor Myself = database.getProfile(1);

        if (Myself.getCount() != 0) {
            Myself.moveToFirst();
            NameToSave = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_NAME));
            TextToSave = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_DESCRIPTION));
        }

        Name.setText(NameToSave);
        Description.setText(TextToSave);

        Button cancel = (Button) findViewById(R.id.edit_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEdit();
            }
        });

        Button save = (Button) findViewById(R.id.edit_conclude);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEdit();
            }
        });

        Button pickImageButton = (Button) findViewById(R.id.pickbutts);
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
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
            Uri loadedimage = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), loadedimage);
                imageView.setImageURI(loadedimage);
                Bitmap bMapScaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                bMapScaled.compress(Bitmap.CompressFormat.PNG,0,BAOS);
                byte[] encodedImg = BAOS.toByteArray();
                imageToSave = Base64.encodeToString(encodedImg, Base64.NO_WRAP);
            }
            catch (IOException e) {
                errorMessage("Não foi possível ler a imagem escolhida. Tente novamente.");
            }
        }
    }

    public void cancelEdit() {
            finish();
    }

    public void saveEdit(){
        TextView Name = (TextView) findViewById(R.id.edit_username);
        TextView Description = (TextView) findViewById(R.id.edit_about);

        database.setAvatar(imageToSave);

        Cursor Myself = database.getProfile(1);
        if(Myself.getCount() != 0) {
            Myself.moveToFirst();
            database.updateProfile(1,Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID)),Name.getText().toString(),Description.getText().toString());
        }
        else database.insertProfile(Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID)),Name.getText().toString(),Description.getText().toString());
        finish();
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

}
