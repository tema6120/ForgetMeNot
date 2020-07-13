package com.odnovolov.forgetmenot.presentation.common

import android.app.Dialog
import android.os.Bundle

class DialogTimeCapsule {
    private val dialogs = HashMap<String, Dialog>()
    private var savedInstanceState: Bundle? = null

    fun register(stateKey: String, dialog: Dialog) {
        dialogs[stateKey] = dialog
        restore(stateKey, dialog)
    }

    fun save(outState: Bundle) {
        dialogs.forEach { (stateKey, dialog) ->
            outState.putBundle(stateKey, dialog.onSaveInstanceState())
        }
    }

    fun restore(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        dialogs.forEach { (stateKey, dialog) -> restore(stateKey, dialog) }
    }

    private fun restore(stateKey: String, dialog: Dialog) {
        savedInstanceState?.run {
            getBundle(stateKey)?.let(dialog::onRestoreInstanceState)
        }
    }
}