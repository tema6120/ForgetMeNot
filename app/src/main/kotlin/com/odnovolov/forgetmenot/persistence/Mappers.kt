package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.globalstate.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.soywiz.klock.DateTime

fun DeckDb.toDeck(
    cards: CopyableList<Card>,
    exercisePreference: ExercisePreference
) = Deck(
    id,
    name,
    DateTime.fromUnix(createdAt),
    lastOpenedAt?.let { DateTime.fromUnix(it) },
    cards,
    exercisePreference
)

fun Deck.toDeckDb(): DeckDb {
    return DeckDb.Impl(
        id,
        name,
        createdAt.unixMillisLong,
        lastOpenedAt?.unixMillisLong,
        exercisePreference.id
    )
}

fun CardDb.toCard() = Card(
    id,
    question,
    answer,
    lap,
    isLearned,
    levelOfKnowledge,
    lastAnsweredAt?.let { DateTime.fromUnix(it) }
)

fun Card.toCardDb(
    deckId: Long,
    ordinal: Int
): CardDb {
    return CardDb.Impl(
        id,
        deckId,
        ordinal,
        question,
        answer,
        lap,
        isLearned,
        levelOfKnowledge,
        lastAnsweredAt?.unixMillisLong
    )
}

fun ExercisePreferenceDb.toExercisePreference(
    intervalScheme: IntervalScheme?,
    pronunciation: Pronunciation
) = ExercisePreference(
    id,
    name,
    randomOrder,
    testMethod,
    intervalScheme,
    pronunciation,
    isQuestionDisplayed,
    cardReverse
)

fun ExercisePreference.toExercisePreferenceDb(): ExercisePreferenceDb {
    return ExercisePreferenceDb.Impl(
        id,
        name,
        randomOrder,
        testMethod,
        intervalScheme?.id,
        pronunciation.id,
        isQuestionDisplayed,
        cardReverse
    )
}

fun IntervalSchemeDb.toIntervalScheme(
    intervals: CopyableList<Interval>
) = IntervalScheme(
    id,
    name,
    intervals
)

fun IntervalScheme.toIntervalSchemeDb(): IntervalSchemeDb {
    return IntervalSchemeDb.Impl(
        id,
        name
    )
}

fun IntervalDb.toInterval() = Interval(
    id,
    targetLevelOfKnowledge,
    value
)

fun Interval.toIntervalDb(
    intervalSchemeId: Long
): IntervalDb {
    return IntervalDb.Impl(
        id,
        intervalSchemeId,
        targetLevelOfKnowledge,
        value
    )
}

fun PronunciationDb.toPronunciation() = Pronunciation(
    id,
    name,
    questionLanguage,
    questionAutoSpeak,
    answerLanguage,
    answerAutoSpeak,
    doNotSpeakTextInBrackets
)

fun Pronunciation.toPronunciationDb(): PronunciationDb {
    return PronunciationDb.Impl(
        id,
        name,
        questionLanguage,
        questionAutoSpeak,
        answerLanguage,
        answerAutoSpeak,
        doNotSpeakTextInBrackets
    )
}

fun DeckReviewPreferenceDb.toDeckReviewPreference() = DeckReviewPreference(
    deckSorting,
    displayOnlyWithTasks
)