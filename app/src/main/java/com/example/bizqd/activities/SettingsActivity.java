package com.example.bizqd.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.bizqd.model.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_NAME_PREF = "pref";
    public static final String KEY_NAME_GIVEN = "given";
    public static final String KEY_NAME_MIDDLE = "middle";
    public static final String KEY_NAME_FAMILY = "family";
    public static final String KEY_NAME_SUFF = "suff";
    public static final String KEY_NICKNAME = "nick";
    public static final String KEY_ORG = "org";
    public static final String KEY_DEPT = "dept";
    public static final String KEY_JOB = "job";
    public static final String KEY_POSTAL = "postal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}