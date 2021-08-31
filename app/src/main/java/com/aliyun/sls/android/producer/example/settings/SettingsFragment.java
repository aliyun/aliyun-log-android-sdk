package com.aliyun.sls.android.producer.example.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.aliyun.sls.android.producer.example.R;

import java.util.Map;

/**
 * @author gordon
 * @date 2021/07/26
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        updateSummary(getPreferenceScreen());
//        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
//            if (getPreferenceScreen().getPreference(i) instanceof EditTextPreference) {
//                updateSummary((EditTextPreference) getPreferenceScreen().getPreference(i));
//            }
//        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Map<String, ?> preferencesMap = sharedPreferences.getAll();

        if (preferencesMap.get(key) instanceof EditTextPreference) {
            updateSummary((EditTextPreference) preferencesMap.get(key));
        }
    }

    private void updateSummary(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            for (int i = 0; i < ((PreferenceCategory) preference).getPreferenceCount(); i++) {
                updateSummary(((PreferenceCategory) preference).getPreference(i));
            }
        } else if (preference instanceof PreferenceScreen) {
            for (int i = 0; i < ((PreferenceScreen) preference).getPreferenceCount(); i++) {
                updateSummary(((PreferenceScreen) preference).getPreference(i));
            }
        } else if (preference instanceof EditTextPreference) {
            updateSummary((EditTextPreference) preference);
        }
    }

    private void updateSummary(EditTextPreference preference) {
        preference.setSummary(preference.getText());
    }
}
