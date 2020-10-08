package com.odnovolov.forgetmenot.domain.architecturecomponents

interface CopyableCollection<E : Copyable> : Collection<E>, Copyable {
    override fun copy(): CopyableCollection<E>
}

operator fun <E : Copyable> CopyableCollection<E>.plus(element: E) : CopyableList<E> {
    val result = ArrayList<E>(size + 1)
    result.addAll(this)
    result.add(element)
    return CopyableList(result)
}