package org.mfri.bbcworldservicepodcastdownloader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.util.Iterator;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    BBCWorldServiceDownloaderUtils utils = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        utils = BBCWorldServiceDownloaderUtils.getInstance();

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_init_settings", true)==false
         && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_settings", false)==false){
            startMainActivity();
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("show_settings", false).apply();
        editor.putBoolean("show_init_settings", false).apply();
        editor.commit();
        //printMap (prefs.getAll());

        Button theBackButton = findViewById(R.id.settings_back_button);
        theBackButton.setOnClickListener(view -> {
            Log.d("SETTINGS_ACTION", "onClick start");
            //if user changed background download behavior
            utils.processChoosenDownloadOptions(getApplicationContext());
            startMainActivity();
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Log.d("MAP", "Entry "+""+pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), ItemMainActivity.class);
        startActivity(intent);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //First, lets check whether we have initialized preferences already
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String selectedDir = prefs.getString("dl_dir_root", "INIT");

            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            PreferenceScreen root = getPreferenceScreen();
            ListPreference  dlDirRoot = root.findPreference("dl_dir_root");
            CharSequence[] arryValues = BBCWorldServiceDownloaderUtils.getStoragePaths(getContext());
            CharSequence[] arryEntrys = BBCWorldServiceDownloaderUtils.getStoragePaths(getContext());
            dlDirRoot.setEntries(arryEntrys);
            dlDirRoot.setEntryValues(arryValues);


            if(selectedDir.equals("INIT")) {
                //Initialize value/index
                dlDirRoot.setDefaultValue(arryValues[0]);
                dlDirRoot.setValueIndex(0);
            }
            else{
                //Position at already selected value/index
                dlDirRoot.setDefaultValue(selectedDir);
                dlDirRoot.setValue(selectedDir);
                dlDirRoot.setValueIndex(dlDirRoot.findIndexOfValue(selectedDir));

            }
        }
    }
}