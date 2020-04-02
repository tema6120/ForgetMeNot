package com.odnovolov.forgetmenot.domain.architecturecomponents

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object PropertyChangeRegistry {
    private var changes: MutableList<Change> = ArrayList()

    fun <PropertyValue> add(
        propertyOwnerClass: KClass<*>,
        propertyOwnerId: Long,
        property: KProperty<*>,
        oldValue: PropertyValue,
        newValue: PropertyValue,
        preferredChangeClass: KClass<*>?
    ) {
        val change = when (determineChangeClass(oldValue, newValue, preferredChangeClass)) {
            TheSameValueAssignment::class -> {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue.copyIfAble()
                )
            }
            ListChange::class -> {
                val listDiffResult: ListDiffResult<*> =
                    calculateListDiff(oldValue as List<*>, newValue as List<*>)
                val addedItems = HashMap<Int, Any?>().apply {
                    for ((position, item) in listDiffResult.addedItems) {
                        put(position, item.copyIfAble())
                    }
                }
                ListChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    listDiffResult.removedItemsAt,
                    listDiffResult.movedItemsAt,
                    addedItems
                )
            }
            CollectionChange::class -> {
                val collectionDiffResult: CollectionDiffResult<*> =
                    calculateCollectionDiff(oldValue as Collection<*>, newValue as Collection<*>)
                val removedItems = collectionDiffResult.removedItems.map { it.copyIfAble() }
                val addedItems = collectionDiffResult.addedItems.map { it.copyIfAble() }
                CollectionChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    removedItems,
                    addedItems
                )
            }
            else -> {
                PropertyValueChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    oldValue.copyIfAble(),
                    newValue.copyIfAble()
                )
            }
        }
        changes.add(change)
    }

    private fun <PropertyValue> determineChangeClass(
        oldValue: PropertyValue,
        newValue: PropertyValue,
        preferredChangeClass: KClass<*>?
    ): KClass<*> {
        if (oldValue === newValue) return TheSameValueAssignment::class
        if (preferredChangeClass == PropertyValueChange::class) return PropertyValueChange::class
        return when {
            oldValue === newValue -> TheSameValueAssignment::class
            preferredChangeClass == PropertyValueChange::class -> preferredChangeClass
            preferredChangeClass == CollectionChange::class
                    && oldValue is Collection<*>
                    && newValue is Collection<*> -> preferredChangeClass
            preferredChangeClass == ListChange::class
                    && oldValue is List<*>
                    && newValue is List<*> -> preferredChangeClass
            oldValue is List<*> && newValue is List<*> -> ListChange::class
            oldValue is Collection<*> && newValue is Collection<*> -> CollectionChange::class
            else -> PropertyValueChange::class
        }
    }

    private fun Any?.copyIfAble() = if (this is Copyable) copy() else this

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