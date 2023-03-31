package com.odnovolov.forgetmenot.presentation.screen.backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.openDocumentTree
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ExportButtonClicked
import kotlinx.android.synthetic.main.fragment_backup.*
import kotlinx.coroutines.launch
import java.io.OutputStream

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
        importButton.setOnClickListener {

        }
        exportButton.setOnClickListener {
            openDocumentTree(OPEN_DOCUMENT_TREE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pickedDir: DocumentFile? = getDirectory(requestCode, resultCode, data)
        if (pickedDir == null) {
            showToast(R.string.toast_couldnt_get_destination)
            return
        }

        val newFile: DocumentFile = pickedDir.createFile("*/*", "2023_03_31_18_20_44.zip") ?: return
        val outputStream: OutputStream =
            requireContext().contentResolver?.openOutputStream(newFile.uri) ?: return

        controller?.dispatch(ExportButtonClicked(outputStream))
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

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            BackupDiScope.close()
        }
    }

    companion object {
        const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 34
    }
}