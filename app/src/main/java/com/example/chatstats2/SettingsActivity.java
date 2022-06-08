package com.example.chatstats2;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            updateGreyOutSettings(Objects.requireNonNull(getPreferenceManager().getSharedPreferences()));
        }

        @Override
        public void onResume() {
            super.onResume();
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getResources().getString(R.string.dark_theme_settings_key))) {
                updateTheme(sharedPreferences);
            }
            if (key.equals(getResources().getString(R.string.auto_settings_settings_key))) {
                updateGreyOutSettings(sharedPreferences);
            }
        }

        private void updateTheme(SharedPreferences sharedPreferences) {
            AppCompatDelegate.setDefaultNightMode(
                    Integer.parseInt(
                            sharedPreferences.getString(
                                    getResources().getString(R.string.dark_theme_settings_key),
                                    getResources().getString(R.string.DARK_THEME_AUTO)
                            )
                    )
            );
        }

        private void updateGreyOutSettings(SharedPreferences sharedPreferences) {
            boolean greyOut = sharedPreferences.getBoolean(getResources().getString(R.string.auto_settings_settings_key), true);
            Preference datePref = findPreference(getResources().getString(R.string.date_pattern_settings_key));
            Preference timePref = findPreference(getResources().getString(R.string.time_pattern_settings_key));
            assert datePref != null;
            datePref.setShouldDisableView(greyOut);
            datePref.setEnabled(!greyOut);
            assert timePref != null;
            timePref.setShouldDisableView(greyOut);
            timePref.setEnabled(!greyOut);
        }
    }
}
