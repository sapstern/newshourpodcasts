package org.mfri.bbcworldservicenewshourdownloader.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mfri.bbcworldservicenewshourdownloader.DownloadListItem;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bbcdownloaditems.db";
    public static final String TABLE_NAME = "bbcdownload_items";
    public static final String COL_1 = "ITEM_ID";
    public static final String COL_2 = "ITEM_CONTENT";
    public static final String COL_3 = "ITEM_URL";
    public static final String COL_4 = "ITEM_FILE_NAME";
    public static final String COL_5 = "ITEM_DATE_OF_PUB";
    public static final String TABLE_CREATE = "CREATE TABLE "+TABLE_NAME+" ("+COL_1+" INTEGER PRIMARY KEY, "+COL_2+" TEXT, "+COL_3+" TEXT, "+COL_4+" TEXT, "+COL_5+" TEXT) ";

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE "+TABLE_NAME);

    }

    public boolean addItem(DownloadListItem item){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, item.id);
        contentValues.put(COL_2, item.content);
        contentValues.put(COL_3, item.dateOfPublication);
        contentValues.put(COL_4, item.fileName);
        contentValues.put(COL_5, item.url);

        if( this.getWritableDatabase().insert(TABLE_NAME, null, contentValues) == -1 )
            return false;
        return true;
    }
}
