package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FileFromIntentReader(
    private val contentResolver: ContentResolver
) {
    fun read(intent: Intent): List<Result> {
        val results: MutableList<Result> = ArrayList()
        val clipData = intent.clipData
        if (clipData != null) {
            repeat(clipData.itemCount) { i: Int ->
                val uri: Uri? = clipData.getItemAt(i).uri
                val result = readUri(uri)
                results.addAll(result)
            }
        } else {
            val uri: Uri? = intent.data
            val result = readUri(uri)
            results.addAll(result)
        }
        return results
    }

    private fun readUri(uri: Uri?): List<Result> {
        if (uri == null) return listOf(Result.Failure())
        val fileName: String? = readFileName(uri)
        if (fileName?.endsWith(".zip") == true) {
            return unzip(fileName, uri)
        }
        val fileContent: ByteArray? = readInputStream(uri)
        return when (fileContent) {
            null -> listOf(Result.Failure(fileName))
            else -> listOf(Result.Success(fileName, fileContent))
        }
    }

    private fun readFileName(uri: Uri): String? {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }
            val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return try {
                cursor.getString(nameIndex)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun unzip(fileName: String?, uri: Uri): List<Result> {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return listOf(Result.Failure(fileName))
            } else {
                val results = ArrayList<Result>()
                ZipInputStream(inputStream).use { zipInputStream: ZipInputStream ->
                    var zipEntry = try {
                        zipInputStream.nextEntry
                    } catch (e: Exception) {
                        return listOf(Result.Failure(fileName))
                    }
                    while (zipEntry != null) {
                        if (!zipEntry.isDirectory) {
                            val result = unzip(zipInputStream, zipEntry)
                            results.add(result)
                        }
                        zipEntry = try {
                            zipInputStream.nextEntry
                        } catch (e: Exception) {
                            return listOf(Result.Failure(fileName))
                        }
                    }
                }
                results
            }
        } catch (e: FileNotFoundException) {
            return listOf(Result.Failure(fileName))
        }
    }

    private fun unzip(zipInputStream: ZipInputStream, zipEntry: ZipEntry): Result {
        val fileName: String = zipEntry.name.substringAfterLast("/")
        return try {
            val fileContent: ByteArray = zipInputStream.readBytes()
            Result.Success(fileName, fileContent)
        } catch (e: Exception) {
            Result.Failure(fileName)
        }
    }

    private fun readInputStream(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: FileNotFoundException) {
            return null
        }
    }

    sealed class Result {
        class Success(val fileName: String?, val fileContent: ByteArray) : Result()
        class Failure(val fileName: String? = null) : Result()
    }
}