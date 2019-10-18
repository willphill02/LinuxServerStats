package com.marnistek.serverstats.Activites;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;
import com.marnistek.serverstats.SSH.SSHAddToKnownHosts;
import com.marnistek.serverstats.SSH.SSHCheck;
import com.marnistek.serverstats.SSH.SSHGetFingerprint;
import com.marnistek.serverstats.SSH.SSHGetInterfaceData;
import com.marnistek.serverstats.SSH.SSHInstall;
import com.marnistek.serverstats.Support.FileArrayProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstallVNStat extends AppCompatActivity {

    String name;
    String os;
    String host;
    String username;
    String password;
    String port;
    String fingerprint;
    String timeout;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        name = getIntent().getStringExtra("nkey");
        os = getIntent().getStringExtra("okey");
        host = getIntent().getStringExtra("hkey");
        username = getIntent().getStringExtra("ukey");
        password = getIntent().getStringExtra("pkey");
        port = getIntent().getStringExtra("ptkey");
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        String timeoutSecs = SP.getString("timeoutSecs","0");
        if(isNumeric(timeoutSecs)){
            timeout = timeoutSecs;
        } else {
            timeout = "0";
        }

        new getFingerprint().execute(username,password,host,port,timeout);
    }

    private class getFingerprint extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String error;

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(InstallVNStat.this);
            loadingDialog.setTitle("Checking Connection");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if(error != null){
                String message = "Failed to connect to server, please check credentials or network connection\n\n" +
                                 "Error message:\n\n" + error + "\n\n" +
                                 "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                String title = "SSH Connection Failed";
                new AlertDialog.Builder(InstallVNStat.this)
                        .setTitle(title)
                        .setMessage(message)
                        //.setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                finish();
                            }
                        })
                        .create().show();
            } else {
                new checkConnection().execute(username, password, host, port, "date", timeout);
            }
            if(fingerprint == null){
                fingerprint = "No Fingerprint";
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                fingerprint = SSHGetFingerprint.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]), Integer.parseInt(params[4]));
            } catch (Exception e) {
                e.printStackTrace();
                error = e.toString();
                Log.d("Error fingerprint", error);
            }
            return null;
        }

    }

    private class checkConnection extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String check;
        String error;

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(InstallVNStat.this);
            loadingDialog.setTitle("Checking Connection");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
            loadingDialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            String message;
            String title;
            String isHostCheck;
            if(error != null){
                if (error.contains("UnknownHostKey")) {
                    title = "Host Key Verification";
                    message = "It looks like this is the first time you have connected to " +
                            host +
                            "\n\n" +
                            error.substring(error.indexOf("RSA"),error.length()) +
                            "\n\n" +
                            "Do you accept this key?";
                    isHostCheck = "hostCheck";
                } else if(error.contains("HostKey has been changed")) {
                    title = "WARNING";
                    message = "REMOTE HOST IDENTIFICATION HAS CHANGED FOR \n\n" +
                            host + "\n\n" +
                            "Someone could be eavesdropping on you right now (man-in-the-middle attack)!\n" +
                            "It is also possible that a host key has just been changed." +
                            "\n\n" +
                            "RSA key fingerprint is " + fingerprint +
                            "\n\n" +
                            "If you wish to connect to this server please select \"Go to Known Hosts\" and delete the host key for this host." +
                            "\n\n" +
                            "WARNING" +
                            "\n" +
                            "ONLY DO THIS IF YOU ARE AWARE OF THIS CHANGE";
                    isHostCheck = "keyChanged";

                } else {
                    title = "SSH Connection Failed";
                    message = "Failed to connect to server, please check credentials or network connection\n\n" +
                            "Server returned the following message:\n\n" + error + "\n\n" +
                            "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                    isHostCheck = "no";
                }
            } else {
                message = "Failed to connect to server, please check credentials or network connection";
                title = "SSH Connection Failed";
                isHostCheck = "no";
            }
            if(check == null || check.equals("")){
                switch (isHostCheck) {
                    case "hostCheck":
                        new AlertDialog.Builder(InstallVNStat.this)
                                .setTitle(title)
                                .setMessage(message)
                                //.setCancelable(false)
                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int which) {
                                        new addHostKey().execute(username, password, host, port, "date", timeout);
                                    }
                                })
                                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .create().show();
                        break;
                    case "keyChanged":
                        new AlertDialog.Builder(InstallVNStat.this)
                                .setTitle(title)
                                .setMessage(message)
                                //.setCancelable(false)
                                .setPositiveButton("Go to Known Hosts", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(InstallVNStat.this, KnownHosts.class));
                                        InstallVNStat.this.finish();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .create().show();
                        break;
                    default:
                        new AlertDialog.Builder(InstallVNStat.this)
                                .setTitle(title)
                                .setMessage(message)
                                //.setCancelable(false)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                        finish();
                                    }
                                })
                                .create().show();
                        break;
                }
            } else {
                new checkIfInstalled().execute(username, password, host, port, "vnstat", timeout);
            }



        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                check = SSHCheck.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("SSHCheck Error",e.toString());
                error = e.toString();
            }
            return null;
        }

    }

    private class addHostKey extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String check;
        String error;

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(InstallVNStat.this);
            loadingDialog.setTitle("Checking Connection");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
            loadingDialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            Log.d("TESTING","test123 " + check);
            if(check == null || check.equals("")){
                new AlertDialog.Builder(InstallVNStat.this)
                        .setTitle("SSH Connection Failed")
                        .setMessage("Failed to connect to server, please check credentials or network connection\n\n" +
                                    "To extend timeout period please change \"SSH Timeout Period\" in Settings")
                        //.setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                finish();
                            }
                        })
                        .create().show();
            } else {
                new checkIfInstalled().execute(username, password, host, port, "vnstat", timeout);
            }



        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                check = SSHAddToKnownHosts.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("addHostKey",e.toString());
                error = e.toString();
            }
            return null;
        }

    }

    private class checkIfInstalled extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String check;

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(InstallVNStat.this);
            loadingDialog.setTitle("Verifying Server");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
            loadingDialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if(check == null || check.equals("") || check.contains("bash")){
                String msg;
                if(os.equals("Other")){
                    msg = getString(R.string.install_vnstat_warning1_other_os);
                } else {
                    if (username.equals("root")) {
                        msg = getString(R.string.install_vnstat_warning1_root);
                    } else {
                        msg = getString(R.string.install_vnstat_warning1);
                    }
                }
                if(os.equals("Other")){
                    new AlertDialog.Builder(InstallVNStat.this)
                            .setTitle("Install vnStat")
                            .setMessage(msg)
                            //.setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    InstallVNStat.this.finish();
                                }
                            }).create().show();

                } else {
                    new AlertDialog.Builder(InstallVNStat.this)
                            .setTitle("Install vnStat?")
                            .setMessage(msg)
                            //.setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "6");
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "vnstat_installed_first_warning");
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "vnstat_installed_first_warning");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                    if (username.equals("root")) {
                                        if (os.equals("CentOS") || os.equals("Fedora") || os.equals("Red Hat") || os.equals("CloudLinux")) {
                                            Log.d("TESTING", "CentOS");
                                            new installEpel().execute(username, password, host, port, "yum install epel-release -y", timeout);
                                        } else if (os.equals("Ubuntu") || os.equals("Debian")) {
                                            Log.d("TESTING", "Ubuntu");
                                            new installVnstat().execute(username, password, host, port, "apt-get install vnstat -y", timeout);
                                        } else if (os.equals("Arch Linux")) {
                                            Log.d("TESTING", "Arch Linux");
                                            new installVnstat().execute(username, password, host, port, "pacman -S vnstat --noconfirm", timeout);
                                        } else if (os.equals("Gentoo")) {
                                            Log.d("TESTING", "Gentoo");
                                            new installVnstat().execute(username, password, host, port, "emerge vnstat", timeout);
                                        }
                                    } else {
                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InstallVNStat.this);
                                        LayoutInflater inflater = InstallVNStat.this.getLayoutInflater();
                                        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                                        dialogBuilder.setView(dialogView);

                                        final EditText edtUser = (EditText) dialogView.findViewById(R.id.sudo_user_edittext);
                                        final EditText edtPass = (EditText) dialogView.findViewById(R.id.sudo_pass_edittext);

                                        dialogBuilder.setTitle("Root Access Required");
                                        dialogBuilder.setMessage("Please enter root username and password");
                                        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                username = edtUser.getText().toString();
                                                password = edtPass.getText().toString();
                                                if (os.equals("CentOS") || os.equals("Fedora") || os.equals("Red Hat") || os.equals("CloudLinux")) {
                                                    Log.d("TESTING", "CentOS");
                                                    new installEpel().execute(username, password, host, port, "yum install epel-release -y", timeout);
                                                } else if (os.equals("Ubuntu") || os.equals("Debian")) {
                                                    Log.d("TESTING", "Ubuntu");
                                                    new installVnstat().execute(username, password, host, port, "apt-get install vnstat -y", timeout);
                                                } else if (os.equals("Arch Linux")) {
                                                    Log.d("TESTING", "Arch");
                                                    new installVnstat().execute(username, password, host, port, "pacman -S vnstat --noconfirm", timeout);
                                                } else if (os.equals("Gentoo")) {
                                                    Log.d("TESTING", "Gentoo");
                                                    new installVnstat().execute(username, password, host, port, "emerge vnstat", timeout);
                                                }
                                            }
                                        });
                                        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                finish();
                                            }
                                        });
                                        AlertDialog b = dialogBuilder.create();
                                        b.show();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String msg;
                                    if (username.equals("root")) {
                                        msg = getString(R.string.install_vnstat_warning2_root);
                                    } else {
                                        msg = getString(R.string.install_vnstat_warning2);
                                    }
                                    final SpannableString s = new SpannableString(msg);
                                    Linkify.addLinks(s, Linkify.ALL);
                                    final AlertDialog d = new AlertDialog.Builder(InstallVNStat.this)
                                            .setTitle("Alert")
                                            .setMessage(s)
                                            //.setCancelable(false)
                                            .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "7");
                                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "vnstat_installed_second_warning");
                                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "vnstat_installed_second_warning");
                                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                                    if (username.equals("root")) {
                                                        if (os.equals("CentOS") || os.equals("Fedora") || os.equals("Red Hat") || os.equals("CloudLinux")) {
                                                            Log.d("TESTING", "CentOS");
                                                            new installEpel().execute(username, password, host, port, "yum install epel-release -y", timeout);
                                                        } else if (os.equals("Ubuntu") || os.equals("Debian")) {
                                                            Log.d("TESTING", "Ubuntu");
                                                            new installVnstat().execute(username, password, host, port, "apt-get install vnstat -y", timeout);
                                                        } else if (os.equals("Arch Linux")) {
                                                            Log.d("TESTING", "Arch Linux");
                                                            new installVnstat().execute(username, password, host, port, "pacman -S vnstat --noconfirm", timeout);
                                                        } else if (os.equals("Gentoo")) {
                                                            Log.d("TESTING", "Gentoo");
                                                            new installVnstat().execute(username, password, host, port, "emerge vnstat", timeout);
                                                        }
                                                    } else {
                                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InstallVNStat.this);
                                                        LayoutInflater inflater = InstallVNStat.this.getLayoutInflater();
                                                        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                                                        dialogBuilder.setView(dialogView);

                                                        final EditText edtUser = (EditText) dialogView.findViewById(R.id.sudo_user_edittext);
                                                        final EditText edtPass = (EditText) dialogView.findViewById(R.id.sudo_pass_edittext);

                                                        dialogBuilder.setTitle("Root Access Required");
                                                        dialogBuilder.setMessage("Please enter root username and password");
                                                        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                                username = edtUser.getText().toString();
                                                                password = edtPass.getText().toString();
                                                                if (os.equals("CentOS") || os.equals("Fedora") || os.equals("Red Hat") || os.equals("CloudLinux")) {
                                                                    Log.d("TESTING", "CentOS");
                                                                    new installEpel().execute(username, password, host, port, "yum install epel-release -y", timeout);
                                                                } else if (os.equals("Ubuntu") || os.equals("Debian")) {
                                                                    Log.d("TESTING", "Ubuntu");
                                                                    new installVnstat().execute(username, password, host, port, "apt-get install vnstat -y", timeout);
                                                                } else if (os.equals("Arch Linux")) {
                                                                    Log.d("TESTING", "Arch");
                                                                    new installVnstat().execute(username, password, host, port, "pacman -S vnstat --noconfirm", timeout);
                                                                } else if (os.equals("Gentoo")) {
                                                                    Log.d("TESTING", "Gentoo");
                                                                    new installVnstat().execute(username, password, host, port, "emerge vnstat", timeout);
                                                                }
                                                            }
                                                        });
                                                        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                                finish();
                                                            }
                                                        });
                                                        AlertDialog b = dialogBuilder.create();
                                                        b.show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    finish();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "8");
                                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "vnstat_install_declined");
                                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "vnstat_install_declined");
                                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                                }
                                            }).create();
                                    d.show();
                                    ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                }
                            }).create().show();
                }
            } else {
                Intent i = new Intent(InstallVNStat.this, MainActivity.class);
                i.putExtra("nkey",name);
                i.putExtra("okey", os);
                i.putExtra("hkey", host);
                i.putExtra("ukey", username);
                i.putExtra("pkey", password);
                i.putExtra("ptkey",port);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                InstallVNStat.this.finish();
            }


        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                check = SSHCheck.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("checkIfInstalled",e.toString());
            }
            return null;
        }

    }


    private class installEpel extends AsyncTask<String, Void, Void> {

            ProgressDialog loadingDialog;
            String error;

            @Override
            protected void onPreExecute (){
                loadingDialog =  new ProgressDialog(InstallVNStat.this);
                loadingDialog.setTitle("Installing vnStat");
                loadingDialog.setMessage("Please Wait");
                loadingDialog.show();
            }

            @Override
            protected void onPostExecute(Void result) {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if(error == null) {
                    new installVnstat().execute(username, password, host, port, "yum install vnstat -y -y", timeout);
                } else {
                    String message = "Server returned the following message:\n\n" + error + "\n\n" +
                                     "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                    String title = "Error";
                    new AlertDialog.Builder(InstallVNStat.this)
                            .setTitle(title)
                            .setMessage(message)
                            //.setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                    finish();
                                }
                            }).create().show();
                }

            }

            @Override
            protected Void doInBackground(String... params) {
                try {
                    SSHInstall.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
                } catch (Exception e) {
                    e.printStackTrace();
                    error = e.toString();
                    Log.d("installEpel",error);
                }
                return null;
            }
    }

    private class installVnstat extends AsyncTask<String, Void, Void> {

            ProgressDialog loadingDialog;
            String error;

            @Override
            protected void onPreExecute (){
                loadingDialog =  new ProgressDialog(InstallVNStat.this);
                loadingDialog.setTitle("Installing vnStat");
                loadingDialog.setMessage("Please Wait");
                loadingDialog.show();
            }

            @Override
            protected void onPostExecute(Void result) {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if(error == null) {
                    switch (os) {
                        case "CentOS":
                            new startVnstatService().execute(username, password, host, port, "chkconfig vnstat on && service vnstat start && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "Fedora":
                            new startVnstatService().execute(username, password, host, port, "chkconfig vnstat on && service vnstat start && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "Red Hat":
                            new startVnstatService().execute(username, password, host, port, "chkconfig vnstat on && service vnstat start && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "CloudLinux":
                            new startVnstatService().execute(username, password, host, port, "chkconfig vnstat on && service vnstat start && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "Ubuntu":
                            new startVnstatService().execute(username, password, host, port, "update-rc.d vnstat defaults && service vnstat start && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "Debian":
                            new startVnstatService().execute(username, password, host, port, "update-rc.d vnstat defaults && service vnstat start && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "Arch Linux":
                            new startVnstatService().execute(username, password, host, port, "systemctl enable vnstat && systemctl start vnstat && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                        case "Gentoo":
                            new startVnstatService().execute(username, password, host, port, "/etc/init.d/vnstatd start && rc-update add vnstatd && chown -R vnstat:vnstat /var/lib/vnstat", timeout);
                            break;
                    }
                } else {
                    String message = "Server returned the following message:\n\n" + error + "\n\n" +
                                     "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                    String title = "Error";
                    new AlertDialog.Builder(InstallVNStat.this)
                            .setTitle(title)
                            .setMessage(message)
                            //.setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                    finish();
                                }
                            }).create().show();
                }

            }

            @Override
            protected Void doInBackground(String... params) {
                try {
                    SSHInstall.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
                } catch (Exception e) {
                    e.printStackTrace();
                    error = e.toString();
                    Log.d("installVnstat",error);
                }
                return null;
            }
    }

    private class startVnstatService extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String error;

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(InstallVNStat.this);
            loadingDialog.setTitle("Starting vnStat Service");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if(error == null) {
                new getNetworkInterfaces().execute(username, password, host, port, "ip link show", timeout);
            } else {
                String message = "Server returned the following message:\n\n" + error + "\n\n" +
                        "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                String title = "Error";
                new AlertDialog.Builder(InstallVNStat.this)
                        .setTitle(title)
                        .setMessage(message)
                        //.setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                finish();
                            }
                        }).create().show();
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SSHInstall.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
            } catch (Exception e) {
                e.printStackTrace();
                error = e.toString();
                Log.d("startVnstatService",error);
            }
            return null;
        }
    }

    private class getNetworkInterfaces extends AsyncTask<String, Void, Void> {

        ProgressDialog loadingDialog;
        String error;

        @Override
        protected void onPreExecute (){
            loadingDialog =  new ProgressDialog(InstallVNStat.this);
            loadingDialog.setTitle("Getting Network Interfaces");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(Void result){
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if(error == null) {
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
                //names.remove(names.indexOf("lo"));

                DataBaseAdapter mDbHelper = new DataBaseAdapter(InstallVNStat.this);
                mDbHelper.createDatabase();
                mDbHelper.open();
                mDbHelper.deleteInterfaces(host);
                mDbHelper.insertInterfaces(names);
                mDbHelper.updateInterfaceTable(host);
                mDbHelper.close();

                StringBuilder stringBuilder = new StringBuilder();
                for (String ifce : names) {
                    String commandPart = "vnstat -u -i " + ifce + " && ";
                    stringBuilder.append(commandPart);
                    /*
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    */
                }
                String command = stringBuilder.toString();
                command = command.substring(0,command.length()-4);
                Log.d("createVnstatDB",command);
                new createVnstatDB().execute(username, password, host, port, command, timeout);
            } else {
                String message = "Server returned the following message:\n\n" + error + "\n\n" +
                        "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                String title = "Error";
                new AlertDialog.Builder(InstallVNStat.this)
                        .setTitle(title)
                        .setMessage(message)
                        //.setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                finish();
                            }
                        }).create().show();
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SSHGetInterfaceData.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
            } catch (Exception e) {
                e.printStackTrace();
                error = e.toString();
                Log.d("getNetworkInterfaces", error);
            }
            return null;
        }
    }

    private class createVnstatDB extends AsyncTask<String, Void, Void> {

            ProgressDialog loadingDialog;
            String error;

            @Override
            protected void onPreExecute (){
                loadingDialog =  new ProgressDialog(InstallVNStat.this);
                loadingDialog.setTitle("Creating Database");
                loadingDialog.setMessage("Please Wait");
                loadingDialog.show();
            }

            @Override
            protected void onPostExecute(Void result) {
                if(error == null) {
                    Intent i = new Intent(InstallVNStat.this, MainActivity.class);
                    i.putExtra("nkey", name);
                    i.putExtra("okey", os);
                    i.putExtra("hkey", host);
                    i.putExtra("ukey", username);
                    i.putExtra("pkey", password);
                    i.putExtra("ptkey", port);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    finish();
                } else {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    String message = "Server returned the following message:\n\n" + error + "\n\n" +
                            "To extend timeout period please change \"SSH Timeout Period\" in Settings";
                    String title = "Error";
                    new AlertDialog.Builder(InstallVNStat.this)
                            .setTitle(title)
                            .setMessage(message)
                            //.setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNeutralButton("Go To Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(InstallVNStat.this, PreferencesActivity.class));
                                    finish();
                                }
                            })
                            .create().show();
                }


            }

            @Override
            protected Void doInBackground(String... params) {
                try {
                    SSHInstall.executeRemoteCommand(params[0], params[1], params[2], Integer.parseInt(params[3]),params[4],Integer.parseInt(params[5]));
                } catch (Exception e) {
                    e.printStackTrace();
                    error = e.toString();
                    Log.d("createVnstatDB",error);
                }
                return null;
            }
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

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
