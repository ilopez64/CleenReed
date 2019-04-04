package com.team5.cleenreed;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;

import java.util.Arrays;

public class Tab2Important extends Fragment {

    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {GmailScopes.GMAIL_READONLY};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2important, container, false);

        mCredential = GoogleAccountCredential.usingOAuth2(getContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());


        return rootView;
    }

}
