package com.sailing.xphoto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by sailing on 15-3-28.
 */
public class XFragmentPreferences extends Activity {
    private static Logger logger = LoggerFactory.getLogger(XFragmentPreferences.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
    }

    public static class PrefsFragement extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);


            //设置Summary为当前设置的值
            Preference pref = findPreference(getString(R.string.pref_key_filter_file_rule));
            pref.setSummary(sharedPreferences.getString(getString(R.string.pref_key_filter_file_rule), ""));

            pref = findPreference(getString(R.string.pref_key_filter_folder_rule));
            pref.setSummary(sharedPreferences.getString(getString(R.string.pref_key_filter_folder_rule),""));

            pref = findPreference(getString(R.string.pref_key_dist_file_prename));
            pref.setSummary(sharedPreferences.getString(getString(R.string.pref_key_dist_file_prename),""));


            //目录文件夹Summary
            pref = findPreference(getString(R.string.pref_key_move_dist_folder));
            String summary = sharedPreferences.getString(getString(R.string.pref_key_move_dist_folder),"");

            if(summary.isEmpty()) {
                summary = getString(R.string.no_move_file);
            }

            pref.setSummary(summary);


        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference connectionPref = findPreference(key);

            //防止Fragment PrefsFragement not attach 异常
            if(!isAdded()) {
                logger.error("not added!");
                return;
            }

            if(!key.equals(getString(R.string.pref_key_is_recursive))
                    && !key.equals(getString(R.string.pref_key_is_create_date_folder))) {
                String summary = sharedPreferences.getString(key, "");
                if(key.equals(getString(R.string.pref_key_move_dist_folder)))
                {
                    if(sharedPreferences.getString(key, "").isEmpty()) {
                        summary = getString(R.string.no_move_file);
                    }
                }

                connectionPref.setSummary(summary);
            }
        }
    }
}
