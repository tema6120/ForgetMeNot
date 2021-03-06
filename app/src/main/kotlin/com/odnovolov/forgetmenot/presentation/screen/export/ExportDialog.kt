package com.odnovolov.forgetmenot.presentation.screen.export

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.openDocumentTree
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.export.ExportController.Command.CreateFiles
import com.odnovolov.forgetmenot.presentation.screen.export.ExportEvent.GotFilesCreationResult
import com.odnovolov.forgetmenot.presentation.screen.export.ExportEvent.GotFilesCreationResult.FileCreationResult
import com.odnovolov.forgetmenot.presentation.screen.export.ExportEvent.SelectedTheFileFormat
import kotlinx.android.synthetic.main.dialog_export.*
import kotlinx.android.synthetic.main.dialog_export.view.*
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.OutputStream

class ExportDialog : BaseDialogFragment() {
    init {
        ExportDiScope.reopenIfClosed()
    }

    private var controller: ExportController? = null
    private lateinit var viewModel: ExportViewModel
    private lateinit var rootView: View
    private var dsvFileFormatAdapter: DsvFileFormatAdapter? = null
    private var deckNames: List<String>? = null
    private var extension: String? = null
    private var pendingEvent: GotFilesCreationResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            deckNames = savedInstanceState.getStringArray(STATE_DECK_NAMES)?.toList()
            extension = savedInstanceState.getString(STATE_EXTENSION)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_export, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ExportDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
        return createDialog(rootView)
    }

    private fun setupView() {
        rootView.closeButton.setOnClickListener {
            dismiss()
        }
        rootView.fmnFormatRadioButton.setOnClickListener {
            controller?.dispatch(SelectedTheFileFormat(FileFormat.FMN_FORMAT))
        }
        rootView.dsvFormatListExpander.setOnClickListener {
            rootView.dsvFormatRecycler.isVisible = !rootView.dsvFormatRecycler.isVisible
            val expandIconRes = if (rootView.dsvFormatRecycler.isVisible)
                R.drawable.ic_round_expand_less_32 else
                R.drawable.ic_round_expand_more_32
            rootView.dsvFormatListExpander.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, expandIconRes, 0
            )
        }
        dsvFileFormatAdapter = DsvFileFormatAdapter(
            onItemClicked = { fileFormat: FileFormat ->
                controller?.dispatch(SelectedTheFileFormat(fileFormat))
            }
        )
        rootView.dsvFormatRecycler.adapter = dsvFileFormatAdapter
        rootView.selectDestinationButton.setOnClickListener {
            openDocumentTree(OPEN_DOCUMENT_TREE_REQUEST_CODE)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            stage.observe { stage: Stage ->
                with(rootView) {
                    waitingForFileFormatGroup.isVisible = stage == Stage.WaitingForFileFormat
                    waitingForDestinationGroup.isVisible = stage == Stage.WaitingForDestination
                    exportingGroup.isVisible = stage == Stage.Exporting
                    finishedGroup.isVisible = stage is Stage.Finished
                    if (stage != Stage.WaitingForFileFormat) {
                        dsvFormatRecycler.isVisible = false
                    }
                    if (stage is Stage.Finished) {
                        val exportedDeckNames: List<String> = stage.exportedDeckNames
                        if (exportedDeckNames.isNotEmpty()) {
                            exportedDeckNamesTextView.text = resources.getQuantityString(
                                R.plurals.export_result_successful,
                                exportedDeckNames.size,
                                exportedDeckNames.size
                            )
                        } else {
                            exportedDeckNamesTextView.isVisible = false
                        }
                        val failedDeckNames: List<String> = stage.failedDeckNames
                        if (failedDeckNames.isNotEmpty()) {
                            val listOfFailedDeckNames: String = failedDeckNames
                                .joinToString(separator = ",\n") { deckName -> "\t'$deckName'" }
                            failedDeckNamesTextView.text = getString(
                                R.string.export_result_error,
                                listOfFailedDeckNames
                            )
                        } else {
                            failedDeckNamesTextView.isVisible = false
                        }
                    }
                }
            }
            dsvFileFormats.observe { dsvFileFormats: List<FileFormat> ->
                dsvFileFormatAdapter!!.items = dsvFileFormats
            }
        }
    }

    private fun executeCommand(command: ExportController.Command) {
        when (command) {
            is CreateFiles -> {
                deckNames = command.deckNames
                extension = command.extension
                openDocumentTree(OPEN_DOCUMENT_TREE_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val pickedDir: DocumentFile? = getDirectory(requestCode, resultCode, intent)
        if (pickedDir == null) {
            showToast(R.string.toast_couldnt_get_destination)
            return
        }
        val deckNames: List<String> = deckNames ?: return
        val mimeType: String = when (extension) {
            FileFormat.EXTENSION_TXT -> "text/plain"
            FileFormat.EXTENSION_CSV -> "text/comma-separated-values"
            FileFormat.EXTENSION_TSV -> "text/tab-separated-values"
            else -> return
        }
        val filesCreationResult: List<FileCreationResult> = deckNames.map { deckName: String ->
            try {
                val newFile: DocumentFile = pickedDir.createFile(mimeType, deckName)
                    ?: return@map FileCreationResult(deckName, null)
                val outputStream: OutputStream? =
                    requireContext().contentResolver?.openOutputStream(newFile.uri)
                FileCreationResult(deckName, outputStream)
            } catch (e: FileNotFoundException) {
                FileCreationResult(deckName, null)
            }
        }
        val event = GotFilesCreationResult(filesCreationResult)
        if (controller == null) {
            pendingEvent = event
        } else {
            controller!!.dispatch(event)
        }
        this.deckNames = null
        extension = null
    }

    private fun getDirectory(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ): DocumentFile? {
        if (requestCode != OPEN_DOCUMENT_TREE_REQUEST_CODE
            || resultCode != Activity.RESULT_OK
            || intent == null
        ) {
            return null
        }
        val uri = intent.data ?: return null
        return DocumentFile.fromTreeUri(requireContext(), uri)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        deckNames?.let { outState.putStringArray(STATE_DECK_NAMES, it.toTypedArray()) }
        extension?.let { outState.putString(STATE_EXTENSION, it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            ExportDiScope.close()
        }
    }

    companion object {
        const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 40
        const val STATE_DECK_NAMES = "STATE_DECK_NAMES"
        const val STATE_EXTENSION = "STATE_EXTENSION"
    }
}