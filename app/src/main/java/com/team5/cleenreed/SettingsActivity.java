package com.team5.cleenreed;

import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.GmailScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
public class SettingsActivity extends AppCompatActivity {

    private Button signOutButton;
    private Button backButton;
    private TextView textView;
    private String email;
    FirebaseAuth mAuth;
    private GoogleSignInAccount mAccount;
    GoogleSignInClient mGoogleSignInClient;
    private Credential mCredential;
    private String TAG = "CleenReed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        signOutButton = (Button) findViewById(R.id.sign_out_button);
        backButton = (Button) findViewById(R.id.back_button);
        textView = (TextView) findViewById(R.id.textView2);

        // Get shared instance of FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();

        //
        // Get last Signed in Account From Main Activity Sign in
        mAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        email = mAccount.getEmail();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(
                        new Scope(GmailScopes.GMAIL_READONLY),
                        new Scope(GmailScopes.GMAIL_MODIFY),
                        new Scope(GmailScopes.GMAIL_LABELS))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        textView.setText("Currently Signed in: " + email);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this,EmailsActivity.class);
                startActivity(intent);
            }
        });

    }
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        mGoogleSignInClient.revokeAccess();
                        mGoogleSignInClient.signOut();
                        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
