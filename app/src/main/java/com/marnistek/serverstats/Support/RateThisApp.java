package com.marnistek.serverstats.Support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;

import com.marnistek.serverstats.R;

import java.util.Date;

/**
 * RateThisApp<br>
 * A library to show the app rate dialog
 *
 */
public class RateThisApp {
        
        private static final String TAG = RateThisApp.class.getSimpleName();
        
        private static final String PREF_NAME = "RateThisApp";
        private static final String KEY_INSTALL_DATE = "rta_install_date";
        private static final String KEY_LAUNCH_TIMES = "rta_launch_times";
        private static final String KEY_OPT_OUT = "rta_opt_out";
        
        private static Date mInstallDate = new Date();
        private static int mLaunchTimes = 0;
        private static boolean mOptOut = false;
        
        /**
         * Days after installation until showing rate dialog
         */
        public static final int INSTALL_DAYS = 20;
        /**
         * App launching times until showing rate dialog
         */
        public static final int LAUNCH_TIMES = 15;
        
        /**
         * If true, print LogCat
         */
        public static final boolean DEBUG = false;
        
        /**
         * Call this API when the launcher activity is launched.<br>
         * It is better to call this API in onStart() of the launcher activity.
         */
        public static void onStart(Context context) {
                SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Editor editor = pref.edit();
                // If it is the first launch, save the date in shared preference.
                if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
                        Date now = new Date();
                        editor.putLong(KEY_INSTALL_DATE, now.getTime());
                        log("First install: " + now.toString());
                }
                // Increment launch times
                int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
                launchTimes++;
                editor.putInt(KEY_LAUNCH_TIMES, launchTimes);
                log("Launch times; " + launchTimes);
                
                editor.apply();
                
                mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
                mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
                mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
                
                printStatus(context);
        }
        
        /**
         * Show the rate dialog if the criteria is satisfied
         */
        public static void showRateDialogIfNeeded(final Context context) {
                if (shouldShowRateDialog()) {
                        showRateDialog(context);
                }
        }
        
        /**
         * Check whether the rate dialog shoule be shown or not
         */
        private static boolean shouldShowRateDialog() {
                if (mOptOut) {
                        return false;
                } else {
                        if (mLaunchTimes >= LAUNCH_TIMES) {
                                return true;
                        }
                        int threshold = INSTALL_DAYS * 24 * 60 * 60 * 1000;
                    return new Date().getTime() - mInstallDate.getTime() >= threshold;
                }
        }
        
        /**
         * Show the rate dialog
         */
        public static void showRateDialog(final Context context) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.rta_dialog_title);
                builder.setMessage(R.string.rta_dialog_message);
                builder.setPositiveButton(R.string.rta_dialog_ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                String appPackage = context.getPackageName();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                                context.startActivity(intent);
                                setOptOut(context, true);
                        }
                });
                builder.setNeutralButton(R.string.rta_dialog_cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                clearSharedPreferences(context);
                        }
                });
                builder.setNegativeButton(R.string.rta_dialog_no, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                setOptOut(context, true);
                        }
                });
                builder.setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                                clearSharedPreferences(context);
                        }
                });
                builder.create().show();
        }
        
        /**
         * Clear data in shared preferences.<br>
         * This API is called when the rate dialog is approved or canceled.
         */
        private static void clearSharedPreferences(Context context) {
                SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Editor editor = pref.edit();
                editor.remove(KEY_INSTALL_DATE);
                editor.remove(KEY_LAUNCH_TIMES);
                editor.apply();
        }
        
        /**
         * Set opt out flag. If it is true, the rate dialog will never shown unless app data is cleared.
         */
        private static void setOptOut(final Context context, boolean optOut) {
                SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Editor editor = pref.edit();
                editor.putBoolean(KEY_OPT_OUT, optOut);
                editor.apply();
        }
        
        /**
         * Print values in SharedPreferences (used for debug)
         */
        private static void printStatus(final Context context) {
                SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                log("*** RateThisApp Status ***");
                log("Install Date: " + new Date(pref.getLong(KEY_INSTALL_DATE, 0)));
                log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0));
                log("Opt out: " + pref.getBoolean(KEY_OPT_OUT, false));
        }
        
        /**
         * Print log if enabled
         */
        private static void log(String message) {
                if (DEBUG) {
                        Log.v(TAG, message);
                }
        }
}
