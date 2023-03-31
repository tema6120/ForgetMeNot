package com.odnovolov.forgetmenot.persistence.backup.serializers

import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PronunciationEventSerializer : KSerializer<PronunciationEvent> {
    private const val SPEAK_QUESTION: String = "SPEAK_QUESTION"
    private const val SPEAK_ANSWER: String = "SPEAK_ANSWER"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("PronunciationEvent", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PronunciationEvent) {
        val string: String = when (value) {
            SpeakQuestion -> SPEAK_QUESTION
            SpeakAnswer -> SPEAK_ANSWER
            is Delay -> value.timeSpan.seconds.toInt().toString()
        }
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): PronunciationEvent {
        val string: String = decoder.decodeString()
        return when (string) {
            SPEAK_QUESTION -> SpeakQuestion
            SPEAK_ANSWER -> SpeakAnswer
            else -> {
                val timeSpan: TimeSpan = string.toInt().seconds
                Delay(timeSpan)
            }
        }
    }
}