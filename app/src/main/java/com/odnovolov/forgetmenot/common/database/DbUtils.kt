package com.odnovolov.forgetmenot.common.database

import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.system.measureTimeMillis

object DbUtils {
    lateinit var supportDb: SupportSQLiteDatabase

    fun dump(statement: String): String {
        lateinit var cursor: Cursor
        val elapsedTime = measureTimeMillis {
            cursor = supportDb.query(statement)
        }
        var dump = DatabaseUtils.dumpCursorToString(cursor)
        dump = dump.replaceFirst(
            regex = Regex("""android\.database\.sqlite\.SQLiteCursor.*\n"""),
            replacement = "\"${statement}\" (${elapsedTime} ms)\n"
        )
        Log.d("sqlite", dump)
        return dump
    }
}