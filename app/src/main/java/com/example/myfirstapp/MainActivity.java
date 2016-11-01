package com.example.myfirstapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;


public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

        GoogleApiClient mGoogleApiClient;
        int RC_SIGN_IN;
        SQLiteHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new SQLiteHelper(this);

        //database.DROPTHEBASS();

        Cursor Myself = database.getProfile(1);


        if(Myself.getCount() != 0) {

                Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatarex);
                Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 120, 120, true);
                ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                bMapScaled.compress(Bitmap.CompressFormat.PNG,0,BAOS);

                database.setAvatar(Base64.encodeToString(BAOS.toByteArray(),Base64.DEFAULT));


                bMap = BitmapFactory.decodeResource(getResources(), R.drawable.bannerprof);
                bMapScaled = Bitmap.createScaledBitmap(bMap, 120, 120, true);
                BAOS = new ByteArrayOutputStream();
                bMapScaled.compress(Bitmap.CompressFormat.PNG,0,BAOS);

                database.setBanner(Base64.encodeToString(BAOS.toByteArray(),Base64.DEFAULT));

                goToMapsPage();
                finish();
        }
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestId()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        findViewById(R.id.sign_in_button).setOnClickListener(this);

    }

    private void goToUserPage(){
        Intent intent = new Intent(this, UserPage.class);
        startActivity(intent);
    }

    private void goToMapsPage(){
        Intent intent = new Intent(this, MainMap.class);
        startActivity(intent);
    }

    private void goToProfileEdit(){
        Intent intent = new Intent(this, EditProfile.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        //Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String id = acct.getId();
            String name = acct.getDisplayName();
            database.login(id,name);
            goToMapsPage();

            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(getApplicationContext(), "Using this app requires a Google Account!", Toast.LENGTH_SHORT).show();
            //updateUI(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        finish();
    }
}

