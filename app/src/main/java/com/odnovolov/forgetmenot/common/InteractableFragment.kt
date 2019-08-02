package com.odnovolov.forgetmenot.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

open class InteractableFragment<Request, Result> : Fragment() {
    open fun request(request: Request) {
    }

    private val resultSender = LiveEvent<Result>()
    val result: LiveData<Result> = resultSender

    protected fun sendResult(result: Result) {
        resultSender.send(result)
    }
}