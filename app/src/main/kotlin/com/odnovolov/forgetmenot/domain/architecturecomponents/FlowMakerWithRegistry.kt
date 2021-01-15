package com.odnovolov.forgetmenot.domain.architecturecomponents

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class FlowMakerWithRegistry<PropertyOwner : FlowMakerWithRegistry<PropertyOwner>>
    : Copyable, Flowable<PropertyOwner> {
    open val id: Long = -1
    private val properties = mutableMapOf<String, WrappingRWProperty<PropertyOwner, *>>()

    fun <PropertyValue> flowOf(
        property: KProperty1<PropertyOwner, PropertyValue>
    ): Flow<PropertyValue> {
        @Suppress("UNCHECKED_CAST")
        return (properties[property.name] as Flowable<PropertyValue>).asFlow()
    }

    protected fun <PropertyValue> flowMaker(
        initialValue: PropertyValue,
        preferredChangeClass: KClass<*>? = null
    ): DelegateProvider<PropertyOwner, PropertyValue> {
        return DelegateProviderImpl(initialValue, preferredChangeClass)
    }

    protected interface DelegateProvider<PropertyOwner, PropertyValue> {
        operator fun provideDelegate(
            thisRef: PropertyOwner,
            prop: KProperty<*>
        ): ReadWriteProperty<PropertyOwner, PropertyValue>
    }

    private inner class DelegateProviderImpl<PropertyValue>(
        private val initialValue: PropertyValue,
        private val preferredChangeClass: KClass<*>?
    ) : DelegateProvider<PropertyOwner, PropertyValue> {
        override fun provideDelegate(
            thisRef: PropertyOwner,
            prop: KProperty<*>
        ): ReadWriteProperty<PropertyOwner, PropertyValue> {
            val property =
                WrappingRWProperty<PropertyOwner, PropertyValue>(
                    value = initialValue,
                    propertyOwnerId = id,
                    preferredChangeClass = preferredChangeClass
                )
            properties[prop.name] = property
            return property
        }
    }

    private class WrappingRWProperty<PropertyOwner : Any, PropertyValue>(
        var value: PropertyValue,
        private val propertyOwnerId: Long,
        private val preferredChangeClass: KClass<*>? = null
    ) : ReadWriteProperty<PropertyOwner, PropertyValue>, Flowable<PropertyValue> {
        private val channels: MutableList<Channel<PropertyValue>> = CopyOnWriteArrayList()

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
            PropertyChangeRegistry.add(
                propertyOwnerClass = thisRef::class,
                propertyOwnerId = propertyOwnerId,
                property = property,
                oldValue = this.value,
                newValue = value,
                preferredChangeClass = preferredChangeClass
            )
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
    }

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
}