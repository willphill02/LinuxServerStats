package com.marnistek.serverstats.Activites;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;

public class Licences extends AppCompatActivity {

    private static SimpleCursorAdapter dataAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licences);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "12");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "licences_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "licences_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Licences");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        DataBaseAdapter mDbHelper = new DataBaseAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor cursor = mDbHelper.getLicences();

        // The desired columns to be bound
        String[] columns = new String[] {
                "name",
                "author",
                "licence",
                "_id"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.licence_name,
                R.id.licence_author,
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information

        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.licence_info,
                cursor,
                columns,
                to,
                0);

        mDbHelper.close();

        listView = (ListView) findViewById(R.id.licences_listView);

        if (listView != null) {
            listView.setAdapter(dataAdapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String licence = cursor.getString(cursor.getColumnIndexOrThrow("licence"));

                Intent i = new Intent(Licences.this, LicenceInfo.class);
                i.putExtra("nkey",name);
                i.putExtra("lkey",licence);
                startActivity(i);



            }

        });

    }

}
