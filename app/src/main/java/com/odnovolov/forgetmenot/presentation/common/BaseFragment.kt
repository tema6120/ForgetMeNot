package com.odnovolov.forgetmenot.presentation.common

import androidx.fragment.app.Fragment
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

abstract class BaseFragment<ViewState : Any, UiEvent : Any, News : Any>
    : Fragment(), Consumer<ViewState>, ObservableSource<UiEvent> {

    val newsConsumer: Consumer<News> = Consumer(::acceptNews)
    private val subject = PublishSubject.create<UiEvent>()

    open fun acceptNews(news: News) {
    }

    fun emitEvent(event: UiEvent) {
        subject.onNext(event)
    }

    override fun subscribe(observer: Observer<in UiEvent>) {
        subject.subscribe(observer)
    }
}