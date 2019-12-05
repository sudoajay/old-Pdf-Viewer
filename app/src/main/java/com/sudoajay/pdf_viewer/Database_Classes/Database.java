package com.sudoajay.pdf_viewer.Database_Classes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database.db";
    private static final String DATABASE_TABLE_NAME = "BackgroundTimerDATABASE_TABLE_NAME";
    private static final String col_1 = "ID";
    private static final String col_2 = "Name";
    private static final String col_3 = "Path";
    private static final int DATABASE_VERSION = 1;


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DATABASE_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , " +
                " Name TEXT ,Path TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME);
        onCreate(db);
    }

    public void deleteData() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from "+ DATABASE_TABLE_NAME);
    }

    public void fill(final String name,final String Path) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_2, name);
        contentValues.put(col_3, Path);
        sqLiteDatabase.insert(DATABASE_TABLE_NAME, null, contentValues);
    }

    public boolean isEmpty() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery("select * from " + DATABASE_TABLE_NAME, null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        return count <= 0;
    }


    public Cursor getSize() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE_NAME, null);
    }

    public Cursor getValue(final String filter) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT Path FROM "+ DATABASE_TABLE_NAME +" WHERE name Like '%" + filter + "%'"
              , null);
    }


    public Cursor getPath() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT path FROM " + DATABASE_TABLE_NAME, null);
    }


    public void UpdateTheTable(final String id, final String name,final String Path) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_1, id);
        contentValues.put(col_2, name);
        contentValues.put(col_3, Path);
        sqLiteDatabase.update(DATABASE_TABLE_NAME, contentValues, "ID = ?", new String[]{id});
    }


}
