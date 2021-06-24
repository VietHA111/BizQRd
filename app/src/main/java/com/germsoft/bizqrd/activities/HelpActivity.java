package com.germsoft.bizqrd.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.germsoft.bizqrd.R;

public class HelpActivity extends AppCompatActivity {

    Button privacyButton, supportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        privacyButton = findViewById(R.id.PrivacyButton);
        privacyButton.setOnClickListener(v -> openSite());

        supportButton = findViewById(R.id.SupportButton);
        supportButton.setOnClickListener(v -> openSupport());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    //EFFECTS: open app website
    private void openSite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bizqrd.app"));
        startActivity(browserIntent);
    }

    //EFFECTS: open support tab of app website
    private void openSupport() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bizqrd.app/contact-form/"));
        startActivity(browserIntent);
    }
}