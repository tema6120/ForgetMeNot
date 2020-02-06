package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.common.database.database
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.reflect.KClass

val json = Json(JsonConfiguration.Stable)
val queries = database.serializableQueries

inline fun <reified Serializable> loadSerializable(
    deserializer: DeserializationStrategy<Serializable>
): Serializable? {
    val className: String = Serializable::class.java.name
    val jsonData: String? = queries
        .selectJsonData(className).executeAsOneOrNull()
    return if (jsonData == null) null
    else json.parse(deserializer, jsonData)
}

inline fun <reified Serializable> saveSerializable(
    serializable: Serializable,
    serializer: SerializationStrategy<Serializable>
) {
    val className: String = Serializable::class.java.name
    val jsonData: String = json.stringify(serializer, serializable)
    queries.replace(className, jsonData)
}

fun deleteSerializable(serializableClass: KClass<*>) {
    queries.delete(serializableClass.java.name)
}