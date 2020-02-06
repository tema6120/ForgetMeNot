package com.odnovolov.forgetmenot.domain.architecturecomponents

import kotlinx.coroutines.flow.Flow

interface Flowable<out T> {
    fun asFlow(): Flow<T>
}