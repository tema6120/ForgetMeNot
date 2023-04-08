package com.odnovolov.forgetmenot.persistence.backup

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.*
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class Backupper {
    private val app = AppDiScope.get().app

    private val databaseDir: File by lazy {
        app.getDatabasePath(DATABASE_NAME).parentFile!!
    }

    private val databaseDirInCacheDir: File by lazy {
        File(app.cacheDir, databaseDir.name)
    }

    fun export(outputStream: OutputStream): Result {
        return try {
            zipBackup(outputStream)
            Result.Success
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Failure(e)
        }
    }

    private fun zipBackup(outputStream: OutputStream) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOutputStream: ZipOutputStream ->
            databaseDir.listFiles()!!.forEach { file: File ->
                zipOutputStream.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { fileInputStream: FileInputStream ->
                    fileInputStream.copyTo(zipOutputStream, 1024)
                }
            }
        }
    }

    fun import(inputStream: InputStream): Result {
        return try {
            closeDatabase()
            moveCurrentDatabaseToCache()
            unzipBackup(inputStream)
            validateNewDatabase()
            Result.Success
        } catch (e: Exception) {
            e.printStackTrace()
            rollback()
            Result.Failure(e)
        } finally {
            clearCache()
        }
    }

    private fun closeDatabase() {
        AppDiScope.get().sqlDriver.close()
    }

    private fun moveCurrentDatabaseToCache() {
        clearCache()
        databaseDirInCacheDir.mkdir()
        databaseDir.copyRecursively(databaseDirInCacheDir, overwrite = true)
        databaseDir.listFiles()!!.forEach(File::delete)
    }

    private fun unzipBackup(inputStream: InputStream) {
        ZipInputStream(inputStream).use { zipInputStream: ZipInputStream ->
            while (true) {
                val zipEntry = zipInputStream.nextEntry ?: break
                val newFile = File(databaseDir, zipEntry.name)
                newFile.createNewFile()
                newFile.outputStream().use { fileOutputStream: FileOutputStream ->
                    zipInputStream.copyTo(fileOutputStream, 1024)
                }
            }
        }
    }

    private fun validateNewDatabase() {
        val sqlDriver: SqlDriver = DatabaseInitializer.initSqlDriver(app)
        val cursor: SqlCursor = sqlDriver.executeQuery(null, "PRAGMA schema_version", 0)
        cursor.next()
        val schemaVersion: Long = cursor.getLong(0)!!
        if (schemaVersion <= 1L) throw Exception("Not a valid database file")

        val database: Database = DatabaseInitializer.initDatabase(sqlDriver)
        database.keyValueQueries
            .selectValue(DbKeys.ARE_INITIAL_DECKS_ADDED)
            .executeAsOne() // it will throw NullPointerException if there is no value
    }

    private fun rollback() {
        databaseDirInCacheDir.copyRecursively(databaseDir, overwrite = true)
    }

    private fun clearCache() {
        databaseDirInCacheDir.deleteRecursively()
    }

    sealed class Result {
        object Success : Result()
        class Failure(val exception: Exception) : Result()
    }
}