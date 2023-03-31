package com.odnovolov.forgetmenot.persistence.backup.serializers

import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.MonthSpan
import com.soywiz.klock.TimeSpan
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DateTimeSpanSerializer : KSerializer<DateTimeSpan> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DateTimeSpan", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTimeSpan) {
        val string = "${value.monthSpan.totalMonths}|${value.timeSpan.millisecondsLong}"
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): DateTimeSpan {
        val string = decoder.decodeString()
        val chunks = string.split("|")
        val totalMonths: Int = chunks[0].toInt()
        val monthSpan = MonthSpan(totalMonths)
        val milliseconds: Double = chunks[1].toDouble()
        val timeSpan = TimeSpan(milliseconds)
        return DateTimeSpan(monthSpan, timeSpan)
    }
}