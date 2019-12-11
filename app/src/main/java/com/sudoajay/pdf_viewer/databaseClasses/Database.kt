package com.sudoajay.pdf_viewer.databaseClasses

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table " + DATABASE_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , " +
                " Name TEXT ,Path TEXT , Size long , Date long)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $DATABASE_TABLE_NAME")
        onCreate(db)
    }

    fun deleteData() {
        val db = this.writableDatabase
        db.execSQL("delete from $DATABASE_TABLE_NAME")
    }

    fun fill(name: String?, Path: String?, Size: Long, Date: Long) {
        val sqLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(col_2, name)
        contentValues.put(col_3, Path)
        contentValues.put(col_4, Size)
        contentValues.put(col_5, Date)
        sqLiteDatabase.insert(DATABASE_TABLE_NAME, null, contentValues)
    }

    val isEmpty: Boolean
        get() {
            val sqLiteDatabase = this.writableDatabase
            @SuppressLint("Recycle") val cursor = sqLiteDatabase.rawQuery("select * from $DATABASE_TABLE_NAME", null)
            cursor.moveToFirst()
            val count = cursor.count
            return count <= 0
        }

    val size: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM $DATABASE_TABLE_NAME", null)
        }

    fun getValue(filter: String): Cursor {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.rawQuery("SELECT Path FROM $DATABASE_TABLE_NAME WHERE name Like '%$filter%'"
                , null)
    }

    val lastModified: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery("SELECT path  FROM $DATABASE_TABLE_NAME ORDER BY Date DESC ", null)
        }

    val pathFromName: Cursor
        get() {
            val sqLiteDatabase = this.writableDatabase
            return sqLiteDatabase.rawQuery("SELECT path  FROM $DATABASE_TABLE_NAME ORDER BY Name ASC ", null)
        }

    fun getpathFromSize(): Cursor {
        val sqLiteDatabase = this.writableDatabase
        return sqLiteDatabase.rawQuery("SELECT path  FROM $DATABASE_TABLE_NAME ORDER BY Size DESC ", null)
    }

    companion object {
        private const val DATABASE_NAME = "Database.db"
        private const val DATABASE_TABLE_NAME = "BackgroundTimerDATABASE_TABLE_NAME"
        private const val col_2 = "Name"
        private const val col_3 = "Path"
        private const val col_4 = "Size"
        private const val col_5 = "Date"
        private const val DATABASE_VERSION = 1
    }
}