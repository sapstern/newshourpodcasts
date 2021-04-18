package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    BBCWorldServiceDownloaderUtils utils = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
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


        Button theBackButton = findViewById(R.id.settings_back_button);
        theBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SETTINGS_ACTION", "onClick start");
                startMainActivity();
            }
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

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), ItemMainActivity.class);
        startActivity(intent);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}