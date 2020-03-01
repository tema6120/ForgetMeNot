package com.odnovolov.forgetmenot.persistence

import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.system.measureTimeMillis

object DbUtils {
    lateinit var supportDb: SupportSQLiteDatabase

    fun dump(statement: String): String {
        lateinit var dump: String
        val elapsedTime = measureTimeMillis {
            val cursor: Cursor = supportDb.query(statement)
            cursor.use {
                dump = DatabaseUtils.dumpCursorToString(cursor)
            }
        }
        dump = dump.replaceFirst(
            regex = Regex("""android\.database\.sqlite\.SQLiteCursor.*\n"""),
            replacement = "\"${statement}\" (${elapsedTime} ms)\n"
        )
        Log.d("sqlite", dump)
        return dump
    }
}