package com.odnovolov.forgetmenot.domain.architecturecomponents

import java.util.*

fun <Item> calculateCollectionDiff(
    oldItems: Collection<Item>,
    newItems: Collection<Item>
): CollectionDiffResult<Item> {
    val removedItems = LinkedList<Item>()
    val addedItems = LinkedList(newItems)
    oldItems.forEach { oldItem: Item ->
        if(!addedItems.remove(oldItem))
            removedItems.add(oldItem)
    }
    return CollectionDiffResult(
        removedItems,
        addedItems
    )
}

class CollectionDiffResult<Item>(
    val removedItems: LinkedList<Item>,
    val addedItems: LinkedList<Item>
)