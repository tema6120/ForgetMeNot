package com.odnovolov.forgetmenot.common.database

import android.database.DatabaseUtils
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase

object DbUtils {
    lateinit var supportDb: SupportSQLiteDatabase

    fun dump(statement: String): String {
        val cursor = supportDb.query(statement)
        var dump = DatabaseUtils.dumpCursorToString(cursor)
        dump = dump.replaceFirst(
            regex = Regex("""android\.database\.sqlite\.SQLiteCursor.*\n"""),
            replacement = "\"$statement\"\n"
        )
        Log.d("sqlite", dump)
        return dump
    }
}