package openhoangnc.browser.unit

import openhoangnc.browser.database.Record

object RecordUnit {
    const val TABLE_HISTORY = "HISTORY"
    const val TABLE_WHITELIST = "WHITELIST"
    const val TABLE_JAVASCRIPT = "JAVASCRIPT"
    const val TABLE_COOKIE = "COOKIE"
    const val TABLE_GRID = "GRID"
    const val COLUMN_TITLE = "TITLE"
    const val COLUMN_URL = "URL"
    const val COLUMN_TIME = "TIME"
    const val COLUMN_DOMAIN = "DOMAIN"
    const val COLUMN_FILENAME = "FILENAME"
    const val COLUMN_ORDINAL = "ORDINAL"
    const val CREATE_HISTORY = ("CREATE TABLE "
            + TABLE_HISTORY
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_TIME + " integer"
            + ")")
    const val CREATE_WHITELIST = ("CREATE TABLE "
            + TABLE_WHITELIST
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")")
    const val CREATE_JAVASCRIPT = ("CREATE TABLE "
            + TABLE_JAVASCRIPT
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")")
    const val CREATE_COOKIE = ("CREATE TABLE "
            + TABLE_COOKIE
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")")
    const val CREATE_GRID = ("CREATE TABLE "
            + TABLE_GRID
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_FILENAME + " text,"
            + " " + COLUMN_ORDINAL + " integer"
            + ")")

    @set:Synchronized
    var holder: Record? = null

}