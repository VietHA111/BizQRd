package com.germsoftcs.bizqrd.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.germsoftcs.bizqrd.model.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_QR_TYPE = "qrCodeType";
    public static final String KEY_NAME_PREF = "pref";
    public static final String KEY_NAME_GIVEN = "given";
    public static final String KEY_NAME_MIDDLE = "middle";
    public static final String KEY_NAME_FAMILY = "family";
    public static final String KEY_NAME_SUFF = "suff";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NICKNAME = "nick";
    public static final String KEY_ORG = "org";
    public static final String KEY_DEPT = "dept";
    public static final String KEY_JOB = "job";
    public static final String KEY_POSTAL = "postal";
    public static final String KEY_NOTES = "notes";
    public static final String[] PREFERENCE_KEYS = {
            KEY_QR_TYPE,
            KEY_NAME_PREF,
            KEY_NAME_GIVEN,
            KEY_NAME_MIDDLE,
            KEY_NAME_FAMILY,
            KEY_NAME_SUFF,
            KEY_EMAIL,
            KEY_NICKNAME,
            KEY_ORG,
            KEY_DEPT,
            KEY_JOB,
            KEY_POSTAL,
            KEY_NOTES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}