package com.example.myfirstapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lucas on 15/09/2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ProfileData.db";
    private static final int DATABASE_VERSION = 1;
    public static final String PROFILE_TABLE_NAME = "profile";
    public static final String PROFILE_COLUMN_ID = "_id";
    public static final String PROFILE_COLUMN_NAME = "name";
    public static final String PROFILE_COLUMN_DESCRIPTION = "description";

    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PROFILE_TABLE_NAME + "(" +
                PROFILE_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                PROFILE_COLUMN_NAME + " TEXT, " +
                PROFILE_COLUMN_DESCRIPTION + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PROFILE_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertProfile(String name, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PROFILE_COLUMN_NAME, name);
        contentValues.put(PROFILE_COLUMN_DESCRIPTION, description);
        db.insert(PROFILE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateProfile(Integer id, String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PROFILE_COLUMN_NAME, name);
        contentValues.put(PROFILE_COLUMN_DESCRIPTION, description);
        db.update(PROFILE_TABLE_NAME, contentValues, PROFILE_COLUMN_ID + " = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Cursor getProfile(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + PROFILE_TABLE_NAME + " WHERE " +
                PROFILE_COLUMN_ID + "=?", new String[] { Integer.toString(id) } );
        return res;
    }

    public Cursor getAllProfiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + PROFILE_TABLE_NAME, null );
        return res;
    }

    public Integer deleteProfile(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PROFILE_TABLE_NAME,
                PROFILE_COLUMN_ID + " = ? ",
                new String[] { Integer.toString(id) });
    }

}