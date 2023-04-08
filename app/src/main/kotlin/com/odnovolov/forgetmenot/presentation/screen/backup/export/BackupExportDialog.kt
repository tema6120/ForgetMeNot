package com.odnovolov.forgetmenot.presentation.screen.backup.export

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.openDocumentTree
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDialogState.ExportResult.Failure
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDialogState.ExportResult.Success
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDialogState.Stage
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportEvent.ReadyToExportBackup
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import kotlinx.android.synthetic.main.dialog_backup_export.view.*
import kotlinx.coroutines.launch
import java.io.OutputStream

class BackupExportDialog : BaseDialogFragment() {
    init {
        BackupExportDiScope.reopenIfClosed()
    }

    private var controller: BackupExportController? = null
    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(requireContext(), R.layout.dialog_backup_export, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = BackupExportDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
        return createDialog(contentView)
    }

    private fun setupView() {
        with(contentView) {
            closeButton.setOnClickListener {
                dismiss()
            }
            selectDestinationButton.setOnClickListener {
                openDocumentTree(OPEN_DOCUMENT_TREE_REQUEST_CODE)
            }
        }
    }

    private fun observeViewModel(viewModel: BackupExportViewModel) {
        viewModel.stage.observe { stage: Stage ->
            with(contentView) {
                waitingForDestinationGroup.isVisible = stage == Stage.WaitingForDestination
                exportingGroup.isVisible = stage == Stage.InProgress
                finishedGroup.isVisible = stage is Stage.Finished
                if (stage is Stage.Finished) {
                    when (stage.result) {
                        is Success -> {
                            successfulBackupExportTextView.text = getString(
                                R.string.backup_has_been_exported,
                                stage.result.fileName
                            )
                            successfulBackupExportTextView.isVisible = true
                            failedToExportBackupTextView.isVisible = false
                        }
                        is Failure -> {
                            val firstLineOfStackTrace: String =
                                stage.result.exception.stackTraceToString().lineSequence().first()
                            failedToExportBackupTextView.text = getString(
                                R.string.error_while_exporting_backup,
                                firstLineOfStackTrace
                            )
                            successfulBackupExportTextView.isVisible = false
                            failedToExportBackupTextView.isVisible = true
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode != OPEN_DOCUMENT_TREE_REQUEST_CODE
            || resultCode != Activity.RESULT_OK
            || intent == null
        ) {
            return
        }

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

        val fileName: String = newFile.name ?: "";
        controller?.dispatch(ReadyToExportBackup(outputStream, fileName))
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
            BackupExportDiScope.close()
        }
    }

    companion object {
        const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 34
    }
}