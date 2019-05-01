package com.team5.cleenreed;

import android.accounts.Account;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;


//Firebase APIs
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

//Chaquopy
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

//Gmail imports
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
    List<String> a;
    private RecyclerAdapter adapter;
    private List<String> list2;

    /** Global instance of the HTTP transport. */
    //private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    /** Global instance of the JSON factory. */
    //private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private String email;
    private FirebaseAuth mAuth;
    private GoogleSignInAccount mAccount;
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.GMAIL_MODIFY,
    };
    private static final String TAG = "CleenReed";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emails);

        //Fetch Firebase User
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();

        // Get last Signed in Account From Main Activity Sign in
        mAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        email = mAccount.getEmail();

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mCredential.setSelectedAccount(new Account(email,"com.team5.cleenreed"));

        List<String> labels = new ArrayList<>();
        labels.add("IMPORTANT");

        a = new ArrayList<>();

        /* Set Recycler View to Emails Item List */
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        new MakeRequestTask(this,mCredential,"16a6b9c99f4dd272").execute();


        list = Arrays.asList(getResources().getStringArray(R.array.android_versions));
        adapter = new RecyclerAdapter(a);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();

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

    public void addA(String text){
        a.add(text);
        System.out.print(text);
    }

    // Async task to fetch emails
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;
        private EmailsActivity activity;
        private String messageId;
        private String response;

        MakeRequestTask(EmailsActivity activity, GoogleAccountCredential credential, String id) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            this.activity = activity;
            messageId = id;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getDataFromApi() {
            String userId = "me";
            Message message;
            //MimeMessage mimeMessage;
            String response = "";
            try {
                message = getMessage(mService, userId, messageId);
                response = message.getSnippet();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        // Method to get email
        private Message getMessage(Gmail service,
                                   String userId,
                                   String messageId)
                throws IOException {
            // GMail's official method to get email with oauth2.0
            Message message = service.users().messages().get(userId, messageId).execute();
            System.out.println("Message snippet: " + message.getSnippet());
            return message;
        }

        protected void onPostExecute(String result){
            Log.d(TAG, "p execute");
            super.onPostExecute(result);
            EmailsActivity.this.addA(result);
            Log.d(TAG,"done executing");

        }
    }
}
