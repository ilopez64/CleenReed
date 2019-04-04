package com.team5.cleenreed;

import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.services.gmail.GmailScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Gmail {

    private FirebaseAuth mAuth;
    private GoogleSignInAccount acct;
    private static final String[] SCOPES = {GmailScopes.GMAIL_READONLY};
    private String email;
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "Tag1Bulk";
    private GoogleApiClient mGoogleApiClient;


}
