package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CsvParser
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FmnFormatParser
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser
import com.odnovolov.forgetmenot.presentation.common.DarkPopupWindow
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.show
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.ImportedTextEditorFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat.FileFormatEvent.FmnFormatRadioButtonClicked
import kotlinx.android.synthetic.main.fragment_file_format.*
import kotlinx.android.synthetic.main.popup_file_format.view.*
import kotlinx.coroutines.launch

class FileFormatFragment : BaseFragment() {
    private var controller: FileFormatController? = null
    private lateinit var viewModel: FileFormatViewModel
    private var formatPopup: PopupWindow? = null

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
                fmnFormatRadioButtonFrame.setOnClickListener {
                    controller?.dispatch(FmnFormatRadioButtonClicked)
                }
                dsvFormatRadioButtonFrame.setOnClickListener {

                }
            }
            formatPopup = DarkPopupWindow(content)
            subscribeFormatPopupToViewModel()
        }
        return formatPopup!!
    }

    private fun subscribeFormatPopupToViewModel() {

    }

    private fun observeViewModel() {
        with(viewModel) {
            parser.observe { parser: Parser ->
                when (parser) {
                    is CsvParser -> {
                        fileFormatButton.text = "CSV"
                    }
                    is FmnFormatParser -> {
                        fileFormatButton.setText(R.string.fmn_file_format_abbreviation)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        formatPopup?.dismiss()
        formatPopup = null
    }
}