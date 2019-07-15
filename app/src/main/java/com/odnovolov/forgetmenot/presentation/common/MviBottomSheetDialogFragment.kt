package com.odnovolov.forgetmenot.presentation.common

import android.os.Bundle
import android.view.View
import com.badoo.mvicore.binder.lifecycle.ManualLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

abstract class MviBottomSheetDialogFragment<ViewState : Any, UiEvent : Any, News : Any>
    : BottomSheetDialogFragment(), Consumer<ViewState>, ObservableSource<UiEvent> {

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

    // binder lifecycle

    val viewLifecycle = ManualLifecycle()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycle.begin()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycle.end()
    }
}