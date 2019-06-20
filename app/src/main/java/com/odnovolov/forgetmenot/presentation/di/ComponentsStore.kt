package com.odnovolov.forgetmenot.presentation.di

import android.annotation.SuppressLint
import io.reactivex.Completable

object ComponentsStore {
    val components: MutableSet<ScopedComponent<*>> = mutableSetOf()

    @SuppressLint("CheckResult")
    fun keep(component: ScopedComponent<*>, destroySignal: Completable? = null) {
        destroySignal?.subscribe {
            component.destroy()
            components.remove(component)
        }
        components.add(component)
    }

    inline fun <reified T> find(): T {
        return components.find { component -> component is T } as T
    }
}