package com.odnovolov.forgetmenot.presentation.screen.backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import com.jakewharton.processphoenix.ProcessPhoenix
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.openDocumentTree
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ReadyToExportBackup
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ReadyToImportBackup
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import kotlinx.android.synthetic.main.fragment_backup.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class BackupFragment : BaseFragment() {
    init {
        BackupDiScope.reopenIfClosed()
    }

    private var controller: BackupController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_backup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = BackupDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun executeCommand(command: Command) {
        when (command) {
            is ShowImportResultAndRestartApp -> {
                showToast(
                    if (command.success)
                        "Backup has been imported" else
                        "Cannot import the backup"
                )
                ProcessPhoenix.triggerRebirth(requireActivity())
            }
            is ShowExportResult -> {
                showToast(
                    if (command.success)
                        "Backup has been exported" else
                        "Cannot create a backup"
                )
            }
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        importButton.setOnClickListener {
            openBackupFileChooser(GET_CONTENT_REQUEST_CODE)
        }
        exportButton.setOnClickListener {
            openDocumentTree(OPEN_DOCUMENT_TREE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode != Activity.RESULT_OK || intent == null) return

        when (requestCode) {
            GET_CONTENT_REQUEST_CODE -> {
                val uri: Uri = intent.data
                    ?: run {
                        showToast(R.string.toast_couldnt_get_file)
                        return
                    }
                val inputStream: InputStream =
                    requireContext().contentResolver.openInputStream(uri)
                        ?: run {
                            showToast(R.string.toast_couldnt_get_file)
                            return
                        }

                controller?.dispatch(ReadyToImportBackup(inputStream))
            }
            OPEN_DOCUMENT_TREE_REQUEST_CODE -> {
                val pickedDir: DocumentFile = getDirectory(intent)
                    ?: run {
                        showToast(R.string.toast_couldnt_get_destination)
                        return
                    }

                val newFile: DocumentFile = pickedDir.createFile("*/*", backupFileName())
                    ?: run {
                        showToast(R.string.toast_couldnt_get_destination)
                        return
                    }

                val outputStream: OutputStream =
                    requireContext().contentResolver.openOutputStream(newFile.uri)
                        ?: run {
                            showToast(R.string.toast_couldnt_get_destination)
                            return
                        }

                controller?.dispatch(ReadyToExportBackup(outputStream))
            }
        }
    }

    private fun getDirectory(intent: Intent?): DocumentFile? {
        val uri = intent?.data ?: return null
        return DocumentFile.fromTreeUri(requireContext(), uri)
    }

    private fun backupFileName(): String {
        val dateFormat = DateFormat("yyyy_MM_dd_HH_mm_ss")
        val timeStamp: String = DateTime.nowLocal().toString(dateFormat)
        return "ForgetMeNot_backup_$timeStamp.zip"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            BackupDiScope.close()
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 17
        const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 34
    }
}