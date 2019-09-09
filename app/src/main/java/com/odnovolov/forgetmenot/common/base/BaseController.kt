package com.odnovolov.forgetmenot.common.base

import com.odnovolov.forgetmenot.common.database.database
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class BaseController<Event, Order> : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
    private val unhandledEventChannel = Channel<Event>()
    private val eventToHandleChannel = Channel<Event>()
    private val nextEventRequestChannel = Channel<Unit>()
    private val orderList: MutableList<Order> = ArrayList()
    private val orderChannel = Channel<Order>()
    val orders: ReceiveChannel<Order> = orderChannel

    init {
        launchEventDispatcher()
        launchEventHandler()
    }

    private fun launchEventDispatcher() = launch {
        val unhandledEventQueue = LinkedList<Event>()
        var isEventHandlerWorking = false
        while (true) {
            select<Unit> {
                unhandledEventChannel.onReceive { newEvent ->
                    evaluateNewEvent(newEvent, unhandledEventQueue)
                    if (!isEventHandlerWorking) {
                        val nextEvent = unhandledEventQueue.poll()
                        if (nextEvent != null) {
                            eventToHandleChannel.send(nextEvent)
                            isEventHandlerWorking = true
                        }
                    }
                }
                nextEventRequestChannel.onReceive {
                    val nextEvent = unhandledEventQueue.poll()
                    if (nextEvent == null) {
                        isEventHandlerWorking = false
                    } else {
                        eventToHandleChannel.send(nextEvent)
                    }
                }
            }
        }
    }

    // override if you need more complicated logic,
    // for example you can remove outdated events or change sequence.
    // Also it is a good place to cancel Job.
    protected open fun evaluateNewEvent(newEvent: Event, unhandledEventQueue: LinkedList<Event>) {
        unhandledEventQueue.addLast(newEvent)
    }

    private fun launchEventHandler() = launch(databaseWriterThread) {
        for (event in eventToHandleChannel) {
            database.transaction {
                handleEvent(event)
            }
            sendOrders()
            nextEventRequestChannel.send(Unit)
        }
    }

    protected abstract fun handleEvent(event: Event)

    protected fun issueOrder(order: Order) {
        orderList.add(order)
    }

    private fun sendOrders() {
        val orderList = this.orderList.toList()
        this.orderList.clear()
        launch {
            orderList.forEach {
                orderChannel.send(it)
            }
        }
    }

    fun dispatch(event: Event) {
        launch {
            unhandledEventChannel.send(event)
        }
    }

    // Only for cancellable coroutines.
    // If coroutine that calls this method is cancelled,
    // it stops working and this event will not dispatch
    suspend fun dispatchSafely(event: Event) {
        unhandledEventChannel.send(event)
    }

    fun dispose() {
        job.cancel()
    }
}

val databaseWriterThread = newSingleThreadContext("Database writer")