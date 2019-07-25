package com.odnovolov.forgetmenot.common

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

fun SharedPreferences.getMutableLiveData(
    key: String,
    initialValue: Int = 0
): MutableLiveData<Int> {
    return getMutableLiveData(key, initialValue, { it }, { it })
}

fun <T> SharedPreferences.getMutableLiveData(
    key: String,
    initialValue: T,
    toIntFunction: (T) -> Int,
    fromIntFunction: (Int) -> T
): MutableLiveData<T> {
    return InternalLiveData(this, key, toIntFunction, fromIntFunction,
        { this.getInt(key, toIntFunction(initialValue)) },
        { value: Int -> this.edit().putInt(key, value).apply() }
    )
}

private class InternalLiveData<LiveDataValueType, WritableType>(
    val sharedPrefs: SharedPreferences,
    val key: String,
    val toWritableType: (LiveDataValueType) -> WritableType,
    val fromWritableType: (WritableType) -> LiveDataValueType,
    val read: () -> WritableType,
    val write: (WritableType) -> Unit
) : MutableLiveData<LiveDataValueType>() {

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == this.key) {
            setValueFromSharedPrefs()
        }
    }

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        setValueFromSharedPrefs()
    }

    fun setValueFromSharedPrefs() {
        super.setValue(fromWritableType(read()))
    }

    override fun setValue(value: LiveDataValueType?) {
        if (value == null) {
            sharedPrefs.edit()
                .remove(key)
                .apply()
        } else {
            write(toWritableType(value))
        }
    }
}
