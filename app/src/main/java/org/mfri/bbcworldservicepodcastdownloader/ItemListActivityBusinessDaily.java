package org.mfri.bbcworldservicepodcastdownloader;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;


public class ItemListActivityBusinessDaily extends AbstractItemListActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("BUSINESSDAILY_CREATE", "onCreate start");
        super.theProgram = PROGRAM_BUSINESSDAILY;
        utils = BBCWorldServiceDownloaderUtils.getInstance();
        Bundle listBundle = this.getIntent().getExtras().getBundle("RESULT_LIST");

        theItemList = new ItemList(listBundle);
        setContentView(R.layout.activity_item_list);
        super.setupTableLayout(theItemList);

        //add swipe refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            super.startListService(PROGRAM_BUSINESSDAILY, this.getClass().getName());
            pullToRefresh.setRefreshing(false);

        });
        tableLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                ScrollView layMain = findViewById(R.id.table);
                Log.d("SCROLL", "onScrollChanged start Y: "+layMain.getScrollY());
                        if (layMain.getScrollY() == 0)
                            pullToRefresh.setEnabled(true);
                        else
                            pullToRefresh.setEnabled(false);
            }
        });
        Toolbar myToolbar = findViewById(R.id.bbc_toolbar);
        setSupportActionBar(myToolbar);
        Spinner theSpinner = findViewById(R.id.spinner_nav);

        ArrayAdapter<String> bbcProgramsAdapter = new ArrayAdapter<String>(ItemListActivityBusinessDaily.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.bbc_programs));
        bbcProgramsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        theSpinner.setAdapter(bbcProgramsAdapter);
        theSpinner.setSelection(bbcProgramsAdapter.getPosition("BBC World Service Business Daily"));
        theSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(theSpinner.getSelectedItem().toString()){
                    case "BBC World Service Newshour":
                        startListService(PROGRAM_NEWSHOUR, "org.mfri.bbcworldservicepodcastdownloader.ItemListActivityNewshour");
                        break;
                    case "BBC World Service Sportsworld":
                        startListService(PROGRAM_SPORTSWORLD, "org.mfri.bbcworldservicepodcastdownloader.ItemListActivitySportsworld");
                        break;
                    case "BBC World Service Sportshour":
                        startListService(PROGRAM_SPORTSHOUR, "org.mfri.bbcworldservicepodcastdownloader.ItemListActivitySportshour");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_LONG).show();

            }
        });

        Log.d("NEWSHOUR_CREATE", "onCreate end");
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
