package com.marnistek.serverstats.Database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.IOException;

public class DataBaseHelper extends SQLiteAssetHelper {

    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME ="ServerStats.sqlite";// Database name
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase mDataBase;

    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, DATABASE_VERSION);
        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }
    }

    public void createDataBase() throws IOException {
        this.getWritableDatabase();
        this.close();
        //Copy the database from assests
        String TAG = "DataBaseHelper";
        Log.e(TAG, "createDatabase database created");
    }

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }
}