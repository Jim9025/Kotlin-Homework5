package com.example.lab16_1

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class MyContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.lab16_1.provider"
        private const val TABLE = "myTable"

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TABLE")

        private const val BOOKS = 1
        private const val BOOK_ID = 2

        private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE, BOOKS)
            addURI(AUTHORITY, "$TABLE/#", BOOK_ID)
        }
    }

    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        dbrw = MyDBHelper(ctx).writableDatabase
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        if (matcher.match(uri) != BOOKS) return null

        val rowId = dbrw.insert(TABLE, null, values)
        if (rowId == -1L) return null

        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(CONTENT_URI, rowId)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (values == null) return 0

        // 建議：selection 用 "book=?"，selectionArgs[0] 放書名
        val count = dbrw.update(TABLE, values, selection, selectionArgs)
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count = dbrw.delete(TABLE, selection, selectionArgs)
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val c: Cursor = dbrw.query(
            TABLE,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        c.setNotificationUri(context?.contentResolver, uri)
        return c
    }

    override fun getType(uri: Uri): String? = null
}
