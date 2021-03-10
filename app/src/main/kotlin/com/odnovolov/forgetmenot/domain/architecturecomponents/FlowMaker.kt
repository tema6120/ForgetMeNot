package com.odnovolov.forgetmenot.domain.architecturecomponents

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class FlowMaker<PropertyOwner : FlowMaker<PropertyOwner>> : Flowable<PropertyOwner> {
    private val properties = mutableMapOf<String, WrappingRWProperty<PropertyOwner, *>>()

    fun <PropertyValue> flowOf(
        property: KProperty1<PropertyOwner, PropertyValue>
    ): Flow<PropertyValue> {
        @Suppress("UNCHECKED_CAST")
        return (properties[property.name] as Flowable<PropertyValue>).asFlow()
    }

    protected fun <PropertyValue> flowMaker(
        initialValue: PropertyValue
    ): DelegateProvider<PropertyOwner, PropertyValue> {
        return DelegateProviderImpl(initialValue)
    }

    protected interface DelegateProvider<PropertyOwner, PropertyValue> {
        operator fun provideDelegate(
            thisRef: PropertyOwner,
            prop: KProperty<*>
        ): ReadWriteProperty<PropertyOwner, PropertyValue>
    }

    private inner class DelegateProviderImpl<PropertyValue>(
        private val initialValue: PropertyValue
    ) : DelegateProvider<PropertyOwner, PropertyValue> {
        override fun provideDelegate(
            thisRef: PropertyOwner,
            prop: KProperty<*>
        ): ReadWriteProperty<PropertyOwner, PropertyValue> {
            val property = WrappingRWProperty<PropertyOwner, PropertyValue>(initialValue)
            properties[prop.name] = property
            return property
        }
    }

    private class WrappingRWProperty<PropertyOwner : Any, PropertyValue>(
        value: PropertyValue
    ) : ReadWriteProperty<PropertyOwner, PropertyValue>, Flowable<PropertyValue> {
        private val stateFlow = MutableStateFlow(value)
        val value: PropertyValue get() = stateFlow.value

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
            stateFlow.value = value
        }

        override fun asFlow(): Flow<PropertyValue> = stateFlow
    }

    override fun asFlow(): Flow<PropertyOwner> {
        val propertyFlows: List<Flow<Any?>> = properties.map { it.value.asFlow() }
        return combine(propertyFlows) { this as PropertyOwner }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this::class != other?.let { it::class }) return false
        other as FlowMaker<*>
        for ((key, value) in properties) {
            val otherRwProperty = other.properties[key] ?: return false
            if (value.value != otherRwProperty.value) return false
        }
        return true
    }

    override fun hashCode(): Int {
        return properties.values
            .fold(initial = 0) { acc, rwProperty -> acc * 31 + rwProperty.value.hashCode() }
    }

    override fun toString(): String {
        return properties.entries.joinToString(
            prefix = "(",
            separator = ", ",
            postfix = ")",
            transform = { entry -> "${entry.key}=${entry.value.value}" }
        )
    }
}