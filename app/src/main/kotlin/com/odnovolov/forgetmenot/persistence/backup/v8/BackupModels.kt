package com.odnovolov.forgetmenot.persistence.backup.v8

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.backup.serializers.DateTimeSerializer
import com.odnovolov.forgetmenot.persistence.backup.serializers.DateTimeSpanSerializer
import com.odnovolov.forgetmenot.persistence.backup.serializers.LocaleSerializer
import com.odnovolov.forgetmenot.persistence.backup.serializers.PronunciationEventSerializer
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import kotlinx.serialization.Serializable
import org.apache.commons.csv.QuoteMode
import java.util.*

@Serializable
data class BackupCardV8(
    val id: Long,
    val deckId: Long,
    val ordinal: Int,
    val question: String,
    val answer: String,
    val lap: Int,
    val isLearned: Boolean,
    val grade: Int,
    @Serializable(with = DateTimeSerializer::class)
    val lastTestedAt: DateTime?
)

@Serializable
data class BackupDeckV8(
    val id: Long,
    val name: String,
    @Serializable(with = DateTimeSerializer::class)
    val createdAt: DateTime?,
    @Serializable(with = DateTimeSerializer::class)
    val lastTestedAt: DateTime?,
    val exercisePreferenceId: Long,
    val isPinned: Boolean
)

@Serializable
data class BackupDeckListV8(
    val id: Long,
    val name: String,
    val color: Int,
    val deckIds: Set<Long>
)

@Serializable
data class BackupDeckReviewPreferenceV8(
    val id: Long,
    val deckListId: Long?,
    val deckSortingCriterion: DeckSorting.Criterion,
    val deckSortingDirection: DeckSorting.Direction,
    val newDecksFirst: Boolean,
    val displayOnlyDecksAvailableForExercise: Boolean
)

@Serializable
data class BackupExercisePreferenceV8(
    val id: Long,
    val name: String,
    val randomOrder: Boolean,
    val pronunciationId: Long,
    val cardInversion: CardInversion,
    val isQuestionDisplayed: Boolean,
    val testingMethod: TestingMethod,
    val intervalSchemeId: Long?,
    val gradingId: Long,
    val timeForAnswer: Int,
    val pronunciationPlanId: Long
)

@Serializable
data class BackupFileFormatV8(
    val id: Long,
    val name: String,
    val extension: String,
    val delimiter: String,
    val trailingDelimiter: Boolean,
    val quoteCharacter: String?,
    val quoteMode: QuoteMode?,
    val escapeCharacter: String?,
    val nullString: String?,
    val ignoreSurroundingSpaces: Boolean,
    val trim: Boolean,
    val ignoreEmptyLines: Boolean,
    val recordSeparator: String?,
    val commentMarker: String?,
    val skipHeaderRecord: Boolean,
    val header: String?,
    val ignoreHeaderCase: Boolean,
    val allowDuplicateHeaderNames: Boolean,
    val allowMissingColumnNames: Boolean,
    val headerComments: String?,
    val autoFlush: Boolean
)

@Serializable
data class BackupGradingV8(
    val id: Long,
    val onFirstCorrectAnswer: GradeChangeOnCorrectAnswer,
    val onFirstWrongAnswer: GradeChangeOnWrongAnswer,
    val askAgain: Boolean,
    val onRepeatedCorrectAnswer: GradeChangeOnCorrectAnswer,
    val onRepeatedWrongAnswer: GradeChangeOnWrongAnswer
)

@Serializable
data class BackupIntervalV8(
    val id: Long,
    val intervalSchemeId: Long,
    val grade: Int,
    @Serializable(with = DateTimeSpanSerializer::class)
    val value: DateTimeSpan
)

@Serializable
data class BackupIntervalSchemeV8(
    val id: Long
)

@Serializable
data class BackupKeyGestureMapV8(
    val keyGesture: KeyGesture,
    val keyGestureAction: KeyGestureAction
)

@Serializable
data class BackupKeyValueV8(
    val key: Long,
    val value: String?
);

@Serializable
data class BackupPronunciationV8(
    val id: Long,
    @Serializable(with = LocaleSerializer::class)
    val questionLanguage: Locale?,
    val questionAutoSpeaking: Boolean,
    @Serializable(with = LocaleSerializer::class)
    val answerLanguage: Locale?,
    val answerAutoSpeaking: Boolean,
    val speakTextInBrackets: Boolean
)

@Serializable
data class BackupPronunciationPlanV8(
    val id: Long,
    val pronunciationEvents: List<
            @Serializable(with = PronunciationEventSerializer::class) PronunciationEvent>
)

@Serializable
data class BackupSharedExercisePreferenceV8(
    val exercisePreferenceId: Long
)

@Serializable
data class BackupTipStateV8(
    val id: Long,
    val needToShow: Boolean,
    @Serializable(with = DateTimeSerializer::class)
    val lastShowedAt: DateTime?
)