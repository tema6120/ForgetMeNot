package com.odnovolov.forgetmenot.presentation.common

import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class Screen<ViewState : Any, UiEvent : Any, News : Any>(
    initialViewState: ViewState
) {
    // ViewState region

    private val viewStateSubject: BehaviorSubject<ViewState> = BehaviorSubject.createDefault(initialViewState)
    val viewState: ObservableSource<ViewState> = viewStateSubject
    val viewStateConsumer: Consumer<ViewState> = Consumer { viewStateSubject.onNext(it) }

    // UiEvent region

    private val uiEventSubject: PublishSubject<UiEvent> = PublishSubject.create()
    val uiEvent: ObservableSource<UiEvent> = uiEventSubject
    private val uiEventWithViewStateSubject: PublishSubject<UiEventWitViewState<UiEvent, ViewState>> =
        PublishSubject.create()
    val uiEventWithLatestViewState: ObservableSource<UiEventWitViewState<UiEvent, ViewState>> =
        uiEventWithViewStateSubject
    val uiEventConsumer: Consumer<UiEvent> = Consumer(::onUiEvent)

    private fun onUiEvent(uiEvent: UiEvent) {
        uiEventSubject.onNext(uiEvent)
        val latestViewState = viewStateSubject.value!!
        val uiEventWithViewState = UiEventWitViewState(uiEvent, latestViewState)
        uiEventWithViewStateSubject.onNext(uiEventWithViewState)
        val newViewState = createNewViewStateBasedOnNewUiEvent(uiEvent, latestViewState)
        if (newViewState != null) {
            viewStateSubject.onNext(newViewState)
        }
    }

    open fun createNewViewStateBasedOnNewUiEvent(uiEvent: UiEvent, viewState: ViewState): ViewState? {
        return null
    }

    // News region

    private val newsSubject: PublishSubject<News> = PublishSubject.create()
    val news: ObservableSource<News> = newsSubject
    val newsConsumer: Consumer<News> = Consumer(::onNews)

    private fun onNews(news: News) {
        newsSubject.onNext(news)
        val latestViewState = viewStateSubject.value!!
        val newViewState = createNewViewStateBasedOnNews(news, latestViewState)
        if (newViewState != null) {
            viewStateSubject.onNext(newViewState)
        }
    }

    open fun createNewViewStateBasedOnNews(news: News, viewState: ViewState): ViewState? {
        return null
    }
}