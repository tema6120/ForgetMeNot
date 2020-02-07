package com.odnovolov.forgetmenot.domain.architecturecomponents

import java.util.*

fun <Item> calculateDiff(
    oldItems: Collection<Item>,
    newItems: Collection<Item>
): DiffResult<Item> {
    val removedItems = LinkedList<Item>()
    val addedItems = LinkedList(newItems)
    oldItems.forEach { oldItem: Item ->
        if(!addedItems.remove(oldItem))
            removedItems.add(oldItem)
    }
    return DiffResult(
        removedItems,
        addedItems
    )
}

class DiffResult<Item>(
    val removedItems: LinkedList<Item>,
    val addedItems: LinkedList<Item>
)