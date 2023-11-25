package org.mfri.bbc.mediamanager;

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
        isChecked = false;
        if (checked){
            sendBackupDownloadPopupToConfirm(getContext());
            //isChecked = checked;
            super.setChecked(isChecked);
        }
        super.setChecked(false);
    }

    private void sendBackupDownloadPopupToConfirm(Context theContext){

        AlertDialog.Builder builder = new AlertDialog.Builder(theContext);
        builder.setCancelable(false);
        builder.setTitle(R.string.bg_warning);
        builder.setMessage(R.string.bg_confirm_message);
        builder.setPositiveButton(R.string.bg_confirm, (dialog, which) -> {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("dl_background", true).apply();
            isChecked = true;
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("dl_background", false).apply();
            isChecked = false;
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
