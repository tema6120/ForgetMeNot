package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CsvParser
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.persistence.globalstate.*
import org.apache.commons.csv.CSVFormat

fun DeckDb.toDeck(
    cards: CopyableList<Card>,
    exercisePreference: ExercisePreference
) = Deck(
    id,
    name,
    createdAt,
    lastTestedAt,
    cards,
    exercisePreference,
    isPinned
)

fun Deck.toDeckDb(): DeckDb = DeckDb(
    id,
    name,
    createdAt,
    lastTestedAt,
    exercisePreference.id,
    isPinned
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

fun FileFormat.toFileFormatDb(): FileFormatDb {
    val csvFormat: CSVFormat = (parser as CsvParser).csvFormat
    return FileFormatDb(
        id,
        name,
        extension,
        csvFormat.delimiter.toString(),
        csvFormat.trailingDelimiter,
        csvFormat.quoteCharacter?.toString(),
        csvFormat.quoteMode,
        csvFormat.escapeCharacter?.toString(),
        csvFormat.nullString,
        csvFormat.ignoreSurroundingSpaces,
        csvFormat.trim,
        csvFormat.ignoreEmptyLines,
        csvFormat.recordSeparator,
        csvFormat.commentMarker?.toString(),
        csvFormat.skipHeaderRecord,
        csvFormat.header?.let(stringArrayAdapter::encode),
        csvFormat.ignoreHeaderCase,
        csvFormat.allowDuplicateHeaderNames,
        csvFormat.allowMissingColumnNames,
        csvFormat.headerComments?.let(stringArrayAdapter::encode),
        csvFormat.autoFlush
    )
}

fun FileFormatDb.toFileFormat(): FileFormat {
    val csvFormat = CSVFormat.newFormat(delimiter[0])
        .withTrailingDelimiter(trailingDelimiter)
        .withQuote(quoteCharacter?.get(0))
        .withEscape(escapeCharacter?.get(0))
        .withQuoteMode(quoteMode)
        .withNullString(nullString)
        .withIgnoreSurroundingSpaces(ignoreSurroundingSpaces)
        .withTrim(trim)
        .withIgnoreEmptyLines(ignoreEmptyLines)
        .withRecordSeparator(recordSeparator)
        .withCommentMarker(commentMarker?.get(0))
        .withSkipHeaderRecord(skipHeaderRecord)
        .let { format ->
            header?.let {
                val decodedHeader: Array<String?> = stringArrayAdapter.decode(header)
                format.withHeader(*decodedHeader)
            } ?: format
        }
        .withIgnoreHeaderCase(ignoreHeaderCase)
        .withAllowDuplicateHeaderNames(allowDuplicateHeaderNames)
        .withAllowMissingColumnNames(allowMissingColumnNames)
        .let { format ->
            headerComments?.let {
                val decodedHeaderComments: Array<String?> =
                    stringArrayAdapter.decode(headerComments)
                format.withHeaderComments(*decodedHeaderComments)
            } ?: format
        }
        .withAutoFlush(autoFlush)
    val parser = CsvParser(csvFormat)
    return FileFormat(
        id,
        name,
        extension,
        parser,
        isPredefined = false
    )
}

fun DeckListDb.toDeckList() = DeckList(
    id,
    name,
    color,
    deckIds
)

fun DeckList.toDeckListDb() = DeckListDb(
    id,
    name,
    color,
    deckIds
)