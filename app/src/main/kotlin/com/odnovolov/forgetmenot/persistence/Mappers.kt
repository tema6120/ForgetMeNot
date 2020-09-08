package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.globalstate.*
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
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

fun Deck.toDeckDb(): DeckDb = DeckDb.Impl(
    id,
    name,
    createdAt.unixMillisLong,
    lastOpenedAt?.unixMillisLong,
    exercisePreference.id
)

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
): CardDb = CardDb.Impl(
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

fun ExercisePreferenceDb.toExercisePreference(
    intervalScheme: IntervalScheme?,
    pronunciation: Pronunciation,
    pronunciationPlan: PronunciationPlan
) = ExercisePreference(
    id,
    name,
    randomOrder,
    testMethod,
    intervalScheme,
    pronunciation,
    isQuestionDisplayed,
    cardReverse,
    pronunciationPlan,
    timeForAnswer
)

fun ExercisePreference.toExercisePreferenceDb(): ExercisePreferenceDb = ExercisePreferenceDb.Impl(
    id,
    name,
    randomOrder,
    testMethod,
    intervalScheme?.id,
    pronunciation.id,
    isQuestionDisplayed,
    cardReverse,
    pronunciationPlan.id,
    timeForAnswer
)

fun IntervalSchemeDb.toIntervalScheme(
    intervals: CopyableList<Interval>
) = IntervalScheme(
    id,
    name,
    intervals
)

fun IntervalScheme.toIntervalSchemeDb(): IntervalSchemeDb = IntervalSchemeDb.Impl(
    id,
    name
)

fun IntervalDb.toInterval() = Interval(
    id,
    levelOfKnowledge,
    value
)

fun Interval.toIntervalDb(
    intervalSchemeId: Long
): IntervalDb = IntervalDb.Impl(
    id,
    intervalSchemeId,
    levelOfKnowledge,
    value
)

fun PronunciationDb.toPronunciation() = Pronunciation(
    id,
    name,
    questionLanguage,
    questionAutoSpeak,
    answerLanguage,
    answerAutoSpeak,
    speakTextInBrackets
)

fun Pronunciation.toPronunciationDb(): PronunciationDb = PronunciationDb.Impl(
    id,
    name,
    questionLanguage,
    questionAutoSpeak,
    answerLanguage,
    answerAutoSpeak,
    speakTextInBrackets
)

fun PronunciationPlanDb.toPronunciationPlan() = PronunciationPlan(
    id,
    name,
    pronunciationEvents
)

fun PronunciationPlan.toPronunciationPlanDb(): PronunciationPlanDb = PronunciationPlanDb.Impl(
    id,
    name,
    pronunciationEvents
)

fun DeckReviewPreferenceDb.toDeckReviewPreference() = DeckReviewPreference(
    deckSorting,
    displayOnlyWithTasks
)

fun RepetitionSetting.toRepetitionSettingDb(): RepetitionSettingDb = RepetitionSettingDb.Impl(
    id,
    name,
    isAvailableForExerciseCardsIncluded,
    isAwaitingCardsIncluded,
    isLearnedCardsIncluded,
    levelOfKnowledgeRange.first,
    levelOfKnowledgeRange.last,
    lastAnswerFromTimeAgo,
    lastAnswerToTimeAgo,
    numberOfLaps
)

fun RepetitionSettingDb.toRepetitionSetting() = RepetitionSetting(
    id,
    name,
    isAvailableForExerciseCardsIncluded,
    isAwaitingCardsIncluded,
    isLearnedCardsIncluded,
    levelOfKnowledgeMin..levelOfKnowledgeMax,
    lastAnswerFromTimeAgo,
    lastAnswerToTimeAgo,
    numberOfLaps
)

fun FullscreenPreferenceDb.toFullscreenPreference() = FullscreenPreference(
    isEnabledInHomeAndSettings,
    isEnabledInExercise,
    isEnabledInRepetition
)