package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.brackeys.ui.editorkit.listener.OnUndoRedoChangedListener
import com.brackeys.ui.editorkit.span.ErrorSpan
import com.brackeys.ui.editorkit.widget.TextProcessor
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.CharsetAdapter
import com.odnovolov.forgetmenot.presentation.screen.fileimport.CharsetItem
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.ImportedTextEditorEvent.EncodingIsChanged
import com.odnovolov.forgetmenot.presentation.screen.fileimport.editor.editorColorScheme
import kotlinx.android.synthetic.main.fragment_cards_file.*
import kotlinx.android.synthetic.main.fragment_imported_text_editor.*
import kotlinx.android.synthetic.main.popup_charsets.view.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class ImportedTextEditorFragment : BaseFragment() {
    companion object {
        const val ARG_ID = "ARG_ID"
        const val MAX_TEXT_LENGTH_TO_EDIT = 20_000
        const val MAX_ERROR_LINES_TO_SHOW = 50

        fun create(id: Long) = ImportedTextEditorFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private var controller: ImportedTextEditorController? = null
    private lateinit var viewModel: ImportedTextEditorViewModel
    private var charsetPopup: PopupWindow? = null
    private var charsetAdapter: CharsetAdapter? = null
    private var errorBlocks: List<ErrorBlock> = emptyList()
    private var lastShownErrorBlock: ErrorBlock? = null
    private val lastShownErrorLine: Int
        get() = lastShownErrorBlock?.lines?.get(0) ?: -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_imported_text_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            controller = diScope.importedTextEditorController
            val id = requireArguments().getLong(ARG_ID)
            viewModel = diScope.importedTextEditorViewModel(id)
            editor.language = diScope.syntaxHighlighting
            observeViewModel(isRecreated = savedInstanceState != null)
        }
    }

    private fun setupView() {
        editor.colorScheme = editorColorScheme
        charsetButton.setOnClickListener {
            showCharsetPopup()
        }
        errorButton.setOnClickListener {
            showNextError()
        }
        undoButton.isEnabled = false
        redoButton.isEnabled = false
        undoButton.setOnClickListener {
            if (editor.canUndo()) editor.undo()
        }
        redoButton.setOnClickListener {
            if (editor.canRedo()) editor.redo()
        }
        editor.onUndoRedoChangedListener = OnUndoRedoChangedListener {
            undoButton.isEnabled = editor.canUndo()
            redoButton.isEnabled = editor.canRedo()
        }
        undoButton.setTooltipTextFromContentDescription()
        redoButton.setTooltipTextFromContentDescription()
    }

    private fun showNextError() {
        if (errorBlocks.isEmpty()) return
        determineNextErrorBlock()
        if (editor.isVisible) {
            editorScrollView.smoothScrollTo(0, determineErrorLineVerticalPosition(), 500)
        }
        errorButton.text = composeErrorMessage()
    }

    private fun determineNextErrorBlock() {
        lastShownErrorBlock = errorBlocks.find { errorBlock: ErrorBlock ->
            errorBlock.lines[0] > lastShownErrorLine
        } ?: errorBlocks.first()
    }

    private fun determineErrorLineVerticalPosition(): Int {
        val errorLineStartIndex: Int = editor.getIndexForStartOfLine(lastShownErrorLine)
        val lineInTermsOfLayout: Int = editor.layout.getLineForOffset(errorLineStartIndex)
        return editor.layout.getLineTop(lineInTermsOfLayout)
    }

    private fun composeErrorMessage(): String {
        val errorOrdinal = errorBlocks.indexOf(lastShownErrorBlock) + 1
        val linesString =
            if (lastShownErrorBlock!!.lines.size > 1) {
                val firstLineNumber = lastShownErrorBlock!!.lines.first() + 1
                val lastLineNumber = lastShownErrorBlock!!.lines.last() + 1
                "lines $firstLineNumber - $lastLineNumber"
            } else {
                val lineNumber = lastShownErrorBlock!!.lines.first() + 1
                "line $lineNumber"
            }
        return "Error $errorOrdinal/${errorBlocks.size}: ${lastShownErrorBlock!!.errorMessage} ($linesString)"
    }

    private fun observeViewModel(isRecreated: Boolean) {
        with(viewModel) {
            updateTextCommand.observe { text: String ->
                if (text.length <= MAX_TEXT_LENGTH_TO_EDIT) {
                    editor.setTextContent(text)
                    editor.isVisible = true
                    editOffTextView.isVisible = false
                } else {
                    editor.isVisible = false
                    editOffTextView.isVisible = true
                }
            }
            errors.observe { errors: List<ErrorBlock> ->
                if (this@ImportedTextEditorFragment.errorBlocks.isNotEmpty()) {
                    editor.clearErrorLines()
                }
                this@ImportedTextEditorFragment.errorBlocks = errors
                applyErrors(errors)
                if (errors.isEmpty()) {
                    editor.clearErrorLines()
                }
                val numberOfErrors = errors.size
                errorButton.isVisible = numberOfErrors > 0
                errorLineView.isVisible = numberOfErrors > 0
                if (numberOfErrors > 0) {
                    errorButton.text = resources.getQuantityString(
                        R.plurals.source_text_error_button,
                        numberOfErrors,
                        numberOfErrors
                    )
                }
            }
            if (!isRecreated) {
                viewCoroutineScope!!.launch {
                    val errorLinesAtStart = errors.first()
                    this@ImportedTextEditorFragment.errorBlocks = errorLinesAtStart
                    if (errorLinesAtStart.isNotEmpty()) {
                        (parentFragment as CardsFileFragment).viewPager?.run {
                            post { setCurrentItem(1, false) }
                        }
                        editor.doOnLayout {
                            editor.post { showNextError() }
                        }
                    }
                }
            }
            currentCharset.observe { charset: Charset ->
                charsetButton.text = charset.name()
            }
        }
    }

    private fun applyErrors(errors: List<ErrorBlock>) {
        var numberOfErrorLines = 0
        errors.forEach { errorBlock: ErrorBlock ->
            errorBlock.lines.forEach { errorLine: Int ->
                editor.setErrorLine(errorLine + 1)
                numberOfErrorLines++
                if (numberOfErrorLines == MAX_ERROR_LINES_TO_SHOW) return
            }
        }
    }

    private fun TextProcessor.clearErrorLines() {
        text.getSpans(0, text.length, ErrorSpan::class.java).forEach(text::removeSpan)
    }

    private fun showCharsetPopup() {
        requireCharsetPopup().show(anchor = charsetButton, gravity = Gravity.BOTTOM)
        charsetButton.requestLayout()
    }

    private fun requireCharsetPopup(): PopupWindow {
        if (charsetPopup == null) {
            val content: View = View.inflate(requireContext(), R.layout.popup_charsets, null)
            val onItemClicked: (Charset) -> Unit = { charset: Charset ->
                charsetPopup?.dismiss()
                controller?.dispatch(EncodingIsChanged(charset))
            }
            charsetAdapter = CharsetAdapter(onItemClicked)
            content.charsetRecycler.adapter = charsetAdapter
            charsetPopup = DarkPopupWindow(content)
            subscribeCharsetPopupToViewModel()
        }
        return charsetPopup!!
    }

    private fun subscribeCharsetPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            val id = requireArguments().getLong(ARG_ID)
            val viewModel = diScope.importedTextEditorViewModel(id)
            viewModel.availableCharsets.observe { availableCharsets: List<CharsetItem> ->
                charsetAdapter!!.items = availableCharsets
            }
        }
    }

    override fun onPause() {
        super.onPause()
        editor.hideSoftInput()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        charsetPopup?.dismiss()
        charsetPopup = null
    }
}