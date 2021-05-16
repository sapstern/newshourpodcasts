package org.mfri.bbcworldservicenewshourdownloader;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;
import androidx.preference.Preference;


public class DirPickerPreference extends Preference implements BBCWorldServiceDownloaderStaticValues{

    Preference.OnPreferenceClickListener clickListener = null;

    /**
     * Constructor to create a preference.
     *
     * @param context The Context this is associated with, through which it can access the
     *                current theme, resources, {@link android.content.SharedPreferences}, etc.
     */
    public DirPickerPreference(Context context) {
        super(context);

        clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {


                final Intent chooserIntent = new Intent(getContext(), BBCDirectoryChooserActivity.class);

                final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                        .newDirectoryName("DownloadRootDirectory")
                        .allowReadOnlyDirectory(true)
                        .allowNewDirectoryNameModification(true)
                        .build();

                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);

// REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
                Bundle theOptions = new Bundle();
                ActivityCompat.startActivityForResult((Activity) context, chooserIntent,REQUEST_DIRECTORY,theOptions);

                return true;
            }
        };
    }



    /**
     * Sets the callback to be invoked when this preference is clicked.
     *
     * @param onPreferenceClickListener The callback to be invoked
     */
    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        super.setOnPreferenceClickListener(clickListener);
    }
}
