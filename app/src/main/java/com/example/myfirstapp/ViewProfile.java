package com.example.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ViewProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Intent intent = getIntent();
        String name = intent.getExtras().getString("PROFILE_ID");
        String text = intent.getExtras().getString("PROFILE_ID");
        TextView nameText = new TextView(this);
        nameText.setTextSize(40);
        nameText.setText(name);

        ViewGroup nameView = (ViewGroup) findViewById(R.id.ViewProfile_Name);
        nameView.addView(nameText);


        TextView descriptionText = new TextView(this);
        descriptionText.setTextSize(40);
        descriptionText.setText(text);

        ViewGroup textView = (ViewGroup) findViewById(R.id.ViewProfile_TextField);
        textView.addView(descriptionText);

        Button back = (Button) findViewById(R.id.ViewProfile_BackButton);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

    }
}
