package com.marnistek.serverstats.Activites;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;
import com.marnistek.serverstats.Support.FileArrayProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KnownHosts extends AppCompatActivity {

    ListView listView;
    SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_known_hosts);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "9");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "known_hosts_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "known_hosts_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
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

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Known Hosts");
        }

        listView = (ListView) findViewById(R.id.known_hosts_listView);
        new processData().execute();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                String host = cursor.getString(cursor.getColumnIndexOrThrow("host"));
                String kh = cursor.getString(cursor.getColumnIndexOrThrow("knownhosts"));
                Intent i = new Intent(KnownHosts.this, KnownHostsData.class);
                i.putExtra("hkey",host);
                i.putExtra("khkey", kh);
                startActivity(i);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor mCursor = (Cursor) listView.getItemAtPosition(pos);
                        String knownhost = mCursor.getString(mCursor.getColumnIndexOrThrow("knownhosts"));
                        String host = mCursor.getString(mCursor.getColumnIndexOrThrow("host"));
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                try {
                                String fileName = "known_hosts.txt";
                                String tempFileName = "known_hosts_temp_file.txt";
                                String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
                                String tempPath = Environment.getExternalStorageDirectory() + "/ServerStats/" + tempFileName;
                                File inputFile = new File(path);
                                File tempFile = new File(tempPath);

                                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                                String currentLine;

                                while ((currentLine = reader.readLine()) != null) {
                                    // trim newline when comparing with lineToRemove
                                    String trimmedLine = currentLine.trim();
                                    if (trimmedLine.equals(knownhost)) {
                                        continue;
                                    }
                                    writer.write(currentLine + System.getProperty("line.separator"));
                                }
                                writer.close();
                                reader.close();
                                boolean successful = tempFile.renameTo(inputFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                                DataBaseAdapter mDbHelper = new DataBaseAdapter(KnownHosts.this);
                                mDbHelper.createDatabase();
                                mDbHelper.open();
                                mDbHelper.deleteFromKnownHosts(knownhost);
                                mDbHelper.deleteInterfaces(host);
                                mDbHelper.close();
                                requery();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }

                    }
                };
                String dialogMessage = "Remove from Known Hosts?";
                AlertDialog.Builder builder = new AlertDialog.Builder(KnownHosts.this);
                builder.setMessage(dialogMessage)
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener);
                builder.show();
                return true;
            }
        });
    }

    public void requery() {

        DataBaseAdapter mDbHelper = new DataBaseAdapter(this); mDbHelper.createDatabase();

        mDbHelper.open();
        Cursor values = mDbHelper.getKnownHosts();
        dataAdapter.changeCursor(values);
        mDbHelper.close();
    }

    private class processData extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;

        @Override
        protected Void doInBackground(String... params) {

            String fileName = "known_hosts.txt";
            String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
            String[] lines = null;
            FileArrayProvider fap = new FileArrayProvider();
            try {
                lines = fap.readLines(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(lines != null) {
                for (String knownhost : lines) {
                    DataBaseAdapter mDbHelper = new DataBaseAdapter(KnownHosts.this);
                    mDbHelper.createDatabase();
                    mDbHelper.open();
                    mDbHelper.updateKnownHosts(knownhost);
                    mDbHelper.close();
                }
            }
            /*
            DataBaseHelper dbHelper = new DataBaseHelper(KnownHosts.this);
            dbHelper.openDataBase();
            dbHelper.deleteKnownHosts();
            dbHelper.insertKnownHosts(lines);
            dbHelper.close();
            */

            return null;
        }

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(KnownHosts.this);
            loadingDialog.setTitle("Getting Known Hosts");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();

        }

        @Override
        protected void onPostExecute(Void result) {

            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            DataBaseAdapter mDbHelper = new DataBaseAdapter(KnownHosts.this);
            mDbHelper.createDatabase();
            mDbHelper.open();

            Cursor cursor = mDbHelper.getKnownHosts();

            // The desired columns to be bound
            String[] columns = new String[] {
                    "host",
                    "knownhosts",
                    "_id"
            };

            // the XML defined views which the data will be bound to
            int[] to = new int[] {
                    R.id.kh_name
            };

            // create the adapter using the cursor pointing to the desired data
            //as well as the layout information

            dataAdapter = new SimpleCursorAdapter(
                    KnownHosts.this,
                    R.layout.known_hosts_list,
                    cursor,
                    columns,
                    to,
                    0);

            listView.setAdapter(dataAdapter);

            mDbHelper.close();
        }

    }

}
