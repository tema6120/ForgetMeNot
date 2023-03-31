package com.odnovolov.forgetmenot.persistence.backup.v8

import kotlinx.serialization.Serializable

@Serializable
data class BackupV8(
    val cards: List<BackupCardV8>,
    val decks: List<BackupDeckV8>,
    val deckLists: List<BackupDeckListV8>,
    val deckReviewPreferences: List<BackupDeckReviewPreferenceV8>,
    val exercisePreferences: List<BackupExercisePreferenceV8>,
    val fileFormats: List<BackupFileFormatV8>,
    val gradings: List<BackupGradingV8>,
    val intervals: List<BackupIntervalV8>,
    val intervalSchemes: List<BackupIntervalSchemeV8>,
    val keyGestures: List<BackupKeyGestureMapV8>,
    val keyValues: List<BackupKeyValueV8>,
    val pronunciations: List<BackupPronunciationV8>,
    val pronunciationPlans: List<BackupPronunciationPlanV8>,
    val sharedExercisePreferences: List<BackupSharedExercisePreferenceV8>,
    val tipStates: List<BackupTipStateV8>
)