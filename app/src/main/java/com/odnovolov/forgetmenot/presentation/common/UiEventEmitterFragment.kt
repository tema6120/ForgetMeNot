package com.odnovolov.forgetmenot.presentation.common

import androidx.fragment.app.Fragment
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

open class UiEventEmitterFragment<UiEvent: Any> : Fragment(), ObservableSource<UiEvent> {

    private val subject = PublishSubject.create<UiEvent>()

    fun emitEvent(event: UiEvent) {
        subject.onNext(event)
    }

    override fun subscribe(observer: Observer<in UiEvent>) {
        subject.subscribe(observer)
    }
}