package com.odnovolov.forgetmenot.domain.architecturecomponents

interface Copyable {
    fun copy(): Copyable
}

//inline fun <reified T : Copyable> T.copyReified(): T = copy() as T