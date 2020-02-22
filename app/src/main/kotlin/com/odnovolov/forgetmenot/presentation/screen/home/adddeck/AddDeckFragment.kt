package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.observeText
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.*
import kotlinx.android.synthetic.main.fragment_adddeck.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class AddDeckFragment : BaseFragment() {
    private val koinScope = getKoin().getOrCreateScope<AddDeckViewModel>(ADD_DECK_SCOPE_ID)
    private val viewModel: AddDeckViewModel by koinScope.viewModel(this)
    private val controller: AddDeckController by koinScope.inject()
    private val fragmentScope = MainScope()
    private lateinit var deckNameInputDialog: AlertDialog
    private lateinit var deckNameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller.commands
            .onEach { executeCommand(it) }
            .launchIn(fragmentScope)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adddeck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        val contentView = View.inflate(context, R.layout.dialog_deck_name_input, null)
        deckNameEditText = contentView.findViewById(R.id.deckNameEditText)
        deckNameEditText.observeText { text -> controller.onDialogTextChanged(text) }
        deckNameInputDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.enter_deck_name)
            .setView(contentView)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        deckNameInputDialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        deckNameInputDialog.setOnShowListener {
            deckNameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener { controller.onPositiveDialogButtonClicked() }
            deckNameInputDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener { controller.onNegativeDialogButtonClicked() }
            deckNameEditText.requestFocus()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            isProcessing.observe { isProcessing ->
                progressBar.visibility = if (isProcessing) View.VISIBLE else View.GONE
            }
            isDialogVisible.observe { isDialogVisible ->
                if (isDialogVisible) {
                    deckNameInputDialog.show()
                } else {
                    deckNameInputDialog.dismiss()
                }
            }
            nameCheckResult.observe { nameCheckResult: NameCheckResult ->
                deckNameEditText.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
            }
            isPositiveButtonEnabled.observe { isPositiveButtonEnabled ->
                deckNameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let { positiveButton ->
                    positiveButton.isEnabled = isPositiveButtonEnabled
                }
            }
        }
    }

    private fun executeCommand(command: AddDeckCommand) {
        when (command) {
            is ShowErrorMessage -> {
                Toast.makeText(context, command.exception.message, Toast.LENGTH_SHORT).show()
            }
            is SetDialogText -> {
                deckNameEditText.setText(command.text)
                deckNameEditText.selectAll()
            }
            NavigateToDeckSettings -> {
                //findNavController().navigate(R.id.action_home_screen_to_deck_settings_screen)
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val dialogState = savedInstanceState?.getBundle(STATE_KEY_DECK_NAME_INPUT_DIALOG)
        if (dialogState != null) {
            deckNameInputDialog.onRestoreInstanceState(dialogState)
        }
    }

    // it is called from parent view
    fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
        startActivityForResult(intent, GET_CONTENT_REQUEST_CODE)
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
        val inputStream = uri?.let { contentResolver?.openInputStream(uri) }
        if (uri == null
            || contentResolver == null
            || inputStream == null
        ) {
            return
        }
        val fileName = getFileNameFromUri(uri, contentResolver)
        // give chance to launch commands collector coroutine
        fragmentScope.launch {
            controller.onContentReceived(inputStream, fileName)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::deckNameInputDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_DECK_NAME_INPUT_DIALOG,
                deckNameInputDialog.onSaveInstanceState()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentScope.cancel()
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
        const val STATE_KEY_DECK_NAME_INPUT_DIALOG = "deckNameInputDialog"
    }
}