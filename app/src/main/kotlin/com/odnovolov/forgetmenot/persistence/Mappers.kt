package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.globalstate.*

fun DeckDb.toDeck(
    cards: CopyableList<Card>,
    exercisePreference: ExercisePreference
) = Deck(
    id,
    name,
    createdAt,
    lastTestedAt,
    cards,
    exercisePreference
)

fun Deck.toDeckDb(): DeckDb = DeckDb(
    id,
    name,
    createdAt,
    lastTestedAt,
    exercisePreference.id
)

fun CardDb.toCard() = Card(
    id,
    question,
    answer,
    lap,
    isLearned,
    grade,
    lastTestedAt
)

fun Card.toCardDb(
    deckId: Long,
    ordinal: Int
): CardDb = CardDb(
    id,
    deckId,
    ordinal,
    question,
    answer,
    lap,
    isLearned,
    grade,
    lastTestedAt
)

fun ExercisePreferenceDb.toExercisePreference(
    intervalScheme: IntervalScheme?,
    pronunciation: Pronunciation,
    pronunciationPlan: PronunciationPlan
) = ExercisePreference(
    id,
    name,
    randomOrder,
    testingMethod,
    intervalScheme,
    pronunciation,
    isQuestionDisplayed,
    cardInversion,
    pronunciationPlan,
    timeForAnswer
)

fun ExercisePreference.toExercisePreferenceDb(): ExercisePreferenceDb = ExercisePreferenceDb(
    id,
    name,
    randomOrder,
    testingMethod,
    intervalScheme?.id,
    pronunciation.id,
    isQuestionDisplayed,
    cardInversion,
    pronunciationPlan.id,
    timeForAnswer
)

fun IntervalScheme.toIntervalSchemeDb(): IntervalSchemeDb = IntervalSchemeDb(
    id
)

fun IntervalDb.toInterval() = Interval(
    id,
    grade,
    value
)

fun Interval.toIntervalDb(
    intervalSchemeId: Long
): IntervalDb = IntervalDb(
    id,
    intervalSchemeId,
    grade,
    value
)

fun PronunciationDb.toPronunciation() = Pronunciation(
    id,
    questionLanguage,
    questionAutoSpeaking,
    answerLanguage,
    answerAutoSpeaking,
    speakTextInBrackets
)

fun Pronunciation.toPronunciationDb(): PronunciationDb = PronunciationDb(
    id,
    questionLanguage,
    questionAutoSpeaking,
    answerLanguage,
    answerAutoSpeaking,
    speakTextInBrackets
)

fun PronunciationPlanDb.toPronunciationPlan() = PronunciationPlan(
    id,
    pronunciationEvents
)

fun PronunciationPlan.toPronunciationPlanDb(): PronunciationPlanDb = PronunciationPlanDb(
    id,
    pronunciationEvents
)

inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    return enumValues<T>().find { it.name == this }
}