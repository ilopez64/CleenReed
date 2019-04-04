package com.team5.cleenreed;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.services.gmail.GmailScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Tab1Bulk extends Fragment {

    private FirebaseAuth mAuth;
    private GoogleSignInAccount acct;
    private TextView name;
    private static final String[] SCOPES = {GmailScopes.GMAIL_READONLY};
    private String email;
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "Tag1Bulk";
    private GoogleApiClient mGoogleApiClient;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab1bulk, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        email = user.getEmail();

        return rootView;
    }

    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


}
