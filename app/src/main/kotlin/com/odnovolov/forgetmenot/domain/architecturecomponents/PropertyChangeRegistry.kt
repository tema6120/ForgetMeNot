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

    sealed class Change(
        val propertyOwnerClass: KClass<*>,
        val propertyOwnerId: Long,
        val property: KProperty<*>
    ) {
        class TheSameValueAssignment(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            val value: Any?
        ) : Change(propertyOwnerClass, propertyOwnerId, property)

        class CollectionChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            val removedItems: Collection<Any?>,
            val addedItems: Collection<Any?>
        ) : Change(propertyOwnerClass, propertyOwnerId, property)

        class PropertyValueChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            val oldValue: Any?,
            val newValue: Any?
        ) : Change(propertyOwnerClass, propertyOwnerId, property)
    }
}