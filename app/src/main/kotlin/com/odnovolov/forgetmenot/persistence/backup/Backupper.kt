package com.odnovolov.forgetmenot.persistence.backup

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.*
import com.odnovolov.forgetmenot.persistence.backup.v8.*
import com.odnovolov.forgetmenot.persistence.globalstate.*
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Backupper(
    private val json: Json,
    private val database: Database
) {
    fun export(outputStream: OutputStream): Boolean {
        return try {
            val backup: BackupV8 = createBackupFromDatabase()
            writeBackup(backup, outputStream)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createBackupFromDatabase(): BackupV8 {
        val cards: List<BackupCardV8> =
            database.cardQueries.selectAll().executeAsList()
                .map(CardDb::toBackupCardV8)
        val decks: List<BackupDeckV8> =
            database.deckQueries.selectAll().executeAsList()
                .map(DeckDb::toBackupDeckV8)
        val deckLists: List<BackupDeckListV8> =
            database.deckListQueries.selectAll().executeAsList()
                .map(DeckListDb::toBackupDeckListV8)
        val deckReviewPreferences: List<BackupDeckReviewPreferenceV8> =
            database.deckReviewPreferenceQueries.selectAll().executeAsList()
                .map(DeckReviewPreferenceDb::toBackupDeckReviewPreferenceV8)
        val exercisePreferences: List<BackupExercisePreferenceV8> =
            database.exercisePreferenceQueries.selectAll().executeAsList()
                .map(ExercisePreferenceDb::toBackupExercisePreferenceV8)
        val fileFormats: List<BackupFileFormatV8> =
            database.fileFormatQueries.selectAll().executeAsList()
                .map(FileFormatDb::toBackupFileFormatV8)
        val gradings: List<BackupGradingV8> =
            database.gradingQueries.selectAll().executeAsList()
                .map(GradingDb::toBackupGradingV8)
        val intervals: List<BackupIntervalV8> =
            database.intervalQueries.selectAll().executeAsList()
                .map(IntervalDb::toBackupIntervalV8)
        val intervalSchemes: List<BackupIntervalSchemeV8> =
            database.intervalSchemeQueries.selectAll().executeAsList()
                .map(::BackupIntervalSchemeV8)
        val keyGestures: List<BackupKeyGestureMapV8> =
            database.keyGestureMapQueries.selectAll().executeAsList()
                .map(KeyGestureMapDb::toBackupKeyGestureMapV8)
        val keyValues: List<BackupKeyValueV8> =
            database.keyValueQueries.selectAll().executeAsList()
                .map(KeyValue::toBackupKeyValueV8)
        val pronunciations: List<BackupPronunciationV8> =
            database.pronunciationQueries.selectAll().executeAsList()
                .map(PronunciationDb::toBackupPronunciationV8)
        val pronunciationPlans: List<BackupPronunciationPlanV8> =
            database.pronunciationPlanQueries.selectAll().executeAsList()
                .map(PronunciationPlanDb::toBackupPronunciationPlanV8)
        val sharedExercisePreferences: List<BackupSharedExercisePreferenceV8> =
            database.sharedExercisePreferenceQueries.selectAll().executeAsList()
                .map(::BackupSharedExercisePreferenceV8)
        val tipStates: List<BackupTipStateV8> =
            database.tipStateQueries.selectAll().executeAsList()
                .map(TipStateDb::toBackupTipStateV8)
        return BackupV8(
            cards,
            decks,
            deckLists,
            deckReviewPreferences,
            exercisePreferences,
            fileFormats,
            gradings,
            intervals,
            intervalSchemes,
            keyGestures,
            keyValues,
            pronunciations,
            pronunciationPlans,
            sharedExercisePreferences,
            tipStates
        )
    }

    private fun writeBackup(
        backup: BackupV8,
        outputStream: OutputStream
    ) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { out ->
            out.putNextEntry(ZipEntry(VERSION_FILE_NAME))
            LATEST_BACKUP_VERSION.byteInputStream().use { origin ->
                origin.copyTo(out, 1024)
            }

            out.putNextEntry(ZipEntry(DATA_FILE_NAME))
            val serializer = BackupV8.serializer()
            val jsonString: String = json.encodeToString(serializer, backup)
            jsonString.byteInputStream().buffered().use { origin ->
                origin.copyTo(out, 1024)
            }
        }
    }

    companion object {
        private const val LATEST_BACKUP_VERSION = "8"
        private const val VERSION_FILE_NAME = "version.txt"
        private const val DATA_FILE_NAME = "data.json"
    }
}