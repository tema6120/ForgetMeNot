package com.odnovolov.forgetmenot.domain.architecturecomponents

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class FlowMakerWithRegistry<PropertyOwner : FlowMakerWithRegistry<PropertyOwner>>
    : Copyable, Flowable<PropertyOwner> {
    open val id: Long = -1
    private val properties = mutableMapOf<String, BaseDelegateProvider<PropertyOwner, *>>()

    fun <PropertyValue> flowOf(
        property: KProperty1<PropertyOwner, PropertyValue>
    ): Flow<PropertyValue> {
        @Suppress("UNCHECKED_CAST")
        return (properties[property.name] as Flowable<PropertyValue>).asFlow()
    }

    protected fun <PropertyValue> flowMaker(
        initialValue: PropertyValue
    ): DelegateProvider<PropertyOwner, PropertyValue> {
        return BaseDelegateProvider(id, initialValue, properties)
    }

    protected fun <PropertyValue : Copyable?> flowMakerForCopyable(
        initialValue: PropertyValue
    ): DelegateProvider<PropertyOwner, PropertyValue> {
        return CopyableDelegateProvider(id, initialValue, properties)
    }

    protected fun <CollectionItem> flowMakerForCollection(
        initialValue: Collection<CollectionItem>
    ): DelegateProvider<PropertyOwner, Collection<CollectionItem>> {
        return CollectionDelegateProvider(id, initialValue, properties)
    }

    protected fun <SetItem> flowMakerForSet(
        initialValue: Set<SetItem>
    ): DelegateProvider<PropertyOwner, Set<SetItem>> {
        return SetDelegateProvider(id, initialValue, properties)
    }

    protected fun <
            CollectionItem : Copyable,
            Collection : CopyableCollection<CollectionItem>
            > flowMakerForCopyableCollection(
        initialValue: Collection
    ): DelegateProvider<PropertyOwner, Collection> {
        return CopyableCollectionDelegateProvider(id, initialValue, properties)
    }

    protected fun <ListItem> flowMakerForList(
        initialValue: List<ListItem>
    ): DelegateProvider<PropertyOwner, List<ListItem>> {
        return ListDelegateProvider(id, initialValue, properties)
    }

    protected fun <ListItem : Copyable> flowMakerForCopyableList(
        initialValue: CopyableList<ListItem>
    ): DelegateProvider<PropertyOwner, CopyableList<ListItem>> {
        return CopyableListDelegateProvider(id, initialValue, properties)
    }

    @Suppress("UNCHECKED_CAST")
    override fun asFlow(): Flow<PropertyOwner> {
        val propertyFlows: List<Flow<Any?>> = properties.map { it.value.asFlow() }
        return combine(propertyFlows) { this as PropertyOwner }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this::class != other?.let { it::class }) return false
        other as FlowMakerWithRegistry<*>
        if (this.id != other.id) return false
        for ((key, value) in properties) {
            val otherRwProperty = other.properties[key] ?: return false
            if (value.value != otherRwProperty.value) return false
        }
        return true
    }

    override fun hashCode(): Int {
        return properties.values.fold(initial = id.hashCode()) { acc, rwProperty ->
            acc * 31 + rwProperty.value.hashCode()
        }
    }

    override fun toString(): String {
        return listOf("id=$id")
            .plus(properties.entries.map { entry -> "${entry.key}=${entry.value.value}" })
            .joinToString(prefix = "(", postfix = ")")
    }

    protected interface DelegateProvider<PropertyOwner, PropertyValue> {
        operator fun provideDelegate(
            thisRef: PropertyOwner,
            prop: KProperty<*>
        ): ReadWriteProperty<PropertyOwner, PropertyValue>
    }

    private open class BaseDelegateProvider<PropertyOwner : Any, PropertyValue>(
        private val propertyOwnerId: Long,
        var value: PropertyValue,
        private val properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : DelegateProvider<PropertyOwner, PropertyValue>,
        ReadWriteProperty<PropertyOwner, PropertyValue>,
        Flowable<PropertyValue> {
        private val channels: MutableList<Channel<PropertyValue>> = CopyOnWriteArrayList()

        override fun provideDelegate(
            thisRef: PropertyOwner,
            prop: KProperty<*>
        ): ReadWriteProperty<PropertyOwner, PropertyValue> {
            properties[prop.name] = this
            return this
        }

        override operator fun getValue(
            thisRef: PropertyOwner,
            property: KProperty<*>
        ): PropertyValue {
            return value
        }

        override operator fun setValue(
            thisRef: PropertyOwner,
            property: KProperty<*>,
            value: PropertyValue
        ) {
            val change: Change = calculateChange(
                propertyOwnerClass = thisRef::class,
                propertyOwnerId = propertyOwnerId,
                property = property,
                oldValue = this.value,
                newValue = value
            )
            PropertyChangeRegistry.register(change)
            this.value = value
            channels.forEach { it.offer(value) }
        }

        override fun asFlow(): Flow<PropertyValue> = flow {
            emit(value)
            val channel = Channel<PropertyValue>(Channel.CONFLATED)
            channels.add(channel)
            try {
                for (item: PropertyValue in channel) {
                    emit(item)
                }
            } finally {
                channels.remove(channel)
            }
        }

        protected open fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: PropertyValue,
            newValue: PropertyValue
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue
                )
            } else {
                PropertyValueChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    oldValue,
                    newValue
                )
            }
        }
    }

    private class CopyableDelegateProvider<PropertyOwner : Any, PropertyValue : Copyable?>(
        propertyOwnerId: Long,
        value: PropertyValue,
        properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : BaseDelegateProvider<PropertyOwner, PropertyValue>(
        propertyOwnerId,
        value,
        properties
    ) {
        override fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: PropertyValue,
            newValue: PropertyValue
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue?.copy()
                )
            } else {
                PropertyValueChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    oldValue?.copy(),
                    newValue?.copy()
                )
            }
        }
    }

    private class CollectionDelegateProvider<PropertyOwner : Any, CollectionItem>(
        propertyOwnerId: Long,
        value: Collection<CollectionItem>,
        properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : BaseDelegateProvider<PropertyOwner, Collection<CollectionItem>>(
        propertyOwnerId,
        value,
        properties
    ) {
        override fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: Collection<CollectionItem>,
            newValue: Collection<CollectionItem>
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue
                )
            } else {
                val collectionDiffResult: CollectionDiffResult<CollectionItem> =
                    calculateCollectionDiff(oldValue, newValue)
                CollectionChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    collectionDiffResult.removedItems,
                    collectionDiffResult.addedItems
                )
            }
        }
    }

    private class SetDelegateProvider<PropertyOwner : Any, SetItem>(
        propertyOwnerId: Long,
        value: Set<SetItem>,
        properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : BaseDelegateProvider<PropertyOwner, Set<SetItem>>(
        propertyOwnerId,
        value,
        properties
    ) {
        override fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: Set<SetItem>,
            newValue: Set<SetItem>
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue
                )
            } else {
                val collectionDiffResult: CollectionDiffResult<SetItem> =
                    calculateCollectionDiff(oldValue, newValue)
                CollectionChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    collectionDiffResult.removedItems,
                    collectionDiffResult.addedItems
                )
            }
        }
    }

    private class CopyableCollectionDelegateProvider<
            PropertyOwner : Any,
            CollectionItem : Copyable,
            Collection : CopyableCollection<CollectionItem>
            >(
        propertyOwnerId: Long,
        value: Collection,
        properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : BaseDelegateProvider<PropertyOwner, Collection>(
        propertyOwnerId,
        value,
        properties
    ) {
        override fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: Collection,
            newValue: Collection
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue.copy()
                )
            } else {
                val collectionDiffResult: CollectionDiffResult<CollectionItem> =
                    calculateCollectionDiff(oldValue, newValue)
                val removedItems = collectionDiffResult.removedItems.map { it.copy() }
                val addedItems = collectionDiffResult.addedItems.map { it.copy() }
                CollectionChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    removedItems,
                    addedItems
                )
            }
        }
    }

    private class ListDelegateProvider<PropertyOwner : Any, ListItem>(
        propertyOwnerId: Long,
        value: List<ListItem>,
        properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : BaseDelegateProvider<PropertyOwner, List<ListItem>>(
        propertyOwnerId,
        value,
        properties
    ) {
        override fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: List<ListItem>,
            newValue: List<ListItem>
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue
                )
            } else {
                val listDiffResult: ListDiffResult<ListItem> = calculateListDiff(oldValue, newValue)
                ListChange(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    listDiffResult.removedItemsAt,
                    listDiffResult.movedItemsAt,
                    listDiffResult.addedItems
                )
            }
        }
    }

    private class CopyableListDelegateProvider<PropertyOwner : Any, ListItem : Copyable>(
        propertyOwnerId: Long,
        value: CopyableList<ListItem>,
        properties: MutableMap<String, BaseDelegateProvider<PropertyOwner, *>>
    ) : BaseDelegateProvider<PropertyOwner, CopyableList<ListItem>>(
        propertyOwnerId,
        value,
        properties
    ) {
        override fun calculateChange(
            propertyOwnerClass: KClass<*>,
            propertyOwnerId: Long,
            property: KProperty<*>,
            oldValue: CopyableList<ListItem>,
            newValue: CopyableList<ListItem>
        ): Change {
            return if (oldValue === newValue) {
                TheSameValueAssignment(
                    propertyOwnerClass,
                    propertyOwnerId,
                    property,
                    newValue.copy()
                )
            } else {
                val listDiffResult: ListDiffResult<ListItem> = calculateListDiff(oldValue, newValue)
                val addedItems = HashMap<Int, Copyable>().apply {
                    for ((position, item: ListItem) in listDiffResult.addedItems) {
                        put(position, item.copy())
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
        }
    }
}