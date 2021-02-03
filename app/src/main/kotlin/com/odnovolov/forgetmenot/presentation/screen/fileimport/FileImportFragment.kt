package com.odnovolov.forgetmenot.presentation.screen.fileimport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*
import kotlinx.android.synthetic.main.fragment_file_import.*
import kotlinx.coroutines.launch

class FileImportFragment : BaseFragment() {
    init {
        FileImportDiScope.reopenIfClosed()
    }

    private var controller: FileImportController? = null
    private lateinit var viewModel: FileImportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel(isRecreated = savedInstanceState != null)
        }
    }

    private fun setupView() {
        previousButton.setOnClickListener {
            controller?.dispatch(CancelButtonClicked)
        }
        nextButton.setOnClickListener {
            controller?.dispatch(DoneButtonClicked)
        }
        sourceEditText.observeText { newText: String ->
            controller?.dispatch(TextChanged(newText))
        }
    }

    private fun observeViewModel(isRecreated: Boolean) {
        with(viewModel) {
            if (!isRecreated) {
                sourceEditText.setText(sourceText)
            }
            deckName.observe(deckNameTextView::setText)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            FileImportDiScope.close()
        }
    }
}