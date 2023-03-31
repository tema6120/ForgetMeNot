package com.odnovolov.forgetmenot.persistence.backup.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

object LocaleSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Locale) {
        val string = value.toLanguageTag()
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Locale {
        val string = decoder.decodeString()
        return Locale.forLanguageTag(string)
    }
}