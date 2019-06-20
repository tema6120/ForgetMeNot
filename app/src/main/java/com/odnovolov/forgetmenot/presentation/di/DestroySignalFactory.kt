package com.odnovolov.forgetmenot.presentation.di

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Completable
import io.reactivex.CompletableEmitter

object DestroySignalFactory {

    fun from(lifecycle: Lifecycle): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    emitter.onComplete()
                }
            })
        }
    }
}