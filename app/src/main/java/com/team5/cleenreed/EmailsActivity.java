package com.team5.cleenreed;

import android.accounts.Account;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


//Firebase APIs
import com.google.firebase.auth.FirebaseAuth;

//Chaquopy
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

//Gmail imports
import com.google.android.gms.common.api.ApiException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import javax.mail.*;
import javax.mail.internet.MimeMessage;


public class EmailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> list;
    private RecyclerAdapter adapter;

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private String email;
    private FirebaseAuth mAuth;
    private GoogleSignInAccount mAccount;
    private static final String[] SCOPES = {GmailScopes.GMAIL_READONLY,GmailScopes.GMAIL_MODIFY,GmailScopes.GMAIL_METADATA};
    private static final String TAG = "CleenReed";
    private static final int RC_SIGN_IN = 9001;
    private GoogleAccountCredential mCredential;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emails);

        // Get last Signed in Account From Main Activity Sign in
        mAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        email = mAccount.getEmail();

        Gmail

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        // Create Gmail service object
        Gmail gmail = new Gmail.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, mCredential)
                .setApplicationName(TAG)
                .build();

        //String user = "me";

        //getLabel(service,email,"IMPORTANT");
        List<String> labels = new ArrayList<>();
        labels.add("IMPORTANT");

        List<Message> messages = new ArrayList<>();
        //messages = listMessagesWithLabels(gmail,user,labels);

        ArrayList<String> list = new ArrayList<>();
        list.add(email);

        /* Set Recycler View to Emails Item List */
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //list = Arrays.asList(getResources().getStringArray(R.array.android_versions));
        adapter = new RecyclerAdapter(list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }
    @Override
    public void onStart() {
        super.onStart();

    }

    public static Message getMessage(Gmail service, String userId, String messageId)
            throws IOException {
        Message message = service.users().messages().get(userId, messageId).execute();

        System.out.println("Message snippet: " + message.getSnippet());

        return message;
    }

    public static List<Message> listMessagesWithLabels(Gmail service, String userId,
                                                       List<String> labelIds) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId)
                .setLabelIds(labelIds).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setLabelIds(labelIds)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        for (Message message : messages) {
            System.out.println(message.toPrettyString());
        }

        return messages;
    }



}
