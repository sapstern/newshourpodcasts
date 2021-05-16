package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Intent;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

public class BBCDirectoryChooserActivity extends DirectoryChooserActivity implements BBCWorldServiceDownloaderStaticValues{
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                //handleDirectoryChoice(data
                  //      .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
            } else {
                // Nothing selected
            }
        }
    }
}
