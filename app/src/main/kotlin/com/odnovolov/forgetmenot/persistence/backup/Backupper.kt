package com.odnovolov.forgetmenot.persistence.backup

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.*
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.squareup.sqldelight.db.SqlCursor
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

    fun export(outputStream: OutputStream): Boolean {
        return try {
            zipBackup(outputStream)
            true
        } catch (e: Exception) {
            false
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

    fun import(inputStream: InputStream): Boolean {
        return try {
            closeDatabase()
            moveCurrentDatabaseToCache()
            unzipBackup(inputStream)
            validateNewDatabase()
            true
        } catch (e: Exception) {
            rollback()
            false
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
        val sqlDriver = DatabaseInitializer.initSqlDriver(app)
        val cursor: SqlCursor =
            DatabaseInitializer.initSqlDriver(app).executeQuery(null, "PRAGMA schema_version", 0)
        cursor.next()
        val schemaVersion = cursor.getLong(0)!!
        if (schemaVersion <= 1L) throw Exception("Not valid database file")

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
}