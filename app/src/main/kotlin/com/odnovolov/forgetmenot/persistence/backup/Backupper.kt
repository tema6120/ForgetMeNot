package com.odnovolov.forgetmenot.persistence.backup

import com.odnovolov.forgetmenot.persistence.*
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class Backupper {
    fun export(outputStream: OutputStream): Boolean {
        return try {
            ZipOutputStream(BufferedOutputStream(outputStream)).use { out ->
                AppDiScope.get().app.getDatabasePath(DATABASE_NAME).parentFile!!.listFiles()!!
                    .forEach { file: File ->
                        out.putNextEntry(ZipEntry(file.name))
                        file.inputStream().use { origin ->
                            origin.copyTo(out, 1024)
                        }
                    }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun import(inputStream: InputStream): Boolean {
        return try {
            AppDiScope.get().sqlDriver.close()
            val databaseDir: File = AppDiScope.get().app.getDatabasePath(DATABASE_NAME).parentFile!!
            // todo: make copy of existing database to restore data in case of failure
            databaseDir.listFiles()!!.forEach(File::delete)
            ZipInputStream(inputStream).use { zipInputStream: ZipInputStream ->
                while (true) {
                    val zipEntry = zipInputStream.nextEntry ?: break
                    val newFile = File(databaseDir, zipEntry.name)
                    newFile.createNewFile()
                    newFile.outputStream().use { out ->
                        zipInputStream.copyTo(out, 1024)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}