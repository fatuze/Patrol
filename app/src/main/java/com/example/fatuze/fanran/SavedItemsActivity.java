package com.example.fatuze.fanran;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SavedItemsActivity extends AppCompatActivity {

    private ListView lv;
    private List<Map<String, Object>> mData;
    private static final String LOG_TAG = "SavedItemsActivity";

    private final static String ITEM_KEY_NAME      = "name";
    private final static String ITEM_KEY_STATUS    = "status";
    private final static String ITEM_KEY_TIME      = "time";

    private final int ITEM_COUNT = 8;
    private final String array_name[] = {
        "铜牛", "佛香阁", "十七孔桥", "骑凤仙人",
        "镜桥", "知春亭", "石舫", "文昌阁"
    };
    private final int array_status[] = {
            0, 0, 1, 0, 0, 1, 0, 0
    };
    private final String array_time[] = {
        "20160517 10:20", "20160517 10:32", "20160517 10:43", "20160517 16:07",
        "20160518 10:22", "20160518 10:26", "20160519 10:55", "20160519 10:52"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_saved_result);
        mData = getData();
        SavedResultAdapter adapter = new SavedResultAdapter(this);
        adapter.setData(mData);
        lv = (ListView)findViewById(R.id.listView);
        lv.setAdapter(adapter);
    }


    private List<Map<String, Object>> getData()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for(int i=0;i<ITEM_COUNT;i++)
        {
            map = new HashMap<String, Object>();
            map.put(ITEM_KEY_NAME, array_name[i]);
            map.put(ITEM_KEY_STATUS, array_status[i]);
            map.put(ITEM_KEY_TIME, array_time[i]);
            list.add(map);
        }
        return list;
    }



}
