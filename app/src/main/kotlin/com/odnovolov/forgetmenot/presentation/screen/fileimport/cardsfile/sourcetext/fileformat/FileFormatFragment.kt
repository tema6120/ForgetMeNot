package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.presentation.common.DarkPopupWindow
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.show
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.ImportedTextEditorFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat.FileFormatEvent.*
import kotlinx.android.synthetic.main.fragment_file_format.*
import kotlinx.android.synthetic.main.popup_file_format.view.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FileFormatFragment : BaseFragment() {
    private var controller: FileFormatController? = null
    private lateinit var viewModel: FileFormatViewModel
    private var formatPopup: PopupWindow? = null
    private var dsvFileFormatAdapter: DsvFileFormatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_format, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            controller = diScope.fileFormatController
            val id = requireParentFragment().requireArguments()
                .getLong(ImportedTextEditorFragment.ARG_ID)
            viewModel = diScope.fileFormatViewModel(id)
            observeViewModel()
        }
    }

    private fun setupView() {
        fileFormatButton.setOnClickListener {
            showFormatPopup()
        }
    }

    private fun showFormatPopup() {
        requireFormatPopup().show(anchor = fileFormatButton, gravity = Gravity.BOTTOM)
    }

    private fun requireFormatPopup(): PopupWindow {
        if (formatPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_file_format, null).apply {
                helpButton.setOnClickListener {
                    controller?.dispatch(HelpButtonClicked)
                    formatPopup?.dismiss()
                }
                fmnFormatRadioButtonFrame.setOnClickListener {
                    controller?.dispatch(FileFormatRadioButtonClicked(FileFormat.FMN_FORMAT))
                    formatPopup?.dismiss()
                }
                addDsvFormatButton.setOnClickListener {
                    controller?.dispatch(AddFileFormatSettingsButtonClicked)
                    formatPopup?.dismiss()
                }
            }
            dsvFileFormatAdapter = DsvFileFormatAdapter(
                onItemClicked = { fileFormat ->
                    controller?.dispatch(FileFormatRadioButtonClicked(fileFormat))
                    formatPopup?.dismiss()
                },
                onViewFormatSettingsButtonClicked = { fileFormat ->
                    controller?.dispatch(ViewFileFormatSettingsButtonClicked(fileFormat))
                    formatPopup?.dismiss()
                },
                onEditFormatSettingsButtonClicked = { fileFormat ->
                    controller?.dispatch(EditFileFormatSettingsButtonClicked(fileFormat))
                    formatPopup?.dismiss()
                }
            )
            content.dsvFormatRecycler.adapter = dsvFileFormatAdapter
            formatPopup = DarkPopupWindow(content)
            subscribeFormatPopupToViewModel(content)
        }
        return formatPopup!!
    }

    private fun subscribeFormatPopupToViewModel(formatPopupContentView: View) {
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            val id = requireParentFragment().requireArguments()
                .getLong(ImportedTextEditorFragment.ARG_ID)
            val viewModel: FileFormatViewModel = diScope.fileFormatViewModel(id)
            with (viewModel) {
                isFmnFormatSelected.observe(formatPopupContentView.fmnFormatRadioButton::setChecked)
                dsvFileFormatItems.observe { dsvFileFormatItems: List<DsvFileFormat> ->
                    dsvFileFormatAdapter!!.items = dsvFileFormatItems
                }

                fun expandDsvFormatList(more: Boolean) {
                    formatPopupContentView.run {
                        dsvFormatRecycler.isVisible = more
                        addDsvFormatButton.isVisible = more
                        val expandIconRes = if (more)
                            R.drawable.ic_round_expand_less_32 else
                            R.drawable.ic_round_expand_more_32
                        dsvFormatListExpander.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, 0, expandIconRes, 0
                        )
                    }
                }
                val dsvFileFormatItems = dsvFileFormatItems.first()
                val isdDsvFileFormatSelected = dsvFileFormatItems.any { it.isSelected }
                expandDsvFormatList(more = isdDsvFileFormatSelected)
                formatPopupContentView.dsvFormatRadioButtonFrame.setOnClickListener {
                    expandDsvFormatList(more = !formatPopupContentView.dsvFormatRecycler.isVisible)
                }
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            formatName.observe(fileFormatButton::setText)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val needToShowFormatPopup = getBoolean(STATE_FORMAT_POPUP, false)
            if (needToShowFormatPopup) showFormatPopup()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isFormatPopupShowing = formatPopup?.isShowing ?: false
        outState.putBoolean(STATE_FORMAT_POPUP, isFormatPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        formatPopup?.dismiss()
        formatPopup = null
    }

    companion object {
        const val STATE_FORMAT_POPUP = "STATE_FORMAT_POPUP"
    }
}