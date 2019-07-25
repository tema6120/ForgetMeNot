package com.odnovolov.forgetmenot.common

import android.content.Context
import android.content.SharedPreferences

object GlobalInjector {

    fun sharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("App preferences", Context.MODE_PRIVATE)
    }

}