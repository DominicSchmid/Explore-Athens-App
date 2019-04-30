package com.ea.exploreathens;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SeekBarPreference;
import android.view.MenuItem;

public class MySettings extends PreferenceFragmentCompat {

    private SeekBarPreference seekBarRadar;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                String key = preference.getKey();
                switch(key){
                    case "enable_radar":

                        break;

                }
                // TODO if prefence is switchbutton for radar
/*
I glab es isch folsch wenn man probiert in do AppCompactSettingsActivity irgendwelche settings zu ändern
Iwo isch gstonn dass es depracated isch und man fragments benutzen soll deswegen geats wenn mo unten
in fragment des mitn Slider mochn obo es geat net wenn mans do probiert.
 */
                if(preference.isEnabled()){

                    //getPreferenceScreen().findPreference("list box preference key").setEnabled(isEnabled);
                    //getPreferenceScreen().findPreference("list box preference key").setEnabled(!isEnabled);
                }

                // For all other preferences, set the summary to the values
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setHasOptionsMenu(true);


        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(findPreference("language"));


    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        seekBarRadar = (SeekBarPreference) findPreference("seek_bar_radar"); //Preference Key
    }


    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }






    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    /*private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }*/


    @SuppressWarnings("deprecation") // TODO this is depracated we should really fix
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LanguagePreferenceFragment extends PreferenceFragment {

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }



}
