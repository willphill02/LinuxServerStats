package com.marnistek.serverstats.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class DataBaseAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DataBaseAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DataBaseAdapter createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataBaseAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getWritableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    public Cursor getTestData(String ifce, String host)
    {
        try
        {
            String sql ="SELECT * FROM v_overall_stats WHERE interface = '" + ifce + "' AND host = '" + host + "'";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getSummaryData(String dataType, String ifce, String host)
    {
        try
        {
            String sql ="SELECT * FROM v_data_main WHERE datatype = '" + dataType + "' AND interface = '" + ifce + "' AND host = '" + host + "' ORDER BY date_time DESC LIMIT 1";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getSummaryData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }


    public Cursor getTableData(String dataType, String ifce, String host)
    {
        try
        {
            String sql = "SELECT DISTINCT " +
                    "date_, " +
                    "data_in, " +
                    "data_out, " +
                    "total_data " +
                    "FROM " +
                    "v_get_table_data " +
                    "WHERE " +
                    "datatype = '" + dataType +"' AND " +
                    "interface = '" + ifce + "' AND " +
                    "host = '" + host + "'";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTableData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getGraphData(String dataType, String ifce, String host)
    {
        try
        {
            String sql = "SELECT " +
                    "xData, " +
                    "rx, " +
                    "tx, " +
                    "total " +
                    "FROM " +
                    "v_get_graph_data " +
                    "WHERE " +
                    "datatype = '" + dataType +"' AND " +
                    "interface = '" + ifce + "' AND " +
                    "host = '" + host + "' " +
                    "ORDER BY date_time";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getDaysGraphData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getPieData(String dataType, String ifce, String host)
    {
        String sql;
        try
        {
            if(dataType.equals("s")){
                sql = "SELECT\n" +
                        "(totalrx_MiB * 1024) + totalrx_KiB AS rx,\n" +
                        "(totaltx_MiB * 1024) + totaltx_KiB AS tx\n" +
                        "FROM\n" +
                        "v_overall_stats\n" +
                        "WHERE\n" +
                        "interface = '" + ifce + "' AND\n" +
                        "host = '" + host + "'";
            }else{
                sql = "SELECT rx_KiB AS rx, tx_KiB AS tx FROM v_data_main WHERE datatype = '" + dataType + "' AND interface = '" + ifce + "' AND host = '" + host + "' ORDER BY date_time DESC LIMIT 1";
            }

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getPieData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }


    public Cursor getServerData()
    {
        try
        {
            String sql ="SELECT\n" +
                    "name,\n" +
                    "username || '@' || host || ':' || port AS displayName,\n" +
                    "os,\n" +
                    "host,\n" +
                    "username,\n" +
                    "password,\n" +
                    "port,\n" +
                    "_id\n" +
                    "\n" +
                    "FROM\n" +
                    "t_servers";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getServerData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getKnownHosts()
    {
        try
        {
            String sql ="SELECT DISTINCT * FROM t_known_hosts WHERE knownhosts IS NOT NULL";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getKnownHosts >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getInterfaceData(String host)
    {
        try
        {
            String sql ="SELECT " +
                    "interface " +
                    "FROM " +
                    "t_interfaces " +
                    "WHERE " +
                    "host = '" + host + "' AND " +
                    "interface IS NOT NULL " +
                    "ORDER BY " +
                    "CASE WHEN interface = 'lo' THEN 'zzzzzz' ELSE interface END";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getInterfaceData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getLicences()
    {
        try
        {
            String sql ="SELECT * FROM t_licences";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getLicences >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor getErrorIfNull()
    {
        try
        {
            String sql ="SELECT data from t_data_split";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getErrorIfNull >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public Cursor checkKnownHosts(String host)
    {
        try
        {
            String sql ="SELECT * from t_known_hosts WHERE host = '" + host + "'";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "checkKnownHosts >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public void deleteData(String ifce, String host){
        String q = "DELETE FROM t_data_split WHERE interface = '" + ifce + "' AND host = '" + host + "'";
        mDb.execSQL(q);
    }
    public void deleteInterfaces(String host){
        String q = "DELETE FROM t_interfaces WHERE host = '" + host + "'";
        mDb.execSQL(q);
    }


    public void insertData(String[] line) {
        try{
            for (String aLine : line) {
                String q = "INSERT INTO t_data_split(data) VALUES('" + aLine + "')";
                mDb.execSQL(q);
            }
            Log.d("TESTING", "data inserted into t_data_split");
        }catch(Exception ex){
            Log.e("Error inserting array", ex.toString());
        }
    }


    public void deleteKnownHosts(){
        String q = "DELETE FROM t_known_hosts";
        mDb.execSQL(q);
    }

    public void deleteFromKnownHosts(String line){
        String q = "DELETE FROM t_known_hosts WHERE knownhosts = '" + line + "'";
        mDb.execSQL(q);
    }

    public void updateKnownHosts(String line){
        String q = "UPDATE t_known_hosts\n" +
                "SET knownhosts = '" + line + "'" +
                "WHERE\n" +
                "'" + line + "'" + " LIKE '%' || host || '%'";
        mDb.execSQL(q);
    }

    public void insertHostIntoKnownHosts(String host){
        String q = "INSERT INTO t_known_hosts(host)" +
                "VALUES ('" + host + "')";
        mDb.execSQL(q);
    }

    public void updateData(String ifce, String host) {
        try{
            String q = "UPDATE t_data_split " +
                    "SET interface = '" + ifce + "', " +
                    "host = '" + host + "'" +
                    "WHERE " +
                    "interface IS NULL AND " +
                    "host IS NULL";
            mDb.execSQL(q);
            Log.d("TESTING", "interface data updated in t_data_split for " + host + " and " + ifce);
        }catch(Exception ex){
            Log.e("Error updating table", ex.toString());
        }
    }

    public void insertServer(String name, String os, String host, String username, String password, int port ) {
        String q = "INSERT INTO t_servers(name,os,host,username,password,port) " +
                "VALUES('" + name + "','" + os + "','" + host + "','" + username + "','" + password + "'," + port + ");";
        mDb.execSQL(q);
    }

    public void updateServer(String name, String os, String host, String username, String password, int port, String nameNew, String osNew, String hostNew, String usernameNew, String passwordNew, int portNew){
        String q = "UPDATE t_servers " +
                "SET name = '" + nameNew + "', " +
                "os = '" + osNew + "', " +
                "host = '" + hostNew + "', " +
                "username = '" + usernameNew + "', " +
                "password = '" + passwordNew + "', " +
                "port = " + portNew + " " +
                "WHERE " +
                "name = '" + name + "' AND " +
                "os = '" + os + "' AND " +
                "host = '" + host + "' AND " +
                "username = '" + username + "' AND " +
                "password = '" + password + "' AND " +
                "port = " + port;
        mDb.execSQL(q);
    }

    public void insertInterfaces(List<String> line) {
        try{
            for (String aLine : line) {
                String q = "INSERT INTO t_interfaces(interface) VALUES('" + aLine + "')";
                Log.d("TEST1",aLine);
                mDb.execSQL(q);
            }
        }catch(Exception ex){
            Log.e("Error inserting array", ex.toString());
        }
    }

    public void updateInterfaceTable(String host){
        try{
            String q = "UPDATE t_interfaces SET host = '" + host + "' WHERE host IS NULL;";
            mDb.execSQL(q);
        }catch(Exception ex){
            Log.e("Error updating table",ex.toString());
        }
    }

    public void deleteServer(String name, String host, String username, String password) {
        String q = "DELETE FROM t_servers " +
                "WHERE " +
                "name = '" + name + "' AND " +
                "host = '" + host + "' AND " +
                "username = '" + username + "' AND " +
                "password = '" + password + "';";
        mDb.execSQL(q);
    }

    public void deleteAllServers() {
        String q = "DELETE FROM t_servers";
        mDb.execSQL(q);
    }
    public void deleteAllData() {
        String q = "DELETE FROM t_data_split";
        mDb.execSQL(q);
    }

    public void deleteAllInterfaces() {
        String q = "DELETE FROM t_interfaces";
        mDb.execSQL(q);
    }
}