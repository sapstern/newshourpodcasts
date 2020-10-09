package org.mfri.bbcworldservicenewshourdownloader;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ItemList {

    /**
     * An array of sample (dummy) items.
     */
    public  final List<DownloadListItem> ITEMS = new ArrayList<DownloadListItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public  final Map<String, DownloadListItem> ITEM_MAP = new HashMap<String, DownloadListItem>();





    public ItemList(Bundle bundle) {
        //Define table header
        addItem(new DownloadListItem("none","Content","none", "Publication Date", "defaultFileame"));
        if (bundle!=null) {
            int itemListSize = bundle.getInt("LIST_SIZE");
            Log.d("ItemList size: ", String.valueOf(itemListSize));
            for (int i = 0; i < itemListSize; i++) {
                DownloadListItem currentItem =  (DownloadListItem)bundle.get("ITEM_"+i);
                if(currentItem!=null) {
                    Log.d("ItemList CONSTRUCTOR", currentItem.toString());
                    addItem(currentItem);
                }
                else
                    Log.d("ItemList CONSTRUCTOR", "ITEM_"+i+" is null");
            }
        }

    }

    private  void addItem(DownloadListItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }



}