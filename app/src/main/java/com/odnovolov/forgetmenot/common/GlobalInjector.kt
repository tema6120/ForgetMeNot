package com.odnovolov.forgetmenot.common

import android.content.Context
import android.content.SharedPreferences
import com.odnovolov.forgetmenot.db.AppDatabase

object GlobalInjector {

    fun sharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("App preferences", Context.MODE_PRIVATE)
    }

    fun db(context: Context): AppDatabase {
        return AppDatabase.getInstance(context.applicationContext)
    }

}