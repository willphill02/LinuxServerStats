package com.marnistek.serverstats.Activites;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;
import com.marnistek.serverstats.Support.RateThisApp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import it.sephiroth.android.library.tooltip.Tooltip;

public class ServerSelector extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 55;
    private static SimpleCursorAdapter dataAdapter;
    ListView listView;
    FloatingActionButton addServer;
    AlertDialog.Builder builder;
    SharedPreferences prefs = null;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selector);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "ServerSelector_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ServerSelector_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        prefs = getSharedPreferences("com.marnistek.serverstats", MODE_PRIVATE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-4291542188052451~8599800529");

        if(Build.VERSION.SDK_INT >= 6){

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        }

        NativeExpressAdView adView = (NativeExpressAdView)findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("5B89614DE79F35A3EA14274322860334")
                .build();
        if (adView != null) {
            adView.loadAd(request);
        }


        listView = (ListView) findViewById(R.id.listView);
        addServer = (FloatingActionButton) findViewById(R.id.addBtn);
        if (addServer != null) {
            addServer.setOnClickListener(this);
        }
        final boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
        if(firstrun) {
            Tooltip.make(this,
                    new Tooltip.Builder(101)
                            .anchor(addServer, Tooltip.Gravity.BOTTOM)
                            .closePolicy(new Tooltip.ClosePolicy()
                                    .insidePolicy(true, false)
                                    .outsidePolicy(true, false), 0)
                            .activateDelay(800)
                            .showDelay(300)
                            .withStyleId(R.style.ToolTipLayoutCustomStyle)
                            .text("Click here to add a new server")
                            .maxWidth(500)
                            .withArrow(true)
                            .withOverlay(false)
                            .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                            .build()
            ).show();
            new AlertDialog.Builder(this)
                    .setTitle("Welcome")
                    .setMessage(R.string.welcome_msg)
                    //.setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do Nothing
                        }
                    }).create().show();

        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit()
                .putBoolean("firstrun", false)
                .apply();

        displayListView();
        dataAdapter.notifyDataSetChanged();



    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.addBtn:
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "add_server_button_pressed");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "add_server_button_pressed");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if(listView.getCount() <= 1){
                    Intent i = new Intent(this, AddNewServer.class);
                    startActivity(i);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Get Pro Version")
                            .setMessage(R.string.get_pro_add_server_msg)
                            //.setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Do Nothing
                                }
                            }).create().show();
                }
        }

    }

    private void displayListView() {

        DataBaseAdapter mDbHelper = new DataBaseAdapter(ServerSelector.this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor cursor = mDbHelper.getServerData();

        // The desired columns to be bound
        String[] columns = new String[] {
                "name",
                "displayName",
                "os",
                "host",
                "username",
                "password",
                "port",
                "_id"
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                R.id.name,
                R.id.host,
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information

        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.server_info,
                cursor,
                columns,
                to,
                0);

        mDbHelper.close();


        final ListView listView = (ListView) findViewById(R.id.listView);
        // Assign adapter to ListView
        if (listView != null) {
            listView.setAdapter(dataAdapter);
        }

        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> listView, View view,
                                        int position, long id) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "2");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "server_selected");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "server_selected");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    AlertDialog.Builder builder;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    ActivityCompat.requestPermissions(ServerSelector.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //Do nothing
                                    break;
                            }
                        }
                    };
                    String dialogMessage = "This app requires write access to your external storage so that it can store the ssh known_hosts file and other files storing bandwidth data from your servers. This app will not work without this permission. \n\nDo you wish to proceed?";
                    builder = new AlertDialog.Builder(ServerSelector.this);
                    builder.setMessage(dialogMessage)
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener);

                    if(Build.VERSION.SDK_INT >= 6 && ContextCompat.checkSelfPermission(ServerSelector.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ServerSelector.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            builder.show();

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(ServerSelector.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    } else {
                        //Create known_hosts if it doesn't already exist
                        File direct = new File(Environment.getExternalStorageDirectory()
                                + "/ServerStats/");
                        if (!direct.exists()) {
                            direct.mkdirs();
                        }
                        String fileName = "known_hosts.txt";
                        String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
                        File targetFile = new File(path);
                        if(!targetFile.exists()) {
                            try {
                                FileOutputStream fos = new FileOutputStream(targetFile);
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                                String data = "";
                                bw.write(data);
                                bw.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("known_hosts error", "ERROR");
                            }
                        }

                        // Get the cursor, positioned to the corresponding row in the result set
                        Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                        // Get server info from this row in the database.
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        String os = cursor.getString(cursor.getColumnIndexOrThrow("os"));
                        String host = cursor.getString(cursor.getColumnIndexOrThrow("host"));
                        String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                        String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                        String port = cursor.getString(cursor.getColumnIndexOrThrow("port"));

                        //Check if host is in known hosts table, if not insert it
                        DataBaseAdapter mDbHelper = new DataBaseAdapter(ServerSelector.this);
                        mDbHelper.createDatabase();
                        mDbHelper.open();
                        Cursor getKnownHosts = mDbHelper.checkKnownHosts(host);
                        int count = getKnownHosts.getCount();
                        if(count == 0){
                            mDbHelper.insertHostIntoKnownHosts(host);
                        }
                        mDbHelper.close();

                        //Start Install / Main Activity
                        Intent i = new Intent(ServerSelector.this, InstallVNStat.class);
                        i.putExtra("nkey",name);
                        i.putExtra("okey", os);
                        i.putExtra("hkey", host);
                        i.putExtra("ukey", username);
                        i.putExtra("pkey", password);
                        i.putExtra("ptkey",port);
                        startActivity(i);
                    }

                }
            });
        }

        if (listView != null) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "3");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "server_long_clicked");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "server_long_clicked");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    final CharSequence[] items = {"Edit Server", "Delete Server"};

                    AlertDialog.Builder builderList = new AlertDialog.Builder(ServerSelector.this);
                    builderList.setTitle("Edit/Delete Server");
                    builderList.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if(item == 0){
                                Cursor cursor = (Cursor) listView.getItemAtPosition(pos);
                                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                                String os = cursor.getString(cursor.getColumnIndexOrThrow("os"));
                                String host = cursor.getString(cursor.getColumnIndexOrThrow("host"));
                                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                                String port = cursor.getString(cursor.getColumnIndexOrThrow("port"));
                                Intent i = new Intent(ServerSelector.this, EditServer.class);
                                i.putExtra("nkey",name);
                                i.putExtra("okey", os);
                                i.putExtra("hkey", host);
                                i.putExtra("ukey", username);
                                i.putExtra("pkey", password);
                                i.putExtra("ptkey",port);
                                startActivity(i);
                            } else if (item == 1){
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


                                        Cursor cursor = (Cursor) listView.getItemAtPosition(pos);
                                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                                        String host = cursor.getString(cursor.getColumnIndexOrThrow("host"));
                                        String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                                        String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:

                                                DataBaseAdapter mDbHelper = new DataBaseAdapter(ServerSelector.this);
                                                mDbHelper.createDatabase();
                                                mDbHelper.open();
                                                mDbHelper.deleteServer(name,host,username,password);
                                                mDbHelper.close();
                                                requery();
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                };
                                String dialogMessage = "Delete Server?";
                                builder = new AlertDialog.Builder(ServerSelector.this);
                                builder.setMessage(dialogMessage)
                                        .setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener);
                                builder.show();
                            }
                        }
                    });
                    AlertDialog alert = builderList.create();
                    alert.show();

                    return true;
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        requery();
    }

    public void requery() {

        DataBaseAdapter mDbHelper = new DataBaseAdapter(this); mDbHelper.createDatabase();

        mDbHelper.open();
        Cursor values = mDbHelper.getServerData();
        dataAdapter.changeCursor(values);
        mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server_selector, menu);
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
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        }
        if (id == R.id.action_known_hosts) {
            startActivity(new Intent(this, KnownHosts.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permissionsGranted = true;
                    //task.execute(command);



                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
