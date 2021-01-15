package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command.SetDialogText
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command.ShowErrorMessage
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckEvent.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_adddeck.*
import kotlinx.android.synthetic.main.popup_add_cards.view.*
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class AddDeckFragment : BaseFragment() {
    init {
        AddDeckDiScope.reopenIfClosed()
    }

    private var controller: AddDeckController? = null
    private var pendingEvent: ContentReceived? = null
    private var addCardsPopup: PopupWindow? = null
    private var deckNameDialog: AlertDialog? = null
    private var deckNameDialogEditText: EditText? = null
    private var dialogOkButton: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adddeck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = AddDeckDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            val viewModel = diScope.viewModel
            observeViewModel(viewModel)
            controller!!.commands.observe(::executeCommand)
            pendingEvent?.let(controller!!::dispatch)
            pendingEvent = null
        }
    }

    private fun observeViewModel(viewModel: AddDeckViewModel) {
        with(viewModel) {
            isProcessing.observe { isProcessing ->
                progressBar.isVisible = isProcessing
            }
            isDialogVisible.observe { isDialogVisible ->
                if (isDialogVisible) {
                    requireDeckNameDialog().show()
                } else {
                    deckNameDialog?.dismiss()
                }
            }
        }
    }

    private fun executeCommand(command: AddDeckController.Command) {
        when (command) {
            is ShowErrorMessage -> {
                showToast(command.exception.message)
            }
            is SetDialogText -> {
                requireDeckNameDialog()
                deckNameDialogEditText!!.setText(command.text)
                deckNameDialogEditText!!.selectAll()
            }
        }
    }

    // it is called from parent fragment
    fun showAddCardsPopup(anchor: View) {
        requireAddCardsPopup().show(anchor, gravity = Gravity.TOP or Gravity.END)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK
            || requestCode != GET_CONTENT_REQUEST_CODE
            || intent == null
        ) {
            return
        }
        val uri = intent.data
        val contentResolver = context?.contentResolver
        val inputStream = uri?.let {
            try {
                contentResolver?.openInputStream(uri)
            } catch (e: FileNotFoundException) {
                val errorMessage: String = getString(R.string.error_loading_file, e.message)
                showToast(errorMessage)
                return
            }
        }
        if (uri == null || contentResolver == null || inputStream == null) return
        val fileName = getFileNameFromUri(uri, contentResolver)
        val event = ContentReceived(inputStream, fileName)
        if (controller == null) {
            pendingEvent = event
        } else {
            controller!!.dispatch(event)
        }
    }

    private fun getFileNameFromUri(uri: Uri, contentResolver: ContentResolver): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }
            val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return try {
                cursor.getString(nameIndex)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun requireAddCardsPopup(): PopupWindow {
        if (addCardsPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_add_cards, null)
                .apply {
                    importFileButton.setOnClickListener {
                        addCardsPopup?.dismiss()
                        openFileChooser(GET_CONTENT_REQUEST_CODE)
                    }
                    helpImportFileButton.setOnClickListener {
                        addCardsPopup?.dismiss()
                        controller?.dispatch(HelpImportFileButtonClicked)
                    }
                    browseCatalogButton.setOnClickListener {
                        addCardsPopup?.dismiss()
                        openUrl(DECK_CATALOG_PAGE)
                    }
                    createCardsHereButton.setOnClickListener {
                        addCardsPopup?.dismiss()
                        controller?.dispatch(AddCardsHereButtonClicked)
                    }
                }
            addCardsPopup = LightPopupWindow(content)
        }
        return addCardsPopup!!
    }

    private fun requireDeckNameDialog(): AlertDialog {
        if (deckNameDialog == null) {
            val dialogView = View.inflate(context, R.layout.dialog_input, null).apply {
                dialogTitle.setText(R.string.title_rename_deck_dialog)
                dialogInput.observeText { dialogText: String ->
                    controller?.dispatch(DialogTextChanged(dialogText))
                }
                okButton.setOnClickListener {
                    controller?.dispatch(DialogOkButtonClicked)
                }
                cancelButton.setOnClickListener {
                    controller?.dispatch(DialogCancelButtonClicked)
                }
                deckNameDialogEditText = dialogInput
                dialogOkButton = okButton
            }
            deckNameDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()
                .apply {
                    window?.setBackgroundDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.background_dialog)
                    )
                    setOnShowListener { deckNameDialogEditText?.showSoftInput() }
                }
            subscribeDeckNameDialogToViewModel()
        }
        return deckNameDialog!!
    }

    private fun subscribeDeckNameDialogToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = AddDeckDiScope.getAsync() ?: return@launch
            with(diScope.viewModel) {
                nameCheckResult.observe { nameCheckResult: NameCheckResult ->
                    deckNameDialogEditText?.error = when (nameCheckResult) {
                        Ok -> null
                        Empty -> getString(R.string.error_message_empty_name)
                        Occupied -> getString(R.string.error_message_occupied_name)
                    }
                    dialogOkButton?.isEnabled = nameCheckResult == Ok
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (deckNameDialog?.isShowing == true) {
            deckNameDialogEditText?.showSoftInput()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val dialogSavedState: Bundle? = getBundle(STATE_DECK_NAME_INPUT_DIALOG)
            if (dialogSavedState != null) {
                requireDeckNameDialog().onRestoreInstanceState(dialogSavedState)
            }
            val isAddCardsPopupShowing = getBoolean(STATE_ADD_CARDS_POPUP, false)
            if (isAddCardsPopupShowing) {
                val anchor: View? = parentFragment?.view?.findViewById(R.id.addCardsButton)
                if (anchor != null) {
                    showAddCardsPopup(anchor)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (deckNameDialog?.isShowing == true) {
            outState.putBundle(
                STATE_DECK_NAME_INPUT_DIALOG,
                deckNameDialog!!.onSaveInstanceState()
            )
        }
        val isAddCardsPopupShowing = addCardsPopup?.isShowing ?: false
        outState.putBoolean(STATE_ADD_CARDS_POPUP, isAddCardsPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deckNameDialog?.dismiss()
        deckNameDialog = null
        deckNameDialogEditText = null
        addCardsPopup?.dismiss()
        addCardsPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            AddDeckDiScope.close()
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
        const val DECK_CATALOG_PAGE =
            "https://drive.google.com/drive/folders/1sjHdkcChH2CvUi3jmhf--PNeVmA_716W?usp=sharing"
        const val STATE_DECK_NAME_INPUT_DIALOG = "STATE_DECK_NAME_INPUT_DIALOG"
        const val STATE_ADD_CARDS_POPUP = "STATE_ADD_CARDS_POPUP"
    }
}