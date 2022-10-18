package com.lytics.android.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * A helper for managing the SQLite database event queue
 */
internal class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "lytics-sdk.db"
        const val DATABASE_VERSION = 1
    }

    /**
     * When the database is created, execute the SQL to create the events queue table
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(EventTable.SQL_CREATE_EVENTS)
    }

    /**
     * On upgrade, for now, just drop the events queue table and re-create it
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(EventTable.SQL_DROP_EVENTS)
        onCreate(db)
    }
}

