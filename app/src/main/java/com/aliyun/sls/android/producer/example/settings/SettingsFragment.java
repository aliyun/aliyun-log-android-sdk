package com.aliyun.sls.android.producer.example.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.aliyun.sls.android.producer.example.R;

/**
 * @author gordon
 * @date 2021/07/26
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}
