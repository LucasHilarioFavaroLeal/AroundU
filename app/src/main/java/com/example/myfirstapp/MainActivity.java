package com.example.myfirstapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button userpage = (Button) findViewById(R.id.Home_UserPages);
        userpage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToUserPage();
            }
        });

        Button maps = (Button) findViewById(R.id.Home_Map);
        maps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToMapsPage();
            }
        });

        Button editprofile = (Button) findViewById(R.id.Home_EditProfile);
        editprofile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToProfileEdit();
            }
        });
    }

    private void goToUserPage(){
        Intent intent = new Intent(this, UserPage.class);
        startActivity(intent);
    }

    private void goToMapsPage(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void goToProfileEdit(){
        Intent intent = new Intent(this, EditProfile.class);
        startActivity(intent);
    }
}
