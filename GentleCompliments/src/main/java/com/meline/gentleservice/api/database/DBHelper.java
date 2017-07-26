package com.meline.gentleservice.api.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import com.meline.gentleservice.api.objects_model.Compliment;
import com.meline.gentleservice.R;

public class DBHelper extends SQLiteOpenHelper {
    //singleton pattern for DB
    private static DBHelper sInstance;
    private static String[] defaultCompliments;


    public static synchronized DBHelper getInstance(Context context) {

        if (sInstance == null) {
            defaultCompliments = context.getResources().getStringArray(R.array.compliments_array);
            sInstance = new DBHelper(context);
        }

        return sInstance;
    }

    private static final String DB_NAME = "GentleServiceDB.db";
    private static final int DB_CURRENT_VERSION = 1;
    private static final String TABLE_NAME = "compliments";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_IS_LOADED = "isLoaded";
    private static final String COLUMN_IS_CUSTOM = "isCustom";
    private static final String COLUMN_IS_HATED = "isHated";
    private SQLiteDatabase database;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCommand = String.format(
                "create " +
                        "table %s " +
                        "(%s integer primary key autoincrement, " +
                        "%s text not null, " +
                        "%s integer not null, " +
                        "%s integer not null, " +
                        "%s integer not null);",

                TABLE_NAME,
                COLUMN_ID,
                COLUMN_CONTENT,
                COLUMN_IS_LOADED,
                COLUMN_IS_CUSTOM,
                COLUMN_IS_HATED
        );

        db.execSQL(sqlCommand);

        //insert all compliments into local database
        insertAllInDB(db, defaultCompliments);
    }

    private void insertAllInDB(SQLiteDatabase db, String[] compliments) {
        for (String compliment : compliments) {
            ContentValues defaultCompliments = new ContentValues();
            defaultCompliments.put(COLUMN_CONTENT, compliment);
            defaultCompliments.put(COLUMN_IS_LOADED, 0);
            defaultCompliments.put(COLUMN_IS_CUSTOM, 0);
            defaultCompliments.put(COLUMN_IS_HATED, 0);
            db.insertWithOnConflict(TABLE_NAME, null, defaultCompliments,SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(String.format("drop table if exist '%s'", TABLE_NAME));
        onCreate(database);
    }

    private ArrayList<Compliment> getComplements(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) throws ParseException, SQLException {
        String[] columns = new String[]{
                COLUMN_ID,
                COLUMN_CONTENT,
                COLUMN_IS_LOADED,
                COLUMN_IS_CUSTOM,
                COLUMN_IS_HATED
        };

        this.openDB();
        Cursor taskCursor = this.database.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (taskCursor == null) {
            return null;
        }

        ArrayList<Compliment> complimentsFromDB = new ArrayList<>();
        if (taskCursor.moveToFirst()) {
            do {
                int id = taskCursor.getInt(taskCursor.getColumnIndex(COLUMN_ID));
                String content = taskCursor.getString(taskCursor.getColumnIndex(COLUMN_CONTENT));
                boolean isLoaded = taskCursor.getInt(taskCursor.getColumnIndex(COLUMN_IS_LOADED)) == 1;
                boolean isCustom = taskCursor.getInt(taskCursor.getColumnIndex(COLUMN_IS_CUSTOM)) == 1;
                boolean isHated = taskCursor.getInt(taskCursor.getColumnIndex(COLUMN_IS_HATED)) == 1;
                complimentsFromDB.add(new Compliment(id, content, isLoaded, isCustom, isHated));
            } while (taskCursor.moveToNext());
        }
        taskCursor.close();
        this.closeDB();
        return complimentsFromDB;
    }

    private void openDB() throws SQLException {
        database = getWritableDatabase();
    }

    private void closeDB() {
        database.close();
    }

    public ArrayList<Compliment> getLoadableComplements() throws ParseException, SQLException {
        String selection = String.format("%s=0 AND %s=0", COLUMN_IS_LOADED, COLUMN_IS_HATED);
        return getComplements(selection, null, null, null, null);
    }

    public ArrayList<Compliment> getHeatedComplements() throws ParseException, SQLException {
        String selection = String.format("%s=1", COLUMN_IS_HATED);
        return getComplements(selection, null, null, null, null);
    }

    public void addComplement(Compliment complement) throws SQLException {
        this.openDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONTENT, complement.getContent());
        contentValues.put(COLUMN_IS_LOADED, (complement.isLoaded() ? 1 : 0));
        contentValues.put(COLUMN_IS_CUSTOM, (complement.isCustom() ? 1 : 0));
        contentValues.put(COLUMN_IS_HATED, (complement.isHated() ? 1 : 0));
        this.database.insertWithOnConflict(
                TABLE_NAME,
                null,
                contentValues,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        this.closeDB();
    }

    public void makeComplimentLoaded(int id) throws SQLException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IS_LOADED, 1);
        this.openDB();
        database.updateWithOnConflict(
                TABLE_NAME, contentValues,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                SQLiteDatabase.CONFLICT_REPLACE
        );
        this.closeDB();
    }

    public void changeIsHatedStatus(String content, boolean isHated) throws SQLException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IS_HATED, (isHated ? 1 : 0));
        this.openDB();
        database.updateWithOnConflict(
                TABLE_NAME,
                contentValues,
                COLUMN_CONTENT + "=?",
                new String[]{String.valueOf(content)},
                SQLiteDatabase.CONFLICT_REPLACE
        );
        this.closeDB();
    }

    public void resetComplimentsToNotLoaded() throws SQLException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IS_LOADED, 0);
        this.openDB();
        database.updateWithOnConflict(TABLE_NAME, contentValues, null, null, SQLiteDatabase.CONFLICT_REPLACE);
        this.closeDB();
    }

    private void deleteAllDefaultCompliments() throws SQLException {
        this.openDB();
        database.delete(TABLE_NAME, COLUMN_IS_CUSTOM + "=?", new String[]{"0"});
        this.closeDB();
    }

    public void deleteCompliment(String content) throws SQLException {
        this.openDB();
        database.delete(TABLE_NAME, COLUMN_CONTENT + "=?", new String[]{content});
        this.closeDB();
    }

/*
    private void addAllCompliments(String[] compliments) throws SQLException {
        SQLiteDatabase db = getWritableDatabase();
        insertAllInDB(db, compliments);
        db.close();
    }
*/

    /*public void dropDb() throws SQLException{
        this.openDB();
        database.execSQL(String.format("drop table if exist '%s'", TABLE_NAME));
        this.closeDB();
    }

    public void deleteAllCompliments() throws SQLException {
        this.openDB();
        database.delete(TABLE_NAME, null, null);
        this.closeDB();
    }*/
}