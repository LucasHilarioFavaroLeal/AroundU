package com.example.myfirstapp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EditProfile extends AppCompatActivity {

    SQLiteHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        TextView Name = (TextView) findViewById(R.id.EditProfile_Username);
        TextView Description = (TextView) findViewById(R.id.EditProfile_Text);

        String NameToSave = "Anon";
        String TextToSave = "WATCH_DOGS";

        database = new SQLiteHelper(this);
        Cursor Myself = database.getProfile(1);

        if(Myself.getCount() != 0){
            Myself.moveToFirst();
            NameToSave = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_NAME));
            TextToSave = Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_DESCRIPTION));
        }

        Name.setText(NameToSave);
        Description.setText(TextToSave);

        Button cancel = (Button) findViewById(R.id.EditProfile_Cancel);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cancelEdit();
            }
        });

        Button save = (Button) findViewById(R.id.EditProfile_Save);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                saveEdit();
            }
        });
    }

    public void cancelEdit() {
            finish();
    }

    public void saveEdit(){
        TextView Name = (TextView) findViewById(R.id.EditProfile_Username);
        TextView Description = (TextView) findViewById(R.id.EditProfile_Text);

        Cursor Myself = database.getProfile(1);
        if(Myself.getCount() != 0) {
            Myself.moveToFirst();
            database.updateProfile(1,Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID)),Name.getText().toString(),Description.getText().toString());
        }
        else database.insertProfile(Myself.getString(Myself.getColumnIndex(SQLiteHelper.PROFILE_COLUMN_GOOGLEID)),Name.getText().toString(),Description.getText().toString());
        finish();
    }
}
