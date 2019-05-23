package com.odnovolov.forgetmenot.presentation.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import androidx.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle

fun AndroidLifecycle.adaptForBinder(): BinderLifecycle =
    Observable.create { emitter: ObservableEmitter<Event> ->
        this.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                emitter.onNext(Event.BEGIN)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                emitter.onNext(Event.END)
            }
        })
    }
        .to { androidState: Observable<Event> ->
            BinderLifecycle.wrap(androidState)
        }