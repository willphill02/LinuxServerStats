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

public class EditServer extends AppCompatActivity implements View.OnClickListener {

    String name;
    String os;
    String host;
    String username;
    String password;
    String port;
    int portInt;
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

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "5");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "EditServer_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "EditServer_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        setContentView(R.layout.activity_add_new_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        name = getIntent().getStringExtra("nkey");
        os = getIntent().getStringExtra("okey");
        host = getIntent().getStringExtra("hkey");
        username = getIntent().getStringExtra("ukey");
        password = getIntent().getStringExtra("pkey");
        port = getIntent().getStringExtra("ptkey");
        portInt = Integer.parseInt(port);
        login = (Button) findViewById(R.id.connectButton);
        etName = (EditText) findViewById(R.id.et_name);
        osSpinner = (Spinner) findViewById(R.id.os_spinner);
        etHost = (EditText) findViewById(R.id.et_host);
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        etPort = (EditText) findViewById(R.id.et_port);

        etName.setText(name);

        switch (os){
            case "Ubuntu":
                osSpinner.setSelection(1);
                break;
            case "Debian":
                osSpinner.setSelection(2);
                break;
            case "CentOS":
                osSpinner.setSelection(3);
                break;
            case "Fedora":
                osSpinner.setSelection(4);
                break;
            case "Red Hat":
                osSpinner.setSelection(5);
                break;
            case "CloudLinux":
                osSpinner.setSelection(6);
                break;
            case "Arch Linux":
                osSpinner.setSelection(7);
                break;
            case "Gentoo":
                osSpinner.setSelection(8);
                break;
            case "Other":
                osSpinner.setSelection(9);
                break;
        }
        etHost.setText(host);
        etUsername.setText(username);
        etPassword.setText(password);
        etPort.setText(port);

        if (login != null) {
            login.setOnClickListener(this);
            login.setText(R.string.edit_server_button_text);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.connectButton:
                String nameNew = etName.getText().toString();
                String osNew = osSpinner.getSelectedItem().toString();
                String hostNew = etHost.getText().toString();
                String usernameNew = etUsername.getText().toString();
                String passwordNew = etPassword.getText().toString();
                int portNew = Integer.parseInt(etPort.getText().toString());

                DataBaseAdapter mDbHelper = new DataBaseAdapter(this);
                mDbHelper.createDatabase();
                mDbHelper.open();
                mDbHelper.updateServer(name, os, host, username, password, portInt, nameNew, osNew, hostNew, usernameNew, passwordNew, portNew);
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
