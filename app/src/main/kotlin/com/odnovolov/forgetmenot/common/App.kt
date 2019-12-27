package com.odnovolov.forgetmenot.common

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.odnovolov.forgetmenot.common.database.backUpTemporaryTables

class App : Application(), LifecycleObserver {
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d("odnovolov", "onTrimMemory: level = $level")
        if (level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            backUpTemporaryTables()
        }
    }
}