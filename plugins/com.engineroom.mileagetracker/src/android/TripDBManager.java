package com.engineroom.mileagetracker;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;

import io.liteglue.SQLCode;
import io.liteglue.SQLColumnType;
import io.liteglue.SQLiteConnection;
import io.liteglue.SQLiteConnector;
import io.liteglue.SQLiteOpenFlags;
import io.liteglue.SQLiteStatement;

import io.sqlc.SQLitePlugin;

public class TripDBManager {
    private static TripDBManager singletonInstance = null;

    public static int INVALID_DB_ID = 0;

    private static SQLiteConnector connector = new SQLiteConnector();

    private SQLiteConnection mydb;

    public class GPSData{
        public int rowID;
        public double distanceSoFar;
        public double latitude;
        public double longitude;
        public double timestamp;
    }

    public static TripDBManager GetInstance(){
        if(singletonInstance == null){
            singletonInstance = new TripDBManager();
        }
        return singletonInstance;
    }

    public void openDB(File dbFile){
        try {
            open(dbFile);
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MileageTracker", "TripDBManager::openDB Exception");
        }
    }

    public void closeDB(){
        closeDatabaseNow();
    }

    public void writeDB(GPSData gpsData){
        Log.w("MileageTracker", "TripDBManager::writeDB");
        try {
            write(gpsData);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MileageTracker", "TripDBManager::writeDB - JSONException");
        }
    }

    public GPSData readDBLatestRow(){
        Log.w("MileageTracker", "TripDBManager::readDBLatestRow");

        JSONArray resultJSON = readLatestRow();

        GPSData gpsData = new GPSData();

        Log.w("MileageTracker", "TripDBManager::readDBLatestRow - resultJSON = " + resultJSON);
        Log.w("MileageTracker", "TripDBManager::readDBLatestRow - resultJSON.length = " + resultJSON.length());
        try {
            JSONObject jsonObject = resultJSON.getJSONObject(0);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - resultJSON[0] = " + jsonObject);

            JSONObject jsonObjectResults = jsonObject.getJSONObject("result");
            JSONObject jsonObjectRows = jsonObjectResults.getJSONArray("rows").getJSONObject(0);
            JSONObject jsonObjectFromString = new JSONObject(jsonObjectRows.get("distance").toString());

            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - jsonObjectResults = " + jsonObjectResults);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - jsonObjectResults.getJSONArray(\"rows\") = " + jsonObjectResults.getJSONArray("rows"));
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - jsonObjectRows = " + jsonObjectRows);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - jsonObjectRows.get(\"id\") = " + jsonObjectRows.get("id"));
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - jsonObjectRows.get(\"distance\") = " + jsonObjectRows.get("distance").toString());
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - jsonObjectFromString = " + jsonObjectFromString);

            gpsData.rowID = jsonObjectRows.getInt("id");
            gpsData.distanceSoFar = jsonObjectFromString.getDouble("distance");
            gpsData.latitude = jsonObjectFromString.getDouble("lat");
            gpsData.longitude = jsonObjectFromString.getDouble("long");
            gpsData.timestamp = jsonObjectFromString.getDouble("timestamp");

            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - gpsData.rowID = " + gpsData.rowID);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - gpsData.distanceSoFar = " + gpsData.distanceSoFar);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - gpsData.latitude = " + gpsData.latitude);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - gpsData.longitude = " + gpsData.longitude);
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - gpsData.timestamp = " + gpsData.timestamp);

        } catch (JSONException e) {
            Log.w("MileageTracker", "TripDBManager::readDBLatestRow - Exception");
            e.printStackTrace();
        }

        return gpsData;
    }

    public void clearDBTable(){
        Log.w("MileageTracker", "TripDBManager::clearDBTable");
        clearTable();
    }

    /**
     * Open a database.
     *
     * @param dbFile   The database File specification
     */
    private void open(File dbFile) throws Exception {
        mydb = connector.newSQLiteConnection(dbFile.getAbsolutePath(),
                SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
        Log.w("MileageTracker", "TripDBManager::open - dbFile = " + dbFile.getAbsolutePath());
    }

    private void write(GPSData gpsData) throws JSONException {
        String[] queryarr = {"INSERT INTO gps_table (distance, lat, long, timestamp) VALUES (?,?,?,?)"};
        JSONArray parameterArray = new JSONArray();
        JSONObject parameters = new JSONObject();

        parameters.put("distance", gpsData.distanceSoFar);
        parameters.put("lat", gpsData.latitude);
        parameters.put("long", gpsData.longitude);
        parameters.put("timestamp", gpsData.timestamp);
        parameterArray.put(parameters);

        JSONArray[] jsonparams = { parameterArray };

        JSONArray result = executeSqlBatch(queryarr, jsonparams);

        Log.w("MileageTracker", "TripDBManager::write - result = " + result);
    }

    private void createTable(){
        String[] queryarr = {"CREATE TABLE IF NOT EXISTS gps_table (id integer primary key, distance double, lat double, long double, timestamp double)"};
        JSONArray[] jsonparams = { new JSONArray() };

        executeSqlBatch(queryarr, jsonparams);
    }

    private void clearTable(){
        String[] queryarr = {"DELETE FROM gps_table"};
        JSONArray[] jsonparams = { new JSONArray() };

        executeSqlBatch(queryarr, jsonparams);
    }

    /**
     * Close a database (in the current thread).
     */
    private void closeDatabaseNow() {
        try {
            if (mydb != null)
                mydb.dispose();
        } catch (Exception e) {
            Log.e(SQLitePlugin.class.getSimpleName(), "couldn't close database, ignoring", e);
        }
    }

    private JSONArray readLatestRow(){
        String[] queryarr = {"SELECT * FROM gps_table WHERE id=(SELECT MAX(id) FROM gps_table)"};
        JSONArray[] jsonparams = { new JSONArray() };

        return executeSqlBatch(queryarr, jsonparams);
    }

    /**
     * Ignore Android bug workaround for NDK version
     */
    private void bugWorkaround() { }

    /**
     * Executes a batch request and sends the results via cbc.
     *
     * @param queryarr   Array of query strings
     * @param jsonparams Array of JSON query parameters
     * @param cbc        Callback context from Cordova API
     */
    JSONArray executeSqlBatch( String[] queryarr, JSONArray[] jsonparams) {
        if (mydb == null) {
            return null;
        }

        int len = queryarr.length;
        JSONArray batchResults = new JSONArray();

        for (int i = 0; i < len; i++) {
            int rowsAffectedCompat = 0;
            boolean needRowsAffectedCompat = false;

            JSONObject queryResult = null;

            String errorMessage = "unknown";
            int sqliteErrorCode = -1;
            int code = 0; // SQLException.UNKNOWN_ERR

            try {
                String query = queryarr[i];

                long lastTotal = mydb.getTotalChanges();
                queryResult = this.executeSQLiteStatement(query, jsonparams[i]);
                long newTotal = mydb.getTotalChanges();
                long rowsAffected = newTotal - lastTotal;

                queryResult.put("rowsAffected", rowsAffected);
                if (rowsAffected > 0) {
                    long insertId = mydb.getLastInsertRowid();
                    if (insertId > 0) {
                        queryResult.put("insertId", insertId);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                sqliteErrorCode = ex.getErrorCode();
                errorMessage = ex.getMessage();
                Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): SQL Error code = " + sqliteErrorCode + " message = " + errorMessage);

                switch(sqliteErrorCode) {
                    case SQLCode.ERROR:
                        code = 5; // SQLException.SYNTAX_ERR
                        break;
                    case 13: // SQLITE_FULL
                        code = 4; // SQLException.QUOTA_ERR
                        break;
                    case SQLCode.CONSTRAINT:
                        code = 6; // SQLException.CONSTRAINT_ERR
                        break;
                    default:
                        /* do nothing */
                }
            } catch (JSONException ex) {
                // NOT expected:
                ex.printStackTrace();
                errorMessage = ex.getMessage();
                code = 0; // SQLException.UNKNOWN_ERR
                Log.e("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): UNEXPECTED JSON Error=" + errorMessage);
            }

            try {
                if (queryResult != null) {
                    JSONObject r = new JSONObject();

                    r.put("type", "success");
                    r.put("result", queryResult);

                    batchResults.put(r);
                } else {
                    JSONObject r = new JSONObject();
                    r.put("type", "error");

                    JSONObject er = new JSONObject();
                    er.put("message", errorMessage);
                    er.put("code", code);
                    r.put("result", er);

                    batchResults.put(r);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                Log.e("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + ex.getMessage());
                // TODO what to do?
            }
        }
        return batchResults;
    }

    /**
     * Get rows results from query cursor.
     *
     * @return results in string form
     */
    private JSONObject executeSQLiteStatement(String query, JSONArray paramsAsJson) throws JSONException, SQLException {
        JSONObject rowsResult = new JSONObject();

        boolean hasRows = false;

        SQLiteStatement myStatement = mydb.prepareStatement(query);

        try {
            String[] params = null;

            params = new String[paramsAsJson.length()];

            for (int i = 0; i < paramsAsJson.length(); ++i) {
                if (paramsAsJson.isNull(i)) {
                    myStatement.bindNull(i + 1);
                } else {
                    Object p = paramsAsJson.get(i);
                    if (p instanceof Float || p instanceof Double)
                        myStatement.bindDouble(i + 1, paramsAsJson.getDouble(i));
                    else if (p instanceof Number)
                        myStatement.bindLong(i + 1, paramsAsJson.getLong(i));
                    else
                        myStatement.bindTextNativeString(i + 1, paramsAsJson.getString(i));
                }
            }

            hasRows = myStatement.step();
        } catch (SQLException ex) {
            ex.printStackTrace();
            String errorMessage = ex.getMessage();
            Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + errorMessage);

            // cleanup statement and throw the exception:
            myStatement.dispose();
            throw ex;
        } catch (JSONException ex) {
            ex.printStackTrace();
            String errorMessage = ex.getMessage();
            Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" + errorMessage);

            // cleanup statement and throw the exception:
            myStatement.dispose();
            throw ex;
        }

        // If query result has rows
        if (hasRows) {
            JSONArray rowsArrayResult = new JSONArray();
            String key = "";
            int colCount = myStatement.getColumnCount();

            // Build up JSON result object for each row
            do {
                JSONObject row = new JSONObject();
                try {
                    for (int i = 0; i < colCount; ++i) {
                        key = myStatement.getColumnName(i);

                        switch (myStatement.getColumnType(i)) {
                            case SQLColumnType.NULL:
                                row.put(key, JSONObject.NULL);
                                break;

                            case SQLColumnType.REAL:
                                row.put(key, myStatement.getColumnDouble(i));
                                break;

                            case SQLColumnType.INTEGER:
                                row.put(key, myStatement.getColumnLong(i));
                                break;

                            case SQLColumnType.BLOB:
                            case SQLColumnType.TEXT:
                            default: // (just in case)
                                row.put(key, myStatement.getColumnTextNativeString(i));
                        }

                    }

                    rowsArrayResult.put(row);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (myStatement.step());

            try {
                rowsResult.put("rows", rowsArrayResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        myStatement.dispose();

        return rowsResult;
    }
}

