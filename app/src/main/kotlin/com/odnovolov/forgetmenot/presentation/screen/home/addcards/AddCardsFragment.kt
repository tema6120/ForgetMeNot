package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsController.Command.ShowCannotReadFilesMessage
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsEvent.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_add_cards.*
import kotlinx.android.synthetic.main.popup_add_cards.view.*
import kotlinx.coroutines.launch

class AddCardsFragment : BaseFragment() {
    init {
        AddCardsDiScope.reopenIfClosed()
    }

    private var controller: AddCardsController? = null
    private var pendingEvent: ReceivedContent? = null
    private var addCardsPopup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = AddCardsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            controller!!.commands.observe(::executeCommand)
            pendingEvent?.let(controller!!::dispatch)
            pendingEvent = null
        }
    }

    private fun setupView() {
        addCardsButton.setOnClickListener {
            showAddCardsPopup()
        }
    }

    private fun showAddCardsPopup() {
        requireAddCardsPopup().show(anchor = addCardsButton, gravity = Gravity.TOP or Gravity.END)
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
                    createNewDeckButton.setOnClickListener {
                        addCardsPopup?.dismiss()
                        controller?.dispatch(CreateNewDeckButtonClicked)
                    }
                }
            addCardsPopup = LightPopupWindow(content)
        }
        return addCardsPopup!!
    }

    private fun executeCommand(command: AddCardsController.Command) {
        when (command) {
            is ShowCannotReadFilesMessage -> {
                val nameList = command.fileNames.joinToString(separator = ",\n") { fileName ->
                    fileName ?: "----"
                }
                val errorMessage = getString(R.string.error_loading_file, nameList)
                showToast(errorMessage, duration = Toast.LENGTH_LONG)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK
            || requestCode != GET_CONTENT_REQUEST_CODE
            || intent == null
        ) {
            return
        }
        val event = ReceivedContent(intent)
        if (controller == null) {
            pendingEvent = event
        } else {
            controller!!.dispatch(event)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val needToShowAddCardsPopup = getBoolean(STATE_ADD_CARDS_POPUP, false)
            if (needToShowAddCardsPopup) showAddCardsPopup()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isAddCardsPopupShowing = addCardsPopup?.isShowing ?: false
        outState.putBoolean(STATE_ADD_CARDS_POPUP, isAddCardsPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addCardsPopup?.dismiss()
        addCardsPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            AddCardsDiScope.close()
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
        const val DECK_CATALOG_PAGE =
            "https://drive.google.com/drive/folders/1sjHdkcChH2CvUi3jmhf--PNeVmA_716W?usp=sharing"
        const val STATE_ADD_CARDS_POPUP = "STATE_ADD_CARDS_POPUP"
    }
}