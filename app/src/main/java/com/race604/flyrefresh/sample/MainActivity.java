package com.race604.flyrefresh.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.race604.flyrefresh.FlyRefreshLayout;


public class MainActivity extends AppCompatActivity {

    private FlyRefreshLayout mFlylayout;
    private ListView mListView;

    private static final String[] INIT_DATA = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
            "Nine", "Ten"};

    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFlylayout = (FlyRefreshLayout) findViewById(R.id.fly_layout);
        mListView = (ListView) findViewById(R.id.list);

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mAdapter.addAll(INIT_DATA);
        mAdapter.addAll(INIT_DATA);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
