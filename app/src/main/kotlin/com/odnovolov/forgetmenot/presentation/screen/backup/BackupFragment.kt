package com.odnovolov.forgetmenot.presentation.screen.backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.processphoenix.ProcessPhoenix
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.openBackupFileChooser
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command.ShowImportResultAndRestartApp
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ExportButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ReadyToImportBackup
import kotlinx.android.synthetic.main.fragment_backup.*
import kotlinx.coroutines.launch
import java.io.InputStream

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
            controller?.dispatch(ExportButtonClicked)
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            BackupDiScope.close()
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 17
    }
}