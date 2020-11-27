package com.datafrey.cooltimer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.datafrey.cooltimer.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.timer_preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();

        int preferenceCount = preferenceScreen.getPreferenceCount();
        for (int preferenceIndex = 0; preferenceIndex < preferenceCount; preferenceIndex++) {
            Preference preference = preferenceScreen.getPreference(preferenceIndex);
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceLabel(preference, value);
            }
        }

        Preference defaultIntervalPreference = findPreference("default_interval");
        defaultIntervalPreference.setOnPreferenceChangeListener(this);
    }

    private void setPreferenceLabel(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int valueIndex = listPreference.findIndexOfValue(value);
            if (valueIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[valueIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (!(preference instanceof CheckBoxPreference)) {
            String value = sharedPreferences.getString(preference.getKey(), "");
            setPreferenceLabel(preference, value);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("default_interval")) {
            String defaultIntervalString = newValue.toString();
            try {
                Integer.parseInt(defaultIntervalString);
            } catch (NumberFormatException nfe) {
                Toast.makeText(
                        getContext(),
                        getString(R.string.default_interval_string_not_integer_number_error_message),
                        Toast.LENGTH_LONG
                ).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
