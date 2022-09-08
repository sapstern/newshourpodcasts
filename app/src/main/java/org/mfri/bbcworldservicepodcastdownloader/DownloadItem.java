package org.mfri.bbcworldservicepodcastdownloader;

import java.util.Date;

public class DownloadItem implements Comparable<DownloadItem> {
        public  String id;
        public  String content;
        public  String url;
        public  String dateOfPublication;
        public  String fileName;
        public Date compareDate;

        public DownloadItem(){

        }

        public DownloadItem(String id, String content, String url, String dateOfPublication, String fileName) {
            this.id = id;
            this.content = content;
            this.url = url;
            this.dateOfPublication = dateOfPublication;
            this.fileName = fileName;

        }


    @Override
    public int compareTo(DownloadItem downloadItem) {
        if (this.compareDate == null || downloadItem.compareDate == null)
            return 0;
        return downloadItem.compareDate.compareTo(this.compareDate);
    }
}

