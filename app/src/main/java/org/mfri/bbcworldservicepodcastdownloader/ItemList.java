package org.mfri.bbcworldservicepodcastdownloader;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class ItemList {

    /**
     * An array of items.
     */
    public  final List<DownloadListItem> ITEMS = new ArrayList<DownloadListItem>();


    public ItemList(Bundle bundle) {
        //Define table header => Empty ListItem
        ITEMS.add(new DownloadListItem("","Content","", "Published", ""));
        if (bundle!=null) {
            int itemListSize = -1;
           try {
                itemListSize = bundle.getInt("LIST_SIZE");
           }
           catch (Exception e){
               e.printStackTrace();
           }
            Log.d("ItemList size: ", String.valueOf(itemListSize));
            for (int i = 0; i < itemListSize; i++) {
                DownloadListItem currentItem =  (DownloadListItem)bundle.get("ITEM_"+i);
                if(currentItem!=null) {
                    Log.d("ItemList CONSTRUCTOR", currentItem.toString());
                    ITEMS.add(currentItem);
                }
                else
                    Log.d("ItemList CONSTRUCTOR", "ITEM_"+i+" is null");
            }
        }

    }




}