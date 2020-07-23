package com.odnovolov.forgetmenot.presentation.common.di

import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class DiScopeManager<DiScope> {
    @Volatile
    protected var diScope: DiScope? = null

    fun open(create: () -> DiScope) {
        GlobalScope.launch(businessLogicThread) {
            diScope = create()
        }
    }

    fun isOpen(): Boolean = diScope != null

    suspend fun isOpenAsync(): Boolean {
        return withContext(businessLogicThread) {
            diScope != null
        }
    }

    fun reopenIfClosed() {
        GlobalScope.launch(businessLogicThread) {
            if (diScope == null) {
                diScope = recreateDiScope()
            }
        }
    }

    suspend fun getAsync(): DiScope {
        return diScope ?: withContext(businessLogicThread) {
            diScope ?: error("DiScope is not opened")
        }
    }

    fun get(): DiScope {
        return diScope ?: error("DiScope is not opened")
    }

    fun close() {
        GlobalScope.launch(businessLogicThread) {
            diScope?.let(::onCloseDiScope)
            diScope = null
        }
    }

    protected abstract fun recreateDiScope(): DiScope

    protected open fun onCloseDiScope(diScope: DiScope) {}
}