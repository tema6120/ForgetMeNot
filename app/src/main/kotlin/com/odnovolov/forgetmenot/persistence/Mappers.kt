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
    grade,
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
    testingMethod,
    intervalScheme?.id,
    pronunciation.id,
    isQuestionDisplayed,
    cardInversion,
    pronunciationPlan.id,
    timeForAnswer
)

fun IntervalSchemeDb.toIntervalScheme(
    intervals: CopyableList<Interval>
) = IntervalScheme(
    id,
    intervals
)

fun IntervalScheme.toIntervalSchemeDb(): IntervalSchemeDb = IntervalSchemeDb.Impl(
    id,
    ""
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
    grade,
    value
)

fun PronunciationDb.toPronunciation() = Pronunciation(
    id,
    questionLanguage,
    questionAutoSpeak,
    answerLanguage,
    answerAutoSpeak,
    speakTextInBrackets
)

fun Pronunciation.toPronunciationDb(): PronunciationDb = PronunciationDb.Impl(
    id,
    "",
    questionLanguage,
    questionAutoSpeak,
    answerLanguage,
    answerAutoSpeak,
    speakTextInBrackets
)

fun PronunciationPlanDb.toPronunciationPlan() = PronunciationPlan(
    id,
    pronunciationEvents
)

fun PronunciationPlan.toPronunciationPlanDb(): PronunciationPlanDb = PronunciationPlanDb.Impl(
    id,
    "",
    pronunciationEvents
)

fun DeckReviewPreferenceDb.toDeckReviewPreference() = DeckReviewPreference(
    deckSorting,
    displayOnlyWithTasks
)

fun CardFilterForAutoplay.toRepetitionSettingDb(): RepetitionSettingDb = RepetitionSettingDb.Impl(
    id,
    "",
    isAvailableForExerciseCardsIncluded,
    isAwaitingCardsIncluded,
    isLearnedCardsIncluded,
    gradeRange.first,
    gradeRange.last,
    lastTestedFromTimeAgo,
    lastTestedToTimeAgo,
    0
)

fun RepetitionSettingDb.toCardFilterForAutoplay() = CardFilterForAutoplay(
    id,
    isAvailableForExerciseCardsIncluded,
    isAwaitingCardsIncluded,
    isLearnedCardsIncluded,
    levelOfKnowledgeMin..levelOfKnowledgeMax,
    lastAnswerFromTimeAgo,
    lastAnswerToTimeAgo
)

fun FullscreenPreferenceDb.toFullscreenPreference() = FullscreenPreference(
    isEnabledInHomeAndSettings,
    isEnabledInExercise,
    isEnabledInRepetition
)