package org.mfri.bbcworldservicepodcastdownloader;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class SwitchPreferenceConfirm extends SwitchPreferenceCompat {

    private boolean isChecked = false;

    public SwitchPreferenceConfirm(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SwitchPreferenceConfirm(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwitchPreferenceConfirm(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPreferenceConfirm(@NonNull Context context) {
        super(context);
    }
    @Override
    public void setChecked(boolean checked){

        if (checked && PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("dl_background", false)==false){
            sendBackupDownloadPopupToConfirm(getContext());
            super.setChecked(isChecked);
        }
        super.setChecked(checked);

    }

    private void sendBackupDownloadPopupToConfirm(Context theContext){
        AlertDialog.Builder builder = new AlertDialog.Builder(theContext);
        builder.setCancelable(true);
        builder.setTitle("Warning!");
        builder.setMessage("Enabling background download is not recommended as it might fill your disk!");
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    isChecked = true;

                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            isChecked = false;
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
