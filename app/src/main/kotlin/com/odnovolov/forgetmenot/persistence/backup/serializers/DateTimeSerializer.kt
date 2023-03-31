package com.odnovolov.forgetmenot.persistence.backup.serializers

import com.soywiz.klock.DateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DateTimeSerializer : KSerializer<DateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: DateTime) {
        val long: Long = value.unixMillisLong
        encoder.encodeLong(long)
    }

    override fun deserialize(decoder: Decoder): DateTime {
        val long: Long = decoder.decodeLong()
        return DateTime.fromUnix(long)
    }
}