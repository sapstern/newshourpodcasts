package org.mfri.bbcworldservicenewshourdownloader;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;

public interface BBCWorldServiceDownloaderStaticValues {
    public static final String BBC_PODCAST_DIR = "BBCWorldServicePodcasts";
    public final static long MILLIS_PER_12H = 12 * 60 * 60 * 1000L;
    public final static int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE=0;
    public final static int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE=1;
    public final static int REQUEST_PERMISSION_INTERNET=2;
    public final static int REQUEST_PERMISSION_NETWORK_STATE=3;
    public final static int REQUEST_PERMISSION_WIFI_STATE=4;

   
}
