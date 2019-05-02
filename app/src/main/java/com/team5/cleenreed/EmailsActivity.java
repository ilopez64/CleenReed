package com.team5.cleenreed;

import android.accounts.Account;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


//Firebase APIs
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.model.MessagePart;
import com.google.firebase.auth.FirebaseAuth;

//Chaquopy

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

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static android.util.Base64.DEFAULT;

public class EmailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> list;
    static List<String> a;
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

        list = Arrays.asList(getResources().getStringArray(R.array.emailIDs));
        new MakeRequestTask(this,mCredential,labels).execute();

        //a.add(email);
        list = Arrays.asList(getResources().getStringArray(R.array.android_versions));

        //Python API start up
        /*if (!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject txtR = py.getModule("txtR");
        txtR.callAttr("main");
        */
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    public void addA(String text){
        EmailsActivity.a.add(text);
        System.out.print(text);
    }

    // Async task to fetch emails
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;
        private EmailsActivity activity;
        private List<String> label;
        private List<Message> messages = new ArrayList<>();
        private List<String> res = new ArrayList<>();
        private List<MimeMessage> mimeMessages = new ArrayList<>();
        String userId = "me";

        MakeRequestTask(EmailsActivity activity, GoogleAccountCredential credential, List<String> labels) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            this.activity = activity;
            label = labels;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                messages = getDataFromAPI();
                for (int i = 0; i < 10; i++){
                    Message message1 = getMessage(mService, userId, messages.get(i).getId());
                    byte[] emailBytes = Base64.decode(message1.getPayload().getParts().get(0).getBody().getData().trim().toString(),DEFAULT);
                    String body = new String(emailBytes,"UTF-8");
                    //Properties props = new Properties();
                    //Session session = Session.getDefaultInstance(props, null);

                    //MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
                    //mimeMessages.add(email);
                    System.out.println(body);
                    res.add(message1.getSnippet());
                }

                return res;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<Message> getDataFromAPI() throws IOException {
            List<Message> messages = new ArrayList<>();
            try {
                messages = getImportant(mService, userId, label);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return messages;
        }

        // Method to return all emails with IMPORTANT label
        private List<Message> getImportant(Gmail service,
                                          String userId,
                                          List<String> labelIds)
                throws IOException {
            ListMessagesResponse responses = service.users().messages().list(userId)
                    .setLabelIds(labelIds).execute();
            while (responses.getMessages() != null) {
                messages.addAll(responses.getMessages());
                if (responses.getNextPageToken() != null) {
                    String pageToken = responses.getNextPageToken();
                    responses = service.users().messages().list(userId).setLabelIds(labelIds)
                            .setPageToken(pageToken).execute();
                } else {
                    break;
                }
            }
            return messages;
        }

        // Method to get email
        private Message getMessage(Gmail service,
                                   String userId,
                                   String messageId)
                throws IOException {
            // GMail's official method to get email with oauth2.0
            Message message = service.users().messages().get(userId, messageId).execute();
            return message;
        }

        // Method to get email content
        /*private String getContent(Message message) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                getPlainTextFromMessageParts(message.getPayload().getParts(), stringBuilder);
                byte[] bodyBytes = Base64.decode(stringBuilder.toString(),DEFAULT);
                String text = new String(bodyBytes, "UTF-8");
                return text;
            } catch (UnsupportedEncodingException e) {
                //logger.error("UnsupportedEncoding: " + e.toString());
                return message.getSnippet();
            }
        }*/

        // method to get email body
        /*private void getPlainTextFromMessageParts(List<MessagePart> messageParts, StringBuilder stringBuilder) {
            for (MessagePart messagePart : messageParts) {
                if (messagePart.getMimeType().equals("text/plain")) {
                    stringBuilder.append(messagePart.getBody().getData());
                }

                if (messagePart.getParts() != null) {
                    getPlainTextFromMessageParts(messagePart.getParts(), stringBuilder);
                }
            }
        }*/

        @Override
        protected void onPostExecute(List<String> result){
            Log.d(TAG, "p execute");
            for (String num : result) {
                System.out.println(num);
                addA(num);
            }
            adapter = new RecyclerAdapter(a);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            Log.d(TAG,"done executing");

        }
    }
}
