package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.IgnoreSurroundingSpacesButton
import kotlinx.android.synthetic.main.fragment_dsv_format.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.coroutines.launch
import org.apache.commons.csv.QuoteMode

class DsvFormatFragment : BaseFragment() {
    init {
        DsvFormatDiScope.reopenIfClosed()
    }

    private var controller: DsvFormatController? = null
    private lateinit var viewModel: DsvFormatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dsv_format, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DsvFormatDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        tipTextView.setTextWithClickableAnnotations(
            stringId = R.string.tip_dsv_format,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "csv_library" -> openUrl(APACHE_COMMONS_CSV_LIBRARY_URL)
                    "CSVFormat" -> openUrl(CSV_FORMAT_URL)
                }
            },
            linkColor = Color.WHITE
        )
        closeTipButton.setOnClickListener {
            tipLayout.isVisible = false
        }
        recordSeparatorEditText.isSelected = true
        delimiterEditText.isSelected = true
        nullStringEditText.isSelected = true
    }

    private fun observeViewModel() {
        with(viewModel) {
            formatName.observe { formatName: String ->
                dsvFormatNameTextView.text = formatName
            }
            setReadOnly(isReadOnly)
            delimiterEditText.setText(delimiter.toString().toDisplayedString())
            trailingDelimiter.observe { trailingDelimiter: Boolean ->
                yesTrailingDelimiterButton.isSelected = trailingDelimiter
                noTrailingDelimiterButton.isSelected = !trailingDelimiter
            }
            quoteCharacterEditText.setText(
                quoteCharacter.firstBlocking()?.toString()?.toDisplayedString()
            )
            quoteCharacter.observe { quoteCharacter: Char? ->
                quoteCharacterEditText.isSelected = quoteCharacter != null
                disabledQuoteCharacterTextView.isSelected = quoteCharacter == null
            }
            quoteMode.observe { quoteMode: QuoteMode? ->
                allQuoteModeButton.isSelected = quoteMode == QuoteMode.ALL
                allNonNullQuoteModeButton.isSelected = quoteMode == QuoteMode.ALL_NON_NULL
                minimalQuoteModeButton.isSelected =
                    quoteMode == null || quoteMode == QuoteMode.MINIMAL
                nonNumericQuoteModeButton.isSelected = quoteMode == QuoteMode.NON_NUMERIC
                noneQuoteModeButton.isSelected = quoteMode == QuoteMode.NONE
            }
            escapeCharacterEditText.setText(
                escapeCharacter.firstBlocking()?.toString()?.toDisplayedString()
            )
            escapeCharacter.observe { escapeCharacter: Char? ->
                escapeCharacterEditText.isSelected = escapeCharacter != null
                disabledEscapeCharacterTextView.isSelected = escapeCharacter == null
            }
            ignoreSurroundingSpaces.observe { ignoreSurroundingSpaces: Boolean ->
                yesIgnoreSurroundingSpacesButton.isSelected = ignoreSurroundingSpaces
                noIgnoreSurroundingSpacesButton.isSelected = !ignoreSurroundingSpaces
            }
            trim.observe { trim: Boolean ->
                yesTrimButton.isSelected = trim
                noTrimButton.isSelected = !trim
            }
            ignoreEmptyLines.observe { ignoreEmptyLines: Boolean ->
                yesIgnoreEmptyLinesButton.isSelected = ignoreEmptyLines
                noIgnoreEmptyLinesButton.isSelected = !ignoreEmptyLines
            }
            recordSeparatorEditText.setText(recordSeparator?.toDisplayedString())
            commentCharacterEditText.setText(
                commentMarker.firstBlocking()?.toString()?.toDisplayedString()
            )
            commentMarker.observe { commentMarker: Char? ->
                commentCharacterEditText.isSelected = commentMarker != null
                disabledCommentCharacterTextView.isSelected = commentMarker == null
            }
            skipHeaderRecord.observe { skipHeaderRecord: Boolean ->
                yesSkipHeaderRecordButton.isSelected = skipHeaderRecord
                noSkipHeaderRecordButton.isSelected = !skipHeaderRecord
            }
            ignoreHeaderCase.observe { ignoreHeaderCase: Boolean ->
                yesIgnoreHeaderNamesCaseButton.isSelected = ignoreHeaderCase
                noIgnoreHeaderNamesCaseButton.isSelected = !ignoreHeaderCase
            }
            allowDuplicateHeaderNames.observe { allowDuplicateHeaderNames: Boolean ->
                yesAllowDuplicateHeaderNamesButton.isSelected = allowDuplicateHeaderNames
                noAllowDuplicateHeaderNamesButton.isSelected = !allowDuplicateHeaderNames
            }
            allowMissingColumnNames.observe { allowMissingColumnNames: Boolean ->
                yesAllowMissingColumnNamesButton.isSelected = allowMissingColumnNames
                noAllowMissingColumnNamesButton.isSelected = !allowMissingColumnNames
            }
            autoFlush.observe { autoFlush: Boolean ->
                yesAutoFlushButton.isSelected = autoFlush
                noAutoFlushButton.isSelected = !autoFlush
            }
        }
    }

    private fun setReadOnly(readOnly: Boolean) {
        backButton.isVisible = readOnly
        cancelButton.isVisible = !readOnly
        doneButton.isVisible = !readOnly
        deleteDSVFormatButton.isVisible = !readOnly
        dsvFormatNameTextView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            marginEnd = if (readOnly) 16.dp else 4.dp
        }
        /*if (!readOnly)*/ setClickListeners()
    }

    private fun setClickListeners() {
        yesIgnoreSurroundingSpacesButton.setOnClickListener {
            controller?.dispatch(IgnoreSurroundingSpacesButton(true))
        }
        noIgnoreSurroundingSpacesButton.setOnClickListener {
            controller?.dispatch(IgnoreSurroundingSpacesButton(false))
        }
    }

    private fun String.toDisplayedString(): String {
        return replace("\\\\", "\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun String.toRegularString(): String {
        return replace("\\", "\\\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DsvFormatDiScope.close()
        }
    }

    companion object {
        const val APACHE_COMMONS_CSV_LIBRARY_URL = "https://commons.apache.org/proper/commons-csv/"
        const val CSV_FORMAT_URL =
            "https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html"
    }
}