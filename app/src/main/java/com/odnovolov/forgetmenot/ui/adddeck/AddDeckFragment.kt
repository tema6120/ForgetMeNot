package com.odnovolov.forgetmenot.ui.adddeck

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.Action.*
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.Event.*
import kotlinx.android.synthetic.main.fragment_add_deck.*
import leakcanary.LeakSentry

class AddDeckFragment : Fragment() {

    lateinit var viewModel: AddDeckViewModel
    private lateinit var deckNameInputDialog: AlertDialog
    private lateinit var deckNameEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_deck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        subscribeToViewModel()
    }

    private fun setupView() {
        val contentView = View.inflate(context, R.layout.dialog_deck_name_input, null)
        deckNameEditText = contentView.findViewById(R.id.deckNameEditText)
        deckNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text != null) {
                    viewModel.onEvent(DialogTextChanged(text.toString()))
                }
            }

        })
        deckNameInputDialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.enter_deck_name)
            .setView(contentView)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        deckNameInputDialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        deckNameInputDialog.setOnShowListener {
            deckNameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener { viewModel.onEvent(PositiveDialogButtonClicked) }
            deckNameInputDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener { viewModel.onEvent(NegativeDialogButtonClicked) }
            deckNameEditText.requestFocus()
        }
    }

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            isProcessing.observe(viewLifecycleOwner, Observer { isProcessing ->
                addDeckProgressBar.visibility = if (isProcessing) View.VISIBLE else View.GONE
            })
            isDialogVisible.observe(viewLifecycleOwner, Observer { isDialogVisible ->
                if (isDialogVisible) {
                    deckNameInputDialog.show()
                } else {
                    deckNameInputDialog.dismiss()
                }
            })
            errorText.observe(viewLifecycleOwner, Observer { errorText ->
                deckNameEditText.error = errorText
            })
            isPositiveButtonEnabled.observe(viewLifecycleOwner, Observer { isPositiveButtonEnabled ->
                deckNameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let {
                    it.isEnabled = isPositiveButtonEnabled
                }
            })
        }

        viewModel.action!!.observe(this, Observer { action ->
            when (action) {
                ShowFileChooser -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("text/plain")
                    startActivityForResult(intent, GET_CONTENT_REQUEST_CODE)
                }
                is ShowToast -> {
                    Toast.makeText(context, action.text, Toast.LENGTH_SHORT).show()
                }
                is SetDialogText -> {
                    deckNameEditText.setText(action.text)
                    deckNameEditText.selectAll()
                }
            }
        })
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val dialogState = savedInstanceState?.getBundle(STATE_KEY_DECK_NAME_INPUT_DIALOG)
        if (dialogState != null) {
            deckNameInputDialog.onRestoreInstanceState(dialogState)
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
        viewModel.onEvent(ContentReceived(inputStream, fileName))
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
        outState.putBundle(STATE_KEY_DECK_NAME_INPUT_DIALOG, deckNameInputDialog.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
        const val STATE_KEY_DECK_NAME_INPUT_DIALOG = "deckNameInputDialog"
    }
}