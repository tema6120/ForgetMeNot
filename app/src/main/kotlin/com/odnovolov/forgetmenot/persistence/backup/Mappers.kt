package com.odnovolov.forgetmenot.persistence.backup

import com.odnovolov.forgetmenot.persistence.*
import com.odnovolov.forgetmenot.persistence.backup.v8.*
import com.odnovolov.forgetmenot.persistence.globalstate.*

fun CardDb.toBackupCardV8() = BackupCardV8(
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

fun DeckDb.toBackupDeckV8() = BackupDeckV8(
    id,
    name,
    createdAt,
    lastTestedAt,
    exercisePreferenceId,
    isPinned
)

fun DeckListDb.toBackupDeckListV8() = BackupDeckListV8(
    id,
    name,
    color,
    deckIds
)

fun DeckReviewPreferenceDb.toBackupDeckReviewPreferenceV8() = BackupDeckReviewPreferenceV8(
    id,
    deckListId,
    deckSortingCriterion,
    deckSortingDirection,
    newDecksFirst,
    displayOnlyDecksAvailableForExercise
)

fun ExercisePreferenceDb.toBackupExercisePreferenceV8() = BackupExercisePreferenceV8(
    id,
    name,
    randomOrder,
    pronunciationId,
    cardInversion,
    isQuestionDisplayed,
    testingMethod,
    intervalSchemeId,
    gradingId,
    timeForAnswer,
    pronunciationPlanId
)

fun FileFormatDb.toBackupFileFormatV8() = BackupFileFormatV8(
    id,
    name,
    extension,
    delimiter,
    trailingDelimiter,
    quoteCharacter,
    quoteMode,
    escapeCharacter,
    nullString,
    ignoreSurroundingSpaces,
    trim,
    ignoreEmptyLines,
    recordSeparator,
    commentMarker,
    skipHeaderRecord,
    header,
    ignoreHeaderCase,
    allowDuplicateHeaderNames,
    allowMissingColumnNames,
    headerComments,
    autoFlush
)

fun GradingDb.toBackupGradingV8() = BackupGradingV8(
    id,
    onFirstCorrectAnswer,
    onFirstWrongAnswer,
    askAgain,
    onRepeatedCorrectAnswer,
    onRepeatedWrongAnswer
)

fun IntervalDb.toBackupIntervalV8() = BackupIntervalV8(
    id,
    intervalSchemeId,
    grade,
    value
)

fun KeyGestureMapDb.toBackupKeyGestureMapV8() = BackupKeyGestureMapV8(
    keyGesture,
    keyGestureAction
)

fun KeyValue.toBackupKeyValueV8() = BackupKeyValueV8(
    key,
    value
)

fun PronunciationDb.toBackupPronunciationV8() = BackupPronunciationV8(
    id,
    questionLanguage,
    questionAutoSpeaking,
    answerLanguage,
    answerAutoSpeaking,
    speakTextInBrackets
)

fun PronunciationPlanDb.toBackupPronunciationPlanV8() = BackupPronunciationPlanV8(
    id,
    pronunciationEvents
)

fun TipStateDb.toBackupTipStateV8() = BackupTipStateV8(
    id,
    needToShow,
    lastShowedAt
)