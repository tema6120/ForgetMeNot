package com.odnovolov.forgetmenot.domain.architecturecomponents

import java.util.*

fun <Item> calculateCollectionDiff(
    oldItems: Collection<Item>,
    newItems: Collection<Item>
): CollectionDiffResult<Item> {
    val removedItems = ArrayList<Item>()
    val addedItems = LinkedList(newItems)
    for (oldItem in oldItems) {
        if (!addedItems.remove(oldItem))
            removedItems.add(oldItem)
    }
    return CollectionDiffResult(
        removedItems,
        addedItems
    )
}

class CollectionDiffResult<Item>(
    val removedItems: List<Item>,
    val addedItems: List<Item>
)

fun <Item> calculateListDiff(
    oldItems: List<Item>,
    newItems: List<Item>
): ListDiffResult<Item> {
    val removedItems = HashMap<Int, Item>()
    val addedItems = HashMap<Int, Item>()
    val minSize: Int = minOf(oldItems.size, newItems.size)
    repeat(minSize) { index ->
        val oldItem = oldItems[index]
        val newItem = newItems[index]
        if (oldItem != newItem) {
            removedItems[index] = oldItem
            addedItems[index] = newItem
        }
    }
    for (index in minSize until oldItems.size) {
        removedItems[index] = oldItems[index]
    }
    for (index in minSize until newItems.size) {
        addedItems[index] = newItems[index]
    }
    val movedItemsAt = HashMap<Int, Int>()
    val removedItemsAt = ArrayList<Int>()
    for ((removedItemIndex, removedItem) in removedItems) {
        var movedIndex: Int? = null
        for ((addedItemIndex, addedItem) in addedItems) {
            if (removedItem == addedItem) {
                movedIndex = addedItemIndex
                break
            }
        }
        if (movedIndex != null) {
            movedItemsAt[removedItemIndex] = movedIndex
            addedItems.remove(movedIndex)
        } else {
            removedItemsAt.add(removedItemIndex)
        }
    }
    return ListDiffResult(
        removedItemsAt,
        movedItemsAt,
        addedItems
    )
}

class ListDiffResult<Item>(
    val removedItemsAt: List<Int>,
    val movedItemsAt: Map<Int, Int>,
    val addedItems: Map<Int, Item>
)