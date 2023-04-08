package com.odnovolov.forgetmenot.presentation.screen.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.*
import kotlinx.android.synthetic.main.fragment_backup.*
import kotlinx.coroutines.launch

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
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
        importButton.setOnClickListener {
            controller?.dispatch(ImportButtonClicked)
        }
        exportButton.setOnClickListener {
            controller?.dispatch(ExportButtonClicked)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            BackupDiScope.close()
        }
    }
}