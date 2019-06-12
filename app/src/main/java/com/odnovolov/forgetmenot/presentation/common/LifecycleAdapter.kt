package com.odnovolov.forgetmenot.presentation.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import androidx.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle

fun AndroidLifecycle.adaptForBinder(lifecycleScope: LifecycleScope): BinderLifecycle =
    Observable.create { emitter: ObservableEmitter<Event> ->
        this.addObserver(object : DefaultLifecycleObserver {

            override fun onCreate(owner: LifecycleOwner) {
                if (lifecycleScope == CREATE_DESTROY) {
                    emitter.onNext(Event.BEGIN)
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                if (lifecycleScope == START_STOP) {
                    emitter.onNext(Event.BEGIN)
                }
            }

            override fun onResume(owner: LifecycleOwner) {
                if (lifecycleScope == RESUME_PAUSE) {
                    emitter.onNext(Event.BEGIN)
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                if (lifecycleScope == RESUME_PAUSE) {
                    emitter.onNext(Event.END)
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (lifecycleScope == START_STOP) {
                    emitter.onNext(Event.END)
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                if (lifecycleScope == CREATE_DESTROY) {
                    emitter.onNext(Event.END)
                }
            }
        })
    }
        .to { androidState: Observable<Event> ->
            BinderLifecycle.wrap(androidState)
        }

enum class LifecycleScope {
    CREATE_DESTROY,
    START_STOP,
    RESUME_PAUSE
}