package com.odnovolov.forgetmenot.presentation.screen

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.UiEventEmitterFragment
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.binding.HomeFragmentBinding
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.InputStream
import javax.inject.Inject

class HomeFragment : UiEventEmitterFragment<UiEvent>() {

    sealed class UiEvent {
        data class GotData(
            val inputStream: InputStream,
            val fileName: String?
        ) : UiEvent()

        data class SubmitRenameDialogText(val dialogText: String) : UiEvent()
        object CancelRenameDialog : UiEvent()
    }

    @Inject lateinit var adapter: DecksPreviewAdapter
    @Inject lateinit var binding: HomeFragmentBinding
    private var renameDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.home_actions)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        Injector.inject(this)
        setupToolbar()
        initRenameDeckDialog()
        recycler.adapter = adapter
        binding.setup(this)
        render(viewModel)
    }

    private fun setupToolbar() {
        toolbar.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.action_add -> {
                    showFileChooser()
                    true
                }
                else -> false
            }
        }
    }

    private fun initRenameDeckDialog() {
        val onPositive = { dialogText: String -> emitEvent(SubmitRenameDialogText(dialogText)) }
        val onNegative = { emitEvent(CancelRenameDialog) }

        val contentView = activity!!.layoutInflater.inflate(R.layout.dialog_rename_deck, null)
        val renameDeckEditText: EditText = contentView.findViewById(R.id.renameDeckEditText)
        renameDialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.enter_deck_name)
            .setView(contentView)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        renameDialog!!.setOnShowListener {
            renameDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener { onPositive.invoke(renameDeckEditText.text.toString()) }
            renameDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener { onNegative.invoke() }
        }
    }

    private fun render(viewModel: HomeViewModel) {
        viewModel.deckNames.observe(this, Observer { deckNames: List<String>? ->
            adapter.submitList(deckNames)
        })
        viewModel.isProcessing.observe(this, Observer { isProcessing: Boolean? ->
            isProcessing ?: return@Observer
            progressBar.visibility =
                if (isProcessing) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        })
        viewModel.isRenameDialogVisible.observe(this, Observer { isVisible: Boolean? ->
            isVisible ?: return@Observer
            if (isVisible) {
                renameDialog?.show()
            } else {
                renameDialog?.dismiss()
            }
        })
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
        startActivityForResult(intent, GET_CONTENT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == GET_CONTENT_REQUEST_CODE) {
            val uri = intent?.data ?: return
            val contentResolver: ContentResolver = context?.contentResolver ?: return
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val fileName = getFileName(contentResolver, uri)
            emitEvent(GotData(inputStream, fileName))
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }
            val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return cursor.getString(nameIndex)
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
    }
}