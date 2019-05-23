package com.odnovolov.forgetmenot.presentation.common

import androidx.fragment.app.Fragment
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

open class ObservableSourceFragment<UiEvent: Any> : Fragment(), ObservableSource<UiEvent> {

    private val source = PublishSubject.create<UiEvent>()

    fun onNext(event: UiEvent) {
        source.onNext(event)
    }

    override fun subscribe(observer: Observer<in UiEvent>) {
        source.subscribe(observer)
    }
}