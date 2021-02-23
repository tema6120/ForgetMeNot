package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentEvent.OpenedTheOutputStream
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentEvent.SelectedTheFileFormatForExport
import com.odnovolov.forgetmenot.presentation.screen.home.DsvFileFormatAdapter
import kotlinx.android.synthetic.main.fragment_deck_content.*
import kotlinx.android.synthetic.main.popup_export_as.view.*
import kotlinx.coroutines.launch

class DeckContentFragment : BaseFragment() {
    init {
        DeckContentDiScope.reopenIfClosed()
    }

    private var controller: DeckContentController? = null
    private lateinit var viewModel: DeckContentViewModel
    private var pendingEvent: OpenedTheOutputStream? = null
    private var isInflated = false
    lateinit var scrollListener: OnScrollListener
    private var exportedDeckName: String? = null
    private var extensionForExport: String? = null
    private var exportAsPopup: PopupWindow? = null
    private var dsvFileFormatAdapter: DsvFileFormatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            exportedDeckName = savedInstanceState.getString(STATE_EXPORTED_FILE_NAME)
            extensionForExport = savedInstanceState.getString(STATE_EXTENSION_FOR_EXPORT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (savedInstanceState == null) {
            inflater.inflateAsync(R.layout.fragment_deck_content, ::onViewInflated)
        } else {
            inflater.inflate(R.layout.fragment_deck_content, container, false)
        }
    }

    private fun onViewInflated() {
        if (viewCoroutineScope != null) {
            isInflated = true
            setupIfReady()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isInflated = true
        }
        viewCoroutineScope!!.launch {
            val diScope = DeckContentDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        val adapter = CardOverviewAdapter(controller!!)
        cardsRecycler.adapter = adapter
        viewModel.cards.observe(adapter::submitItems)
        controller!!.commands.observe(::executeCommand)
        pendingEvent?.let(controller!!::dispatch)
        pendingEvent = null
        cardsRecycler.addOnScrollListener(scrollListener)
    }

    private fun executeCommand(command: DeckContentController.Command) {
        when (command) {
            ShowFileFormatChooser -> {
                showExportAsPopup()
            }
            is CreateFile -> {
                exportedDeckName = command.deckName
                extensionForExport = command.extension
                openDocumentTree(OPEN_DOCUMENT_TREE_REQUEST_CODE)
            }
            ShowDeckIsExportedMessage -> {
                showToast(R.string.toast_deck_is_exported)
            }
            ShowExportErrorMessage -> {
                showToast(R.string.toast_error_while_exporting_deck)
            }
        }
    }

    private fun requireExportAsPopup(): PopupWindow {
        if (exportAsPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_export_as, null).apply {
                fmnFormatRadioButton.setOnClickListener {
                    controller?.dispatch(SelectedTheFileFormatForExport(FileFormat.FMN_FORMAT))
                    exportAsPopup?.dismiss()
                }
                dsvFormatListExpander.setOnClickListener {
                    dsvFormatRecycler.isVisible = !dsvFormatRecycler.isVisible
                    val expandIconRes = if (dsvFormatRecycler.isVisible)
                        R.drawable.ic_round_expand_less_32 else
                        R.drawable.ic_round_expand_more_32
                    dsvFormatListExpander.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, expandIconRes, 0
                    )
                }
            }
            dsvFileFormatAdapter = DsvFileFormatAdapter(
                onItemClicked = { fileFormat: FileFormat ->
                    controller?.dispatch(SelectedTheFileFormatForExport(fileFormat))
                    exportAsPopup?.dismiss()
                }
            )
            content.dsvFormatRecycler.adapter = dsvFileFormatAdapter
            exportAsPopup = LightPopupWindow(content)
            subscribeExportAsPopupToViewModel()
        }
        return exportAsPopup!!
    }

    private fun subscribeExportAsPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = DeckContentDiScope.getAsync() ?: return@launch
            diScope.viewModel.dsvFileFormats.observe { dsvFileFormats: List<FileFormat> ->
                dsvFileFormatAdapter!!.items = dsvFileFormats
            }
        }
    }

    private fun showExportAsPopup() {
        requireExportAsPopup().show(
            anchor = cardsRecycler,
            gravity = GravityCompat.START or Gravity.TOP
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != OPEN_DOCUMENT_TREE_REQUEST_CODE
            || resultCode != Activity.RESULT_OK
            || intent == null
        ) {
            return
        }
        val uri: Uri = intent.data ?: return
        val deckName: String = exportedDeckName ?: return
        val mimeType: String = when (extensionForExport) {
            FileFormat.EXTENSION_TXT -> "text/plain"
            FileFormat.EXTENSION_CSV -> "text/comma-separated-values"
            FileFormat.EXTENSION_TSV -> "text/tab-separated-values"
            else -> return
        }
        val pickedDir: DocumentFile =
            DocumentFile.fromTreeUri(requireContext(), uri) ?: kotlin.run {
                showToast(R.string.toast_error_while_exporting_deck)
                return
            }
        val newFile: DocumentFile = pickedDir.createFile(mimeType, deckName) ?: kotlin.run {
            showToast(R.string.toast_error_while_exporting_deck)
            return
        }
        val outputStream = requireContext().contentResolver?.openOutputStream(newFile.uri)
        if (outputStream != null) {
            val event = OpenedTheOutputStream(outputStream)
            if (controller == null) {
                pendingEvent = event
            } else {
                controller!!.dispatch(event)
            }
        } else {
            showToast(R.string.toast_error_while_exporting_deck)
        }
        exportedDeckName = null
        extensionForExport = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val needToShowExportAsPopup = getBoolean(STATE_EXPORT_AS_POPUP, false)
            if (needToShowExportAsPopup) {
                showExportAsPopup()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        exportedDeckName?.let { outState.putString(STATE_EXPORTED_FILE_NAME, it) }
        extensionForExport?.let { outState.putString(STATE_EXTENSION_FOR_EXPORT, it) }
        val isExportAsPopupShowing = exportAsPopup?.isShowing ?: false
        outState.putBoolean(STATE_EXPORT_AS_POPUP, isExportAsPopupShowing)
    }

    override fun onDestroyView() {
        cardsRecycler.removeOnScrollListener(scrollListener)
        super.onDestroyView()
        isInflated = false
        exportAsPopup?.dismiss()
        exportAsPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckContentDiScope.close()
        }
    }

    companion object {
        const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 80
        const val STATE_EXPORTED_FILE_NAME = "STATE_EXPORTED_FILE_NAME"
        const val STATE_EXTENSION_FOR_EXPORT = "STATE_EXTENSION_FOR_EXPORT"
        const val STATE_EXPORT_AS_POPUP = "STATE_EXPORT_AS_POPUP"
    }
}