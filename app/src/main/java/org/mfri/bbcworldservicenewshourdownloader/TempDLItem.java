package org.mfri.bbcworldservicenewshourdownloader;

import java.util.Date;

public class TempDLItem implements Comparable<TempDLItem> {
        public  String id;
        public  String content;
        public  String url;
        public  String dateOfPublication;
        public  String fileName;
        public Date compareDate;


        public TempDLItem(String id, String content, String url, String dateOfPublication, String fileName) {
            this.id = id;
            this.content = content;
            this.url = url;
            this.dateOfPublication = dateOfPublication;
            this.fileName = fileName;

        }


    @Override
    public int compareTo(TempDLItem tempDLItem) {
        if (this.compareDate == null || tempDLItem.compareDate == null)
            return 0;
        return tempDLItem.compareDate.compareTo(this.compareDate);
    }
}

