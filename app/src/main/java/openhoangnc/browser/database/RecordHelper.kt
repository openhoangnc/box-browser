package openhoangnc.browser.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import openhoangnc.browser.unit.RecordUnit

class RecordHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(RecordUnit.CREATE_HISTORY)
        database.execSQL(RecordUnit.CREATE_WHITELIST)
        database.execSQL(RecordUnit.CREATE_JAVASCRIPT)
        database.execSQL(RecordUnit.CREATE_COOKIE)
        database.execSQL(RecordUnit.CREATE_GRID)
    }

    // UPGRADE ATTENTION!!!
    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val DATABASE_NAME = "Ninja4.db"
        private const val DATABASE_VERSION = 1
    }
}