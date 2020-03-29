/*
    This file is part of BOX Browser.

    BOX Browser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BOX Browser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */
package openhoangnc.browser.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.preference.PreferenceManager

class BookmarkList(//establish connection with SQLiteDataBase
        private val c: Context?) {
    private class DatabaseHelper internal constructor(context: Context?) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS $dbTable (_id INTEGER PRIMARY KEY autoincrement, pass_title, pass_content, pass_icon, pass_attachment, pass_creation, UNIQUE(pass_content))")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $dbTable")
            onCreate(db)
        }
    }

    private var sqlDb: SQLiteDatabase? = null

    @Throws(SQLException::class)
    fun open() {
        val dbHelper = DatabaseHelper(c)
        sqlDb = dbHelper.writableDatabase
    }

    //insert data
    fun insert(pass_title: String?, pass_content: String?, pass_icon: String, pass_attachment: String, pass_creation: String) {
        if (!isExist(pass_content)) {
            sqlDb!!.execSQL("INSERT INTO pass (pass_title, pass_content, pass_icon, pass_attachment, pass_creation) VALUES('$pass_title','$pass_content','$pass_icon','$pass_attachment','$pass_creation')")
        }
    }

    //check entry already in database or not
    fun isExist(pass_content: String?): Boolean {
        val query = "SELECT pass_title FROM pass WHERE pass_content='$pass_content' LIMIT 1"
        @SuppressLint("Recycle") val row = sqlDb!!.rawQuery(query, null)
        return row.moveToFirst()
    }

    //edit data
    fun update(id: Int, pass_title: String?, pass_content: String?, pass_icon: String?, pass_attachment: String?, pass_creation: String?) {
        sqlDb!!.execSQL("UPDATE $dbTable SET pass_title='$pass_title', pass_content='$pass_content', pass_icon='$pass_icon', pass_attachment='$pass_attachment', pass_creation='$pass_creation'   WHERE _id=$id")
    }

    //delete data
    fun delete(id: Int) {
        sqlDb!!.execSQL("DELETE FROM $dbTable WHERE _id=$id")
    }

    //fetch data
    fun fetchAllData(activity: Context?): Cursor? {
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        val columns = arrayOf("_id", "pass_title", "pass_content", "pass_icon", "pass_attachment", "pass_creation")
        when (sp.getString("sortDBB", "title")) {
            "title" -> return sqlDb!!.query(dbTable, columns, null, null, null, null, "pass_title" + " COLLATE NOCASE DESC;")
            "icon" -> {
                val orderBy = "pass_creation" + " COLLATE NOCASE DESC;" + "," + "pass_title" + " COLLATE NOCASE ASC;"
                return sqlDb!!.query(dbTable, columns, null, null, null, null, orderBy)
            }
        }
        return null
    }

    //fetch data
    fun fetchAllForSearch(): Cursor {
        val columns = arrayOf("pass_title", "pass_content", "pass_icon", "pass_attachment", "pass_creation")
        return sqlDb!!.query(dbTable, columns, null, null, null, null, "pass_title" + " COLLATE NOCASE DESC;")
    }

    //fetch data by filter
    @Throws(SQLException::class)
    fun fetchDataByFilter(inputText: String?, filterColumn: String): Cursor? {
        val row: Cursor?
        var query = "SELECT * FROM $dbTable"
        if (inputText == null || inputText.length == 0) {
            row = sqlDb!!.rawQuery(query, null)
        } else {
            query = "SELECT * FROM $dbTable WHERE $filterColumn like '%$inputText%'"
            row = sqlDb!!.rawQuery(query, null)
        }
        row?.moveToFirst()
        return row
    }

    companion object {
        //define static variable
        private const val dbVersion = 7
        private const val dbName = "pass_DB_v01.db"
        private const val dbTable = "pass"
    }

}