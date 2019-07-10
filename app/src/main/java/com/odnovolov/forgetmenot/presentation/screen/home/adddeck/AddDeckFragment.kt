package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

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
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.News
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.News.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.ViewState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.di.AddDeckScreenComponent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_add_deck.*
import leakcanary.LeakSentry
import java.lang.Exception
import javax.inject.Inject

class AddDeckFragment : BaseFragment<ViewState, UiEvent, News>(), HomeFragment.AddButtonClickListener {

    @Inject lateinit var bindings: AddDeckFragmentBindings
    private lateinit var deckNameInputDialog: AlertDialog
    private lateinit var deckNameEditText: EditText
    private lateinit var timeCapsule: AndroidTimeCapsule

    override fun onCreate(savedInstanceState: Bundle?) {
        timeCapsule = AndroidTimeCapsule(savedInstanceState)
        AddDeckScreenComponent.createWith(timeCapsule).inject(this)
        super.onCreate(savedInstanceState)
        bindings.setup(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_deck, container, false)
        initRenameDialog()
        return rootView
    }

    private fun initRenameDialog() {
        val contentView = View.inflate(context, R.layout.dialog_deck_name_input, null)
        deckNameEditText = contentView.findViewById(R.id.deckNameEditText)
        deckNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text != null) {
                    emitEvent(DialogTextChanged(text.toString()))
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
                .setOnClickListener { emitEvent(PositiveDialogButtonClicked) }
            deckNameInputDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener { emitEvent(NegativeDialogButtonClicked) }
            deckNameEditText.requestFocus()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val dialogState = savedInstanceState?.getBundle(STATE_KEY_DECK_NAME_INPUT_DIALOG)
        if (dialogState != null) {
            deckNameInputDialog.onRestoreInstanceState(dialogState)
        }
    }

    override fun onAddButtonClicked() {
        emitEvent(AddButtonClicked)
    }

    override fun accept(viewState: ViewState) {
        addDeckProgressBar.visibility = if (viewState.isProcessing) View.VISIBLE else View.GONE
        if (viewState.isDialogVisible) {
            deckNameInputDialog.show()
        } else {
            deckNameInputDialog.dismiss()
        }
        deckNameEditText.error = viewState.errorText
        deckNameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let {
            it.isEnabled = viewState.isPositiveButtonEnabled
        }
    }

    override fun acceptNews(news: News) {
        when (news) {
            ShowFileChooser -> showFileChooser()
            is ShowToast -> showToast(news.text)
            is SetDialogText -> setDialogText(news.text)
        }
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
        startActivityForResult(intent, GET_CONTENT_REQUEST_CODE)
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT)
            .show()
    }

    private fun setDialogText(dialogText: String) {
        deckNameEditText.setText(dialogText)
        deckNameEditText.selectAll()
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
        emitEvent(ContentReceived(inputStream, fileName))
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
        timeCapsule.saveState(outState)
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