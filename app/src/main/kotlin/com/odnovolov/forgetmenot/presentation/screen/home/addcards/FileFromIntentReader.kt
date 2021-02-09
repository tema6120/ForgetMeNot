package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileNotFoundException

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
                results.add(result)
            }
        } else {
            val uri: Uri? = intent.data
            val result = readUri(uri)
            results.add(result)
        }
        return results
    }

    private fun readUri(uri: Uri?): Result {
        if (uri == null) return Result.Failure()
        val fileName: String? = readFileName(uri)
        val fileContent: ByteArray? = readInputStream(uri)
        return when (fileContent) {
            null -> Result.Failure(fileName)
            else -> Result.Success(fileName, fileContent)
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