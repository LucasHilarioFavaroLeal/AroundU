package com.example.myfirstapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class EditProfile extends AppCompatActivity {

    SQLiteHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = new SQLiteHelper(this);

        setContentView(R.layout.activity_edit_profile);

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
        try{
            finalize();
        }
        catch(Throwable error){
        }

    }

    public void saveEdit(){
        database.insertProfile("Zezinho","Um cara dahora");
    }
}
