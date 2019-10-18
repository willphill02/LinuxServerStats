package com.marnistek.serverstats.Activites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;

public class AddNewServer extends AppCompatActivity implements View.OnClickListener {

    EditText etName;
    EditText etHost;
    EditText etUsername;
    EditText etPassword;
    EditText etPort;
    Button login;
    Spinner osSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_server);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "4");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AddNewServer_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "AddNewServer_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        login = (Button) findViewById(R.id.connectButton);
        etName = (EditText) findViewById(R.id.et_name);
        osSpinner = (Spinner) findViewById(R.id.os_spinner);
        etHost = (EditText) findViewById(R.id.et_host);
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        etPort = (EditText) findViewById(R.id.et_port);
        if (etPort != null) {
            etPort.setText("22");
        }
        if (login != null) {
            login.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.connectButton:
                String name = etName.getText().toString();
                String os = osSpinner.getSelectedItem().toString();
                String host = etHost.getText().toString();
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                int port = Integer.parseInt(etPort.getText().toString());

                DataBaseAdapter mDbHelper = new DataBaseAdapter(this);
                mDbHelper.createDatabase();
                mDbHelper.open();
                mDbHelper.insertServer(name, os, host, username, password, port);
                mDbHelper.insertHostIntoKnownHosts(host);
                mDbHelper.close();

                this.finish();
                //Intent i = new Intent(this, ServerSelector.class);
                //startActivity(i);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
