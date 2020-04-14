package com.odnovolov.forgetmenot.presentation.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext

const val BUSINESS_LOGIC_THREAD_NAME = "businessLogicThread"

val businessLogicThread: CoroutineDispatcher = newSingleThreadContext(BUSINESS_LOGIC_THREAD_NAME)