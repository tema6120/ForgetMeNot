package com.odnovolov.forgetmenot.presentation.common

import androidx.lifecycle.MutableLiveData

// Duc - Distinct until changed
class DucLiveData<T> : MutableLiveData<T>() {

    override fun setValue(newValue: T?) {
        if (newValue != value) {
            super.setValue(newValue)
        }
    }
}