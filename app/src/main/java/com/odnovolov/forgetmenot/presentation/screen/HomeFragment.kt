package com.odnovolov.forgetmenot.presentation.screen

import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
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
import androidx.fragment.app.Fragment
import com.badoo.mvicore.binder.Binder
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.data.FakeRepository
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.Wish
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    lateinit var rootView: View
    val subject = PublishSubject.create<AddNewDeckFeature.Wish>()
    val adapter = DecksPreviewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.home_actions)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecycler()
        bindToFeature()
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

    private fun setupRecycler() {
        recycler.adapter = adapter
    }

    private fun bindToFeature() {
        val feature = AddNewDeckFeature(FakeRepository())
        val binder = Binder(lifecycle.adaptForBinder())
        binder.bind(subject to feature)
        binder.bind(feature to Consumer<AddNewDeckFeature.State>(::render))
    }

    private fun render(state: AddNewDeckFeature.State?) {
        state ?: return
        state.deck?.let { deck ->
            adapter.submitList(deck.cards)
        }
        when (state.stage) {
            is Idle ->{
                recycler.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
            is Processing -> {
                recycler.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
            is WaitingForName -> showRenameDeckDialog()
            is WaitingForChangingName -> showRenameDeckDialog()
            is Saving -> {
                recycler.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun showRenameDeckDialog() {
        val onPositive = { dialogText: String -> subject.onNext(Wish.OfferName(dialogText)) }
        val onNegative = { subject.onNext(Wish.Cancel) }

        val contentView = activity!!.layoutInflater.inflate(R.layout.dialog_rename_deck, null)
        val renameDeckEditText: EditText = contentView.findViewById(R.id.renameDeckEditText)
        AlertDialog.Builder(context!!)
            .setTitle(R.string.enter_deck_name)
            .setView(contentView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onPositive.invoke(renameDeckEditText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> onNegative.invoke() }
            .setOnCancelListener { onNegative.invoke() }
            .create()
            .show()
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
            val wish =
                if (fileName == null) {
                    Wish.AddFromInputStream(inputStream)
                } else {
                    Wish.AddFromInputStream(inputStream, fileName = fileName)
                }
            subject.onNext(wish)
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }
            val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val fileName: String = cursor.getString(nameIndex)
            return fileName
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
    }

}