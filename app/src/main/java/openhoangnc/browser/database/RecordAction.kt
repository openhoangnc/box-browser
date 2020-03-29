package openhoangnc.browser.database

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.preference.PreferenceManager
import openhoangnc.browser.unit.RecordUnit
import openhoangnc.browser.view.GridItem
import java.util.*

class RecordAction(context: Context?) {
    private var database: SQLiteDatabase? = null
    private val helper: RecordHelper = RecordHelper(context)
    fun open(rw: Boolean) {
        database = if (rw) helper.writableDatabase else helper.readableDatabase
    }

    fun close() {
        helper.close()
    }

    fun addHistory(record: Record?) {
        if (record?.title == null || record.title?.trim { it <= ' ' }!!.isEmpty()
            || record.url == null || record.url?.trim { it <= ' ' }!!.isEmpty()
                || record.time < 0L) {
            return
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, record.title?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_URL, record.url?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_TIME, record.time)
        database?.insert(RecordUnit.TABLE_HISTORY, null, values)
    }

    fun addDomain(domain: String?, table: String?) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_DOMAIN, domain.trim { it <= ' ' })
        database?.insert(table, null, values)
    }

    fun addGridItem(item: GridItem?): Boolean {
        if (item?.title == null || item.title?.trim { it <= ' ' }!!.isEmpty()
            || item.url == null || item.url?.trim { it <= ' ' }!!.isEmpty()
            || item.filename == null || item.filename?.trim { it <= ' ' }!!.isEmpty()
                || item.ordinal < 0) {
            return false
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, item.title?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_URL, item.url?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_FILENAME, item.filename?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_ORDINAL, item.ordinal)
        database?.insert(RecordUnit.TABLE_GRID, null, values)
        return true
    }

    fun updateGridItem(item: GridItem?) {
        if (item?.title == null || item.title?.trim { it <= ' ' }!!.isEmpty()
            || item.url == null || item.url?.trim { it <= ' ' }!!.isEmpty()
            || item.filename == null || item.filename?.trim { it <= ' ' }!!.isEmpty()
                || item.ordinal < 0) {
            return
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, item.title?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_URL, item.url?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_FILENAME, item.filename?.trim { it <= ' ' })
        values.put(RecordUnit.COLUMN_ORDINAL, item.ordinal)
        database?.update(
            RecordUnit.TABLE_GRID,
            values,
            RecordUnit.COLUMN_URL + "=?",
            arrayOf(item.url)
        )
    }

    fun checkHistory(url: String?): Boolean {
        if (url == null || url.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database?.query(
                RecordUnit.TABLE_HISTORY, arrayOf<String?>(RecordUnit.COLUMN_URL),
                RecordUnit.COLUMN_URL + "=?", arrayOf(url.trim { it <= ' ' }),
                null,
                null,
                null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun checkDomain(domain: String?, table: String?): Boolean {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database?.query(
                table, arrayOf<String?>(RecordUnit.COLUMN_DOMAIN),
                RecordUnit.COLUMN_DOMAIN + "=?", arrayOf(domain.trim { it <= ' ' }),
                null,
                null,
                null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun checkGridItem(url: String?): Boolean {
        if (url == null || url.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database?.query(
                RecordUnit.TABLE_GRID, arrayOf<String?>(RecordUnit.COLUMN_URL),
                RecordUnit.COLUMN_URL + "=?", arrayOf(url.trim { it <= ' ' }),
                null,
                null,
                null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun deleteHistoryItemByURL(domain: String?) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_HISTORY + " WHERE " + RecordUnit.COLUMN_URL + " = " + "\"" + domain.trim { it <= ' ' } + "\"")
    }

    fun deleteHistoryItem(record: Record?) {
        if (record == null || record.time <= 0) {
            return
        }
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_HISTORY + " WHERE " + RecordUnit.COLUMN_TIME + " = " + record.time)
    }

    fun deleteGridItem(item: GridItem?) {
        if (item?.url == null || item.url?.trim { it <= ' ' }!!.isEmpty()) {
            return
        }
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_GRID + " WHERE " + RecordUnit.COLUMN_URL + " = " + "\"" + item.url?.trim { it <= ' ' } + "\"")
    }

    fun deleteDomain(domain: String?, table: String?) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        database?.execSQL("DELETE FROM " + table + " WHERE " + RecordUnit.COLUMN_DOMAIN + " = " + "\"" + domain.trim { it <= ' ' } + "\"")
    }

    fun clearHome() {
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_GRID)
    }

    fun clearHistory() {
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_HISTORY)
    }

    fun clearDomains() {
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_WHITELIST)
    }

    fun clearDomainsJS() {
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_JAVASCRIPT)
    }

    fun clearDomainsCookie() {
        database?.execSQL("DELETE FROM " + RecordUnit.TABLE_COOKIE)
    }

    private fun getRecord(cursor: Cursor?): Record {
        val record = Record()
        record.title = cursor?.getString(0)
        record.url = cursor?.getString(1)
        record.time = cursor?.getLong(2)!!
        return record
    }

    private fun getGridItem(cursor: Cursor): GridItem {
        val item = GridItem()
        item.title = cursor.getString(0)
        item.url = cursor.getString(1)
        item.filename = cursor.getString(2)
        item.ordinal = cursor.getInt(3)
        return item
    }

    fun listEntries(activity: Activity?, listAll: Boolean): MutableList<Record> {
        val list: MutableList<Record> = ArrayList()
        var cursor: Cursor?
        if (listAll) { //add startSite
            cursor = database?.query(
                    RecordUnit.TABLE_GRID, arrayOf<String?>(
                    RecordUnit.COLUMN_TITLE,
                    RecordUnit.COLUMN_URL,
                    RecordUnit.COLUMN_FILENAME,
                    RecordUnit.COLUMN_ORDINAL
            ),
                    null,
                    null,
                    null,
                    null,
                    RecordUnit.COLUMN_ORDINAL
            )
            cursor?.moveToFirst()
            while (!cursor?.isAfterLast!!) {
                list.add(getRecord(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            //add bookmarks
            val db = BookmarkList(activity)
            db.open()
            cursor = db.fetchAllForSearch()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(getRecord(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            db.close()
        }
        //add history
        cursor = database?.query(
                RecordUnit.TABLE_HISTORY, arrayOf<String?>(
                RecordUnit.COLUMN_TITLE,
                RecordUnit.COLUMN_URL,
                RecordUnit.COLUMN_TIME
        ),
                null,
                null,
                null,
                null,
                RecordUnit.COLUMN_TIME + " asc"
        )
        cursor?.moveToFirst()
        while (!cursor?.isAfterLast!!) {
            list.add(getRecord(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    fun listDomains(table: String?): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        val cursor = database?.query(
                table, arrayOf<String?>(RecordUnit.COLUMN_DOMAIN),
                null,
                null,
                null,
                null,
                RecordUnit.COLUMN_DOMAIN
        )
                ?: return list
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    fun listGrid(context: Context?): List<GridItem> {
        val list: MutableList<GridItem> = LinkedList()
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val sortBy = sp.getString("sort_startSite", "ordinal")
        val cursor: Cursor?
        cursor = database?.query(
                RecordUnit.TABLE_GRID, arrayOf<String?>(
                RecordUnit.COLUMN_TITLE,
                RecordUnit.COLUMN_URL,
                RecordUnit.COLUMN_FILENAME,
                RecordUnit.COLUMN_ORDINAL
        ),
                null,
                null,
                null,
                null,
                sortBy
        )
        if (cursor == null) {
            return list
        }
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(getGridItem(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

}