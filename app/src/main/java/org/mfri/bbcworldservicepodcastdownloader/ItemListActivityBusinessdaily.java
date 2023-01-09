package org.mfri.bbcworldservicepodcastdownloader;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;


public class ItemListActivityBusinessdaily extends AbstractItemListActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("BUSINESSDAILY_CREATE", "onCreate start");
        super.setupLayout(PROGRAM_BUSINESSDAILY);
        Log.d("BUSINESSDAILY_CREATE", "onCreate end");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }




    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("IMPLICIT_INTENT_START_PODCAST"));
    }

    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.d("SAVE_STATE", "onSaveInstanceState start");
        savedInstanceState.putParcelableArrayList("ITEM_LIST", (ArrayList<? extends Parcelable>) theItemList.ITEMS);
        Log.d("SAVE_STATE", "onSaveInstanceState exit");
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true); // exist app
    }


}
