package com.odnovolov.forgetmenot.presentation.di

import java.lang.ref.WeakReference

object ComponentStore {
    val components: MutableSet<WeakReference<Any>> = mutableSetOf()

    fun keep(component: Any) {
        components.add(WeakReference(component))
    }

    inline fun <reified DesiredComponentType> find(): DesiredComponentType {
        val result = components.find { componentWeak ->
            val component = componentWeak.get()
            component is DesiredComponentType
        }
        result ?: throw NoSuchElementException("Component Store doesn't keep such component")
        return result.get() as DesiredComponentType
    }
}