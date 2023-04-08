package com.odnovolov.forgetmenot.presentation.screen.backup.import

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.jakewharton.processphoenix.ProcessPhoenix
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result.Failure
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result.Success
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.openBackupFileChooser
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportDialogState.Stage
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportEvent.ReadyToImportBackup
import kotlinx.android.synthetic.main.dialog_backup_import.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class BackupImportDialog : BaseDialogFragment() {
    init {
        BackupImportDiScope.reopenIfClosed()
    }

    private var controller: BackupImportController? = null
    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(requireContext(), R.layout.dialog_backup_import, null)
        setupView()
        viewCoroutineScope!!.launch(Dispatchers.Main) {
            val diScope = BackupImportDiScope.getAsync() ?: return@launch
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
            selectBackupFileButton.setOnClickListener {
                openBackupFileChooser(GET_CONTENT_REQUEST_CODE)
            }
            restartButton.setOnClickListener {
                ProcessPhoenix.triggerRebirth(requireActivity())
            }
        }
    }

    private fun observeViewModel(viewModel: BackupImportViewModel) {
        viewModel.stage.observe { stage: Stage ->
            with(contentView) {
                waitingForBackupFileGroup.isVisible = stage == Stage.WaitingForBackupFile
                dialog?.setCancelable(stage == Stage.WaitingForBackupFile)
                dialog?.setCanceledOnTouchOutside(stage == Stage.WaitingForBackupFile)
                importingGroup.isVisible = stage == Stage.InProgress
                finishedGroup.isVisible = stage is Stage.Finished
                if (stage is Stage.Finished) {
                    when (stage.result) {
                        Success -> {
                            successfulBackupImportTextView.isVisible = true
                            failedToImportBackupTextView.isVisible = false
                        }
                        is Failure -> {
                            val firstLineOfStackTrace: String =
                                stage.result.exception.stackTraceToString().lineSequence().first()
                            failedToImportBackupTextView.text = getString(
                                R.string.error_while_importing_backup,
                                firstLineOfStackTrace
                            )
                            successfulBackupImportTextView.isVisible = false
                            failedToImportBackupTextView.isVisible = true
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode != GET_CONTENT_REQUEST_CODE
            || resultCode != Activity.RESULT_OK
            || intent == null
        ) {
            return
        }

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

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            BackupImportDiScope.close()
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 17
    }
}