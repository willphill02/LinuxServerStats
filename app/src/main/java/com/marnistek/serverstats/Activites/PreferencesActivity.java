package com.marnistek.serverstats.Activites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;

import java.io.File;

public class PreferencesActivity extends AppCompatActivity {

    static boolean reset = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "18");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Settings_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Settings_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reset) {
                        startActivity(new Intent(PreferencesActivity.this, ServerSelector.class));
                    } else {
                        finish();
                    }
                }
            });
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsPreference())
                .commit();
    }

    public static class SettingsPreference extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // applyTheme();
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference prefViewKnownHosts = findPreference("prefViewKnownHosts");
            prefViewKnownHosts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), KnownHosts.class));
                    return true;
                }
            });

            Preference prefDeleteKnownHosts = findPreference("prefDeleteKnownHosts");
            prefDeleteKnownHosts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    String fileName = "known_hosts.txt";
                                    String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
                                    File targetFile = new File(path);
                                    if(targetFile.exists()){
                                        targetFile.delete();
                                    }
                                    DataBaseAdapter mDbHelper = new DataBaseAdapter(getActivity());
                                    mDbHelper.createDatabase();
                                    mDbHelper.open();
                                    mDbHelper.deleteKnownHosts();
                                    mDbHelper.close();
                                    Toast.makeText(getActivity(), "Known hosts deleted", Toast.LENGTH_LONG).show();

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //Do nothing
                                    break;
                            }
                        }
                    };
                    String dialogMessage = "Are you sure you want to delete all known hosts?";
                    builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(dialogMessage)
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                    return true;
                }
            });

            Preference prefReset = findPreference("prefReset");
            prefReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    DataBaseAdapter mDbHelper = new DataBaseAdapter(getActivity());
                                    mDbHelper.createDatabase();
                                    mDbHelper.open();
                                    mDbHelper.deleteAllData();
                                    mDbHelper.deleteAllServers();
                                    mDbHelper.deleteKnownHosts();
                                    mDbHelper.deleteAllInterfaces();
                                    mDbHelper.close();

                                    File direct = new File(Environment.getExternalStorageDirectory()
                                            + "/ServerStats/");
                                    if (direct.exists()) {
                                        deleteFiles(direct);
                                    }
                                    reset = true;
                                    Toast.makeText(getActivity(), "Data wiped", Toast.LENGTH_LONG).show();

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //Do nothing
                                    break;
                            }
                        }
                    };
                    String dialogMessage = "Are you sure you want to wipe all stored data?";
                    builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(dialogMessage)
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                    return true;
                }
            });

            Preference prefRate = findPreference("prefRate");
            prefRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //Update with correct package details
                    String appPackage = getActivity().getPackageName();
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                    startActivity(i);
                    return true;
                }
            });

            Preference prefHelp = findPreference("prefHelp");
            prefHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    String msg = getString(R.string.help_dialog_message);
                    final SpannableString s = new SpannableString(msg);
                    Linkify.addLinks(s, Linkify.ALL);
                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setTitle("Help")
                            .setMessage(s)
                            //.setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
                    d.show();
                    ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                    return true;
                }
             });

            Preference prefLicences = findPreference("prefLicences");
            prefLicences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), Licences.class));
                    return true;
                }
            });

            Preference prefMoreInfo = findPreference("prefMoreInfo");
            prefMoreInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //Update website details
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://marnistek.site88.net/"));
                    startActivity(i);
                    return true;
                }
            });

            Preference prefContact = findPreference("prefContact");
            prefContact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    String email = "marnistek@gmail.com";
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    //emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    //emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
                    return true;
                }
            });

        }
    }

    public static void deleteFiles(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFiles(f);
                } else {
                    f.delete();
                }
            }
        }
    }
}
