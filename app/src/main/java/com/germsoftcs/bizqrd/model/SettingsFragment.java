package com.germsoftcs.bizqrd.model;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.germsoftcs.bizqrd.R;
import com.germsoftcs.bizqrd.activities.MainActivity;
import com.germsoftcs.bizqrd.activities.SettingsActivity;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setVisibility(getPreferenceManager().getSharedPreferences());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(SettingsActivity.KEY_QR_TYPE)) {
            setVisibility(sharedPreferences);
            MainActivity.Companion.reset();
        }
    }

    private void setVisibility(SharedPreferences sharedPreferences) {
        PreferenceScreen root = getPreferenceScreen();
        boolean value = sharedPreferences.getString(SettingsActivity.KEY_QR_TYPE, " ").equals("Contact");
        for (int i = 1; i < 13; i++) {
            findPreference(SettingsActivity.PREFERENCE_KEYS[i]).setVisible(value);
        }
    }
}