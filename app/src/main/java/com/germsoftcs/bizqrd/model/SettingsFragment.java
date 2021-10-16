package com.germsoftcs.bizqrd.model;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.germsoftcs.bizqrd.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

    }
}