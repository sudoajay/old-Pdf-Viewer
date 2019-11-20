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
    private static final String col_2 = "Path";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DATABASE_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "Path TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME);
        onCreate(db);
    }

    public void deleteData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE_NAME, "ID =?", new String[]{1 + ""});
    }

    public void FillIt(int Path) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_2, Path);
        sqLiteDatabase.insert(DATABASE_TABLE_NAME, null, contentValues);
    }

    public boolean isEmpty() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery("select * from " + DATABASE_TABLE_NAME, null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        return count <= 0;
    }

    public Cursor GetTheValueFromId() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + DATABASE_TABLE_NAME, null);
    }

    public Cursor GetTheRepeatedlyWeekdays() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT  Repeatedly ,Weekdays FROM " + DATABASE_TABLE_NAME, null);
    }

    public Cursor GetTheChoose_TypeRepeatedlyEndlessly() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("SELECT Repeatedly,Weekdays,Endlessly FROM " + DATABASE_TABLE_NAME, null);
    }


    public void UpdateTheTable(String id, int Path) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_1, id);
        contentValues.put(col_2, Path);

        sqLiteDatabase.update(DATABASE_TABLE_NAME, contentValues, "ID = ?", new String[]{id});
    }


}
