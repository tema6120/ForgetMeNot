package com.odnovolov.forgetmenot.domain.architecturecomponents

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object PropertyChangeRegistry {
    private var changes: MutableList<Change> = ArrayList()

    fun register(change: Change) {
        changes.add(change)
    }

    fun removeAll(): List<Change> {
        val result = changes
        changes = LinkedList()
        return result
    }

    sealed class Change {
        abstract val propertyOwnerClass: KClass<*>
        abstract val propertyOwnerId: Long
        abstract val property: KProperty<*>

        class TheSameValueAssignment(
            override val propertyOwnerClass: KClass<*>,
            override val propertyOwnerId: Long,
            override val property: KProperty<*>,
            val value: Any?
        ) : Change() {
            override fun toString(): String =
                "TheSameValueAssignment(${propertyOwnerClass.simpleName}.${property.name}):\n" +
                        "propertyOwnerId = $propertyOwnerId\n" +
                        "value = $value"
        }

        class ListChange(
            override val propertyOwnerClass: KClass<*>,
            override val propertyOwnerId: Long,
            override val property: KProperty<*>,
            val removedItemsAt: List<Int>,
            val movedItemsAt: Map<Int, Int>,
            val addedItems: Map<Int, Any?>
        ) : Change() {
            override fun toString(): String =
                "ListChange(${propertyOwnerClass.simpleName}.${property.name}):\n" +
                        "propertyOwnerId = $propertyOwnerId\n" +
                        "removedItemsAt = $removedItemsAt\n" +
                        "movedItemsAt = $movedItemsAt\n" +
                        "addedItems = $addedItems"
        }

        class CollectionChange(
            override val propertyOwnerClass: KClass<*>,
            override val propertyOwnerId: Long,
            override val property: KProperty<*>,
            val removedItems: Collection<Any?>,
            val addedItems: Collection<Any?>
        ) : Change() {
            override fun toString(): String =
                "CollectionChange(${propertyOwnerClass.simpleName}.${property.name}):\n" +
                        "propertyOwnerId = $propertyOwnerId\n" +
                        "removedItems = $removedItems\n" +
                        "addedItems = $addedItems"
        }

        class PropertyValueChange(
            override val propertyOwnerClass: KClass<*>,
            override val propertyOwnerId: Long,
            override val property: KProperty<*>,
            val oldValue: Any?,
            val newValue: Any?
        ) : Change() {
            override fun toString(): String =
                "PropertyValueChange(${propertyOwnerClass.simpleName}.${property.name}):\n" +
                        "propertyOwnerId = $propertyOwnerId\n" +
                        "oldValue = $oldValue\n" +
                        "newValue = $newValue"
        }
    }
}