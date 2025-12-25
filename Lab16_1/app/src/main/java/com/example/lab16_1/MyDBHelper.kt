package com.example.lab16_1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDBHelper(
    context: Context,
    name: String = DB_NAME,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = VERSION
) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private const val DB_NAME = "myDatabase"
        private const val VERSION = 1

        const val TABLE_NAME = "myTable"
        const val COL_BOOK = "book"
        const val COL_PRICE = "price"

        private const val CREATE_TABLE_SQL =
            "CREATE TABLE $TABLE_NAME ($COL_BOOK TEXT PRIMARY KEY, $COL_PRICE INTEGER NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}
