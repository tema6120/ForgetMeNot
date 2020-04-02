package com.odnovolov.forgetmenot.domain.architecturecomponents

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object PropertyChangeRegistry {
    private var changes: MutableList<Change> = LinkedList()

    fun <PropertyValue> add(
        propertyOwnerClass: KClass<*>,
        propertyOwnerId: Long,
        property: KProperty<*>,
        oldValue: PropertyValue,
        newValue: PropertyValue
    ) {
        changes.add(
            when {
                oldValue === newValue -> {
                    TheSameValueAssignment(
                        propertyOwnerClass,
                        propertyOwnerId,
                        property,
                        newValue.copyIfAble()
                    )
                }
                oldValue is Collection<*> && newValue is Collection<*> -> {
                    val collectionDiffResult: CollectionDiffResult<*> =
                        calculateCollectionDiff(oldValue, newValue)
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
        )
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