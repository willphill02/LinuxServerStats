package com.marnistek.serverstats.Activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.Fragments.ChartFragmentDays;
import com.marnistek.serverstats.Fragments.ChartFragmentHours;
import com.marnistek.serverstats.Fragments.ChartFragmentMonths;
import com.marnistek.serverstats.Fragments.ServerInfoFragment;
import com.marnistek.serverstats.Fragments.SummaryFragment;
import com.marnistek.serverstats.R;
import com.marnistek.serverstats.SSH.SSHGetData;
import com.marnistek.serverstats.SSH.SSHGetInterfaceData;
import com.marnistek.serverstats.Support.FileArrayProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    String name;
    String os;
    String host;
    String username;
    String password;
    String port;
    String fragTag;
    String timeout;
    int spinnerPosition;
    boolean userIsInteracting;
    Spinner interfaceSpinner;
    SummaryFragment summaryFragment;
    ChartFragmentHours chartFragmentHours;
    ChartFragmentDays chartFragmentDays;
    ChartFragmentMonths chartFragmentMonths;
    ServerInfoFragment serverInfoFragment;
    TextView navServerName;
    ProgressDialog progressDialog;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "13");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "MainActivity_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MainActivity_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("MAINACTIVITY","Activity created");

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        String timeoutSecs = SP.getString("timeoutSecs","0");
        if(isNumeric(timeoutSecs)){
            timeout = timeoutSecs;
        } else {
            timeout = "0";
        }

        progressDialog =  new ProgressDialog(this);
        progressDialog.setTitle("Gathering Data");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        summaryFragment = new SummaryFragment();
        chartFragmentHours = new ChartFragmentHours();
        chartFragmentDays = new ChartFragmentDays();
        chartFragmentMonths = new ChartFragmentMonths();
        serverInfoFragment = new ServerInfoFragment();
        //upTimeFragment = new UpTimeFragment();


        name = getIntent().getStringExtra("nkey");
        os = getIntent().getStringExtra("okey");
        host = getIntent().getStringExtra("hkey");
        username = getIntent().getStringExtra("ukey");
        password = getIntent().getStringExtra("pkey");
        port = getIntent().getStringExtra("ptkey");
        fragTag = getIntent().getStringExtra("fkey");
        spinnerPosition = getIntent().getIntExtra("ikey",0);
        userIsInteracting = false;

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawerLayout != null) {
            drawerLayout.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        View headerView = null;
        if (navigationView != null) {
            headerView = navigationView.inflateHeaderView(R.layout.nav_header_data);
        }
        if (headerView != null) {
            interfaceSpinner = (Spinner) headerView.findViewById(R.id.interface_spinner_nav);
            navServerName = (TextView) headerView.findViewById(R.id.txt_nav_server_name);
            navServerName.setText(name);
        }

        DataBaseAdapter mDbHelper = new DataBaseAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();
        Cursor getData = mDbHelper.getInterfaceData(host);
        int rows = getData.getCount();
        List<String> ifceList = new ArrayList<>();
        getData.moveToFirst();
        if(rows == 0){
            if (savedInstanceState == null){
                if(username.equals("root")) {
                    new getNetworkInterfaces().execute(username, password, host, port, "ip link show", timeout);
                } else {
                    new getNetworkInterfaces().execute(username, password, host, port, "PATH=/sbin:/usr/sbin:$PATH ip link show", timeout);
                }
            }
        } else {
            if (savedInstanceState == null) {
                while (!getData.isAfterLast()) {
                    String ifce = getData.getString(getData.getColumnIndexOrThrow("interface"));
                    ifceList.add(ifce);
                    Log.d("TESTING", ifce);
                    getData.moveToNext();
                }
                new runBackgroundTask().execute(username, password, host, port, timeout);
            }
            String ifceArray[] = ifceList.toArray(new String[ifceList.size()]);
            setSpinnerArray(ifceArray);
            if (interfaceSpinner != null) {
                interfaceSpinner.setSelection(spinnerPosition,false);
                interfaceSpinner.setOnItemSelectedListener(this);
            }
        }
        mDbHelper.close();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
                this.finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.
        String ifce = interfaceSpinner.getSelectedItem().toString();
        switch (menuItem.getItemId()) {
            case R.id.nav_summary:
                Bundle bundleA = new Bundle();
                bundleA.putString(FirebaseAnalytics.Param.ITEM_ID, "14");
                bundleA.putString(FirebaseAnalytics.Param.ITEM_NAME, "SummaryFragment_opened");
                bundleA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SummaryFragment_opened");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleA);
                menuItem.setChecked(true);
                Bundle bundleS = new Bundle();
                bundleS.putString("nkey", name );
                bundleS.putString("hkey", host);
                bundleS.putString("ikey", ifce);
                setFragment(summaryFragment,bundleS,"SUMMARY");
                drawerLayout.closeDrawer(GravityCompat.START);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Summary");
                }
                return true;
            case R.id.nav_hours:
                Bundle bundleAH = new Bundle();
                bundleAH.putString(FirebaseAnalytics.Param.ITEM_ID, "15");
                bundleAH.putString(FirebaseAnalytics.Param.ITEM_NAME, "chartFragmentHours_opened");
                bundleAH.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chartFragmentHours_opened");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleAH);

                menuItem.setChecked(true);
                Bundle bundleH = new Bundle();
                bundleH.putString("key", "h");
                bundleH.putString("ikey", ifce);
                bundleH.putString("hkey", host);
                setFragment(chartFragmentHours,bundleH,"HOURS_CHART");
                drawerLayout.closeDrawer(GravityCompat.START);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.chart_header_hours);
                }
                return true;
            case R.id.nav_days:
                Bundle bundleAD = new Bundle();
                bundleAD.putString(FirebaseAnalytics.Param.ITEM_ID, "16");
                bundleAD.putString(FirebaseAnalytics.Param.ITEM_NAME, "chartFragmentDays_opened");
                bundleAD.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chartFragmentDays_opened");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleAD);

                menuItem.setChecked(true);
                Bundle bundleD = new Bundle();
                bundleD.putString("key", "d");
                bundleD.putString("ikey", ifce);
                bundleD.putString("hkey", host);
                setFragment(chartFragmentDays,bundleD,"DAYS_CHART");
                drawerLayout.closeDrawer(GravityCompat.START);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.chart_header_days);
                }
                return true;
            case R.id.nav_months:
                Bundle bundleAM = new Bundle();
                bundleAM.putString(FirebaseAnalytics.Param.ITEM_ID, "17");
                bundleAM.putString(FirebaseAnalytics.Param.ITEM_NAME, "chartFragmentMonths_opened");
                bundleAM.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chartFragmentMonths_opened");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleAM);

                menuItem.setChecked(true);
                Bundle bundleM = new Bundle();
                bundleM.putString("key", "m");
                bundleM.putString("ikey", ifce);
                bundleM.putString("hkey", host);
                setFragment(chartFragmentMonths,bundleM,"MONTHS_CHART");
                drawerLayout.closeDrawer(GravityCompat.START);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.chart_header_months);
                }
                return true;

            case R.id.nav_server_info:
                menuItem.setChecked(true);
                Bundle bundleU = new Bundle();
                bundleU.putString("key", "test" );
                setFragment(serverInfoFragment,bundleU,"UP_TIME");
                drawerLayout.closeDrawer(GravityCompat.START);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.server_info_header);
                }
                return true;

            case R.id.nav_settings:
                startActivity(new Intent(this, PreferencesActivity.class));
        }
        return true;
    }

    public void setFragment(Fragment fragment, Bundle bundle, String tag) {

        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        //Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment);
        if(fragment.isVisible()) {
            fragment.getArguments().putAll(bundle);
        }else{
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(R.id.fragment, fragment, tag);
        fragmentTransaction.commit();
        /*
        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                SummaryFragment summaryFragment = new SummaryFragment();
                summaryFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment, summaryFragment);
                fragmentTransaction.commit();
                break;
            case 1:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                ChartFragmentHours chartFragmentHours = new ChartFragmentHours();
                chartFragmentHours.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment, chartFragmentHours);
                fragmentTransaction.commit();
                break;
        }
        */
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(userIsInteracting) {
            refreshActivity();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class getNetworkInterfaces extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;

        @Override
        protected void onPreExecute (){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            loadingDialog =  new ProgressDialog(MainActivity.this);
            loadingDialog.setTitle("Getting Network Interfaces");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(Void result){
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            String fileName = "interfaces.txt";
            String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
            String[] lines = null;
            FileArrayProvider fap = new FileArrayProvider();
            try {
                lines = fap.readLines(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> names = new ArrayList<>();
            if (lines != null) {
                for (String line : lines) {
                    if (isNumeric(line.substring(0, 1))) {
                        String cln = ":";
                        char c = cln.charAt(0);
                        String ifce = line.substring(ordinalIndexOf(line, c, 0) + 2, ordinalIndexOf(line, c, 1));
                        names.add(ifce);
                    }
                }
            }

            DataBaseAdapter mDbHelper = new DataBaseAdapter(MainActivity.this);
            mDbHelper.createDatabase();
            mDbHelper.open();
            mDbHelper.deleteInterfaces(host);
            Log.d("INTERFACEARRAY", names.toString());
            mDbHelper.insertInterfaces(names);
            mDbHelper.updateInterfaceTable(host);
            mDbHelper.close();

            new runBackgroundTask().execute(username, password, host, port, timeout);

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SSHGetInterfaceData.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class runBackgroundTask extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;

        @Override
        protected Void doInBackground(String... params) {
            Log.d("RUNBACKGROUNDTASK", "IS RUNNING");
            DataBaseAdapter mDbHelper = new DataBaseAdapter(MainActivity.this);
            mDbHelper.createDatabase();
            mDbHelper.open();
            Cursor getData = mDbHelper.getInterfaceData(host);
            mDbHelper.close();
            getData.moveToFirst();
            while (!getData.isAfterLast()) {
                String ifce = getData.getString(getData.getColumnIndexOrThrow("interface"));
                String command;
                if(username.equals("root")) {
                    command = "vnstat -u -i " + ifce + " && vnstat -i " + ifce + " --dumpdb";
                } else {
                    command = "vnstat -i " + ifce + " --dumpdb";
                }
                try {
                    SSHGetData.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),command,ifce,Integer.parseInt(params[4]));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("GATHERING DATA", command);
                getData.moveToNext();
            }

            return null;
        }

        @Override
        protected void onPreExecute (){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            loadingDialog =  new ProgressDialog(MainActivity.this);
            loadingDialog.setTitle("Gathering Data");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {

            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            new processData().execute();

        }

    }

    private class processData extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String ifceArray[];
        String ifce;

        @Override
        protected Void doInBackground(String... params) {

            DataBaseAdapter mDbHelper = new DataBaseAdapter(MainActivity.this);
            mDbHelper.createDatabase();
            mDbHelper.open();
            Cursor getData = mDbHelper.getInterfaceData(host);
            int rows = getData.getCount();
            Log.d("proccessData row count", "" + rows);
            List<String> ifceList = new ArrayList<>();
            getData.moveToFirst();
            while (!getData.isAfterLast()) {
                ifce = getData.getString(getData.getColumnIndexOrThrow("interface"));
                String fileName = ifce + ".txt";
                Log.d("PROCESSDATA",fileName);
                String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
                String[] lines = null;
                FileArrayProvider fap = new FileArrayProvider();
                try {
                    lines = fap.readLines(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ifceList.add(ifce);
                mDbHelper.deleteData(ifce, host);
                mDbHelper.insertData(lines);
                mDbHelper.updateData(ifce, host);
                getData.moveToNext();
            }
            ifceArray = ifceList.toArray(new String[ifceList.size()]);
            mDbHelper.close();

            return null;
        }

        @Override
        protected void onPreExecute (){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            loadingDialog =  new ProgressDialog(MainActivity.this);
            loadingDialog.setTitle("Processing Data");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();

        }

        @Override
        protected void onPostExecute(Void result) {

            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            setSpinnerArray(ifceArray);
            if (interfaceSpinner != null) {
                interfaceSpinner.setSelection(spinnerPosition,false);
                interfaceSpinner.setOnItemSelectedListener(MainActivity.this);
                ifce = interfaceSpinner.getSelectedItem().toString();
            }

            //Set first fragment
            if (fragTag != null) {
                switch (fragTag) {
                    case "SUMMARY":
                        Bundle bundleA = new Bundle();
                        bundleA.putString(FirebaseAnalytics.Param.ITEM_ID, "14");
                        bundleA.putString(FirebaseAnalytics.Param.ITEM_NAME, "SummaryFragment_opened");
                        bundleA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SummaryFragment_opened");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleA);
                        Bundle bundle = new Bundle();
                        bundle.putString("nkey", name);
                        bundle.putString("hkey", host);
                        bundle.putString("ikey", ifce);
                        setFragment(summaryFragment, bundle, "SUMMARY");
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Summary");
                        }
                        break;
                    case "HOURS_CHART":
                        Bundle bundleAH = new Bundle();
                        bundleAH.putString(FirebaseAnalytics.Param.ITEM_ID, "15");
                        bundleAH.putString(FirebaseAnalytics.Param.ITEM_NAME, "chartFragmentHours_opened");
                        bundleAH.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chartFragmentHours_opened");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleAH);
                        Bundle bundleH = new Bundle();
                        bundleH.putString("key", "h");
                        bundleH.putString("ikey", ifce);
                        bundleH.putString("hkey", host);
                        setFragment(chartFragmentHours, bundleH, "HOURS_CHART");
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(R.string.chart_header_hours);
                        }
                        break;
                    case "DAYS_CHART":
                        Bundle bundleAD = new Bundle();
                        bundleAD.putString(FirebaseAnalytics.Param.ITEM_ID, "16");
                        bundleAD.putString(FirebaseAnalytics.Param.ITEM_NAME, "chartFragmentDays_opened");
                        bundleAD.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chartFragmentDays_opened");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleAD);
                        Bundle bundleD = new Bundle();
                        bundleD.putString("key", "d");
                        bundleD.putString("ikey", ifce);
                        bundleD.putString("hkey", host);
                        setFragment(chartFragmentDays, bundleD, "DAYS_CHART");
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(R.string.chart_header_days);
                        }
                        break;
                    case "MONTHS_CHART":
                        Bundle bundleAM = new Bundle();
                        bundleAM.putString(FirebaseAnalytics.Param.ITEM_ID, "17");
                        bundleAM.putString(FirebaseAnalytics.Param.ITEM_NAME, "chartFragmentMonths_opened");
                        bundleAM.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chartFragmentMonths_opened");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleAM);
                        Bundle bundleM = new Bundle();
                        bundleM.putString("key", "m");
                        bundleM.putString("ikey", ifce);
                        bundleM.putString("hkey", host);
                        setFragment(chartFragmentMonths, bundleM, "MONTHS_CHART");
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(R.string.chart_header_months);
                        }
                        break;
                }
            } else {
                Bundle bundleA = new Bundle();
                bundleA.putString(FirebaseAnalytics.Param.ITEM_ID, "14");
                bundleA.putString(FirebaseAnalytics.Param.ITEM_NAME, "SummaryFragment_opened");
                bundleA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SummaryFragment_opened");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleA);

                Bundle bundle = new Bundle();
                bundle.putString("nkey", name);
                bundle.putString("hkey", host);
                bundle.putString("ikey", ifce);
                setFragment(summaryFragment, bundle, "SUMMARY");
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Summary");
                }
            }




        }

    }

    public void refreshActivity(){
        Fragment f = this.getSupportFragmentManager().findFragmentById(R.id.fragment);
        int spinnerPosition = interfaceSpinner.getSelectedItemPosition();
        Log.d("CURRENT FRAGMENT",f.getTag());
        Log.d("CURRENT INTERFACE","" + spinnerPosition);
        Intent i = getIntent();
        i.putExtra("fkey",f.getTag());
        i.putExtra("ikey",spinnerPosition);
        i.putExtra("hkey",host);
        finish();
        startActivity(i);
    }

    public static boolean isNumeric(String str) {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static int ordinalIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }

    public void setSpinnerArray(String[] array){
        //ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, array);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item,array);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        interfaceSpinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }
}

