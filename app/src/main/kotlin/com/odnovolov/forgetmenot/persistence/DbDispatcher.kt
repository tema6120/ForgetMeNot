package com.odnovolov.forgetmenot.persistence

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

val dbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()