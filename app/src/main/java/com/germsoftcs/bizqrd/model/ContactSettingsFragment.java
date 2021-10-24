package com.germsoftcs.bizqrd.model;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.germsoftcs.bizqrd.R;

public class ContactSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.contact_settings_fragment, rootKey);

    }
}