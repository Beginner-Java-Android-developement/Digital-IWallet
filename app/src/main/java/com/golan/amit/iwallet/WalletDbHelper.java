package com.golan.amit.iwallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WalletDbHelper extends SQLiteOpenHelper {

    public static final String DATABASENAME = "wallet.db";
    public static final String TABLE = "tblproducts";
    public static final int DATABASEVERSION = 1;
    public static final String ID_COLUMN = "id";
    public static final String DEPOSIT_COLUMN = "deposit";
    public static final String DRAW_COLUMN = "draw";
    public static final String DATETIME_COLUMN = "curr_datetime";

    SQLiteDatabase database;

    public static final String CREATE_TABLE_MONEY =
            "CREATE TABLE IF NOT EXISTS " + TABLE +
                    "(" + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DEPOSIT_COLUMN + " INTEGER," +
                    DRAW_COLUMN + " INTEGER," +
                    DATETIME_COLUMN + " DATE);";

    String[] allColumns = {
            ID_COLUMN, DEPOSIT_COLUMN, DRAW_COLUMN, DATETIME_COLUMN
    };

    public WalletDbHelper(Context context) {
        super(context, DATABASENAME, null, DATABASEVERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if(MainActivity.DEBUG) {
            Log.i(MainActivity.DEBUGTAG, "create string: {" + CREATE_TABLE_MONEY + "}");
        }
        try {
            db.execSQL(CREATE_TABLE_MONEY);
            if(MainActivity.DEBUG) {
                Log.i(MainActivity.DEBUGTAG, "database created");
            }
        } catch (Exception edb) {
            Log.e(MainActivity.DEBUGTAG, "database creation exception: " + edb);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void open() {
        database = this.getWritableDatabase();
        if(MainActivity.DEBUG) {
            Log.i(MainActivity.DEBUGTAG, "database connection open");
        }
    }

    public void close() {
        if (database != null) {
            try {
                database.close();
                if(MainActivity.DEBUG) {
                    Log.i(MainActivity.DEBUGTAG, "database connection closed");
                }
            } catch (Exception edbc) {
                Log.e(MainActivity.DEBUGTAG, "database connection close exception: " + edbc);
            }
        } else {
            Log.e(MainActivity.DEBUGTAG, "database is null");
        }
    }

    public void insertDeposit(String aDeposit) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DEPOSIT_COLUMN, aDeposit);
        contentValues.put(DRAW_COLUMN, "0");
        contentValues.put(DATETIME_COLUMN, currentDate());
        long insertedId = -1;
        try {
            insertedId = database.insert(TABLE, null, contentValues);
            if(MainActivity.DEBUG) {
                Log.i(MainActivity.DEBUGTAG, "inserted deposit " + aDeposit + " to db, id is: " + insertedId);
            }
        } catch (Exception eid) {
            Log.e(MainActivity.DEBUGTAG, "insert exception: " + eid);
        }
    }

    public void insertDraw(String aDraw) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DEPOSIT_COLUMN, "0");
        contentValues.put(DRAW_COLUMN, aDraw);
        contentValues.put(DATETIME_COLUMN, currentDate());
        long insertedId = -1;
        try {
            insertedId = database.insert(TABLE, null, contentValues);
            Log.i(MainActivity.DEBUGTAG, "inserted draw " + aDraw + " to db, id is: " + insertedId);
        } catch (Exception eid) {
            Log.e(MainActivity.DEBUGTAG, "insert exception: " + eid);
        }
    }

    public void resetTableToScratch() {
        try {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE);
            if(MainActivity.DEBUG) {
                Log.i(MainActivity.DEBUGTAG, "database dropped");
            }
        } catch (Exception edbd) {
            Log.e(MainActivity.DEBUGTAG, "database drop exception: " + edbd);
        }

        try {
            database.execSQL(CREATE_TABLE_MONEY);
            if(MainActivity.DEBUG) {
                Log.i(MainActivity.DEBUGTAG, "database re-created");
            }
        } catch (Exception edbc) {
            Log.e(MainActivity.DEBUGTAG, "database re-create exception: " + edbc);
        }
    }

    public void displayDatabaseContent() {
        String query = "SELECT * FROM " + TABLE;
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                int deposit = cursor.getInt(cursor.getColumnIndex(DEPOSIT_COLUMN));
                int draw = cursor.getInt(cursor.getColumnIndex(DRAW_COLUMN));
                String currentdate = cursor.getString(cursor.getColumnIndex(DATETIME_COLUMN));

//                String tmpInfoDisplay = String.format("id: %d, deposit: %d, draw: %d, date; %s",
//                        id, deposit, draw, currentdate);
//                Log.i(MainActivity.DEBUGTAG, tmpInfoDisplay);
            }
        } else {
            Log.e(MainActivity.DEBUGTAG, "database is empty, no activity in account");
        }
    }


    public ArrayList<WalletAction> getAllWalletActions() {
        ArrayList<WalletAction> l = new ArrayList<WalletAction>();
        String query = "SELECT * FROM " + TABLE;
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                int deposit = cursor.getInt(cursor.getColumnIndex(DEPOSIT_COLUMN));
                int draw = cursor.getInt(cursor.getColumnIndex(DRAW_COLUMN));
                String currentdate = cursor.getString(cursor.getColumnIndex(DATETIME_COLUMN));
                WalletAction wa = new WalletAction((int)id, deposit, draw, currentdate);
                l.add(wa);
            }
        }
        return l;
    }

    public ArrayList<WalletAction> getAllWalletActionsByFilter(String selection, String orderBy) {
        Cursor cursor = database.query(TABLE, allColumns, selection, null, null, null, orderBy);
        ArrayList<WalletAction> l = new ArrayList<WalletAction>();
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                int deposit = cursor.getInt(cursor.getColumnIndex(DEPOSIT_COLUMN));
                int draw = cursor.getInt(cursor.getColumnIndex(DRAW_COLUMN));
                String currentdate = cursor.getString(cursor.getColumnIndex(DATETIME_COLUMN));
                WalletAction wa = new WalletAction((int)id, deposit, draw, currentdate);
                l.add(wa);
            }
        }
        return l;
    }


    public int depositSum() {
        int totalSum = -1;
        String query = "SELECT SUM(" + DEPOSIT_COLUMN + ") FROM " + TABLE;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToNext()) {
            try {
                totalSum = cursor.getInt(0);
                if(MainActivity.DEBUG) {
                    Log.i(MainActivity.DEBUGTAG, "total deposit sum: " + totalSum);
                }
            } catch (Exception e) {
                Log.e(MainActivity.DEBUGTAG, "total deposit sum exception: " + e);
            }
        }
        return totalSum;
    }

    public int drawSum() {
        int totalSum = -1;
        String query = "SELECT SUM(" + DRAW_COLUMN + ") FROM " + TABLE;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToNext()) {
            try {
                totalSum = cursor.getInt(0);
                Log.i(MainActivity.DEBUGTAG, "total draw sum: " + totalSum);
            } catch (Exception e) {
                Log.e(MainActivity.DEBUGTAG, "total draw sum exception: " + e);
            }
        }
        return totalSum;
    }

    public String lastActivityDate() {
        String tmpDate = null;
        String query = "SELECT " + DATETIME_COLUMN + " FROM " + TABLE +
                " ORDER BY " + DATETIME_COLUMN + " DESC LIMIT 1";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToNext()) {
            try {
                tmpDate = cursor.getString(0);
                Log.d(MainActivity.DEBUGTAG, "select last date :" + tmpDate);
            } catch (Exception e) {
                Log.e(MainActivity.DEBUGTAG, "select last date exception:" + e);
            }
        }
        return tmpDate;
    }


    public int currentMoneyAmount() {
        return depositSum() - drawSum();
    }


    private String currentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public long deleteRecordById(long rowId) {
        return database.delete(TABLE, ID_COLUMN + "=" + rowId, null);
    }
}
