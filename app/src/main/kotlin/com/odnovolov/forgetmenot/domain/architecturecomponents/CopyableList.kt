package com.odnovolov.forgetmenot.domain.architecturecomponents

class CopyableList<E : Copyable>(
    private val listRealization: List<E>
) : List<E> by listRealization, CopyableCollection<E>, Copyable {
    override fun copy(): CopyableList<E> {
        return CopyableList(listRealization.map { it.copy() } as List<E>)
    }

    override fun equals(other: Any?): Boolean = listRealization == other

    override fun hashCode(): Int = listRealization.hashCode()

    override fun toString(): String = listRealization.toString()
}

fun <E : Copyable> copyableListOf(vararg element: E) = CopyableList(listOf(*element))

fun <E : Copyable> List<E>.toCopyableList() = CopyableList(this)