package org.mfri.bbcworldservicenewshourdownloader;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class DownloadListItem implements Parcelable {
        public final String id;
        public final String content;
        public final String url;
        public final String dateOfPublication;
        public final String fileName;



        public DownloadListItem(String id, String content, String url, String dateOfPublication, String fileName) {
            this.id = id;
            this.content = content;
            this.url = url;
            this.dateOfPublication = dateOfPublication;
            this.fileName = fileName;

        }

        protected DownloadListItem(Parcel in) {
            id = in.readString();
            content = in.readString();
            url = in.readString();
            dateOfPublication = in.readString();
            fileName = in.readString();

        }

        public static final Creator<DownloadListItem> CREATOR = new Creator<DownloadListItem>() {
            @Override
            public DownloadListItem createFromParcel(Parcel in) {
                return new DownloadListItem(in);
            }

            @Override
            public DownloadListItem[] newArray(int size) {
                return new DownloadListItem[size];
            }
        };

        @Override
        public String toString() {
            return id+" "+content+" "+url+" "+dateOfPublication+" "+fileName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(id);
            parcel.writeString(content);
            parcel.writeString(url);
            parcel.writeString(dateOfPublication);
            parcel.writeString(fileName);

        }
}

