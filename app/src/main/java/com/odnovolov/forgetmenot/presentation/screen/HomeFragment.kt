package com.odnovolov.forgetmenot.presentation.screen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.badoo.mvicore.binder.Binder

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.parser.ParserFeature
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class HomeFragment : Fragment() {

    lateinit var rootView: View
    val subject = PublishSubject.create<ParserFeature.Wish>()
    val adapter = DecksPreviewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        setupToolbar()
        setupRecycler()
        bindToFeature()
        return rootView
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.home_actions)
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
        val decksPreviewRecycler: RecyclerView = rootView.findViewById(R.id.decksPreviewRecycler)
        decksPreviewRecycler.adapter = adapter
    }

    private fun bindToFeature() {
        val feature = ParserFeature()
        val binder = Binder(lifecycle.adaptForBinder())
        binder.bind(subject to feature)
        binder.bind(feature to Consumer<ParserFeature.State>(::render))
    }

    private fun render(state: ParserFeature.State?) {
        val deck: Deck = state?.deck ?: return
        adapter.submitList(deck.cards)
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
            val inputStream = context?.contentResolver?.openInputStream(uri) ?: return
            subject.onNext(ParserFeature.Wish.Parse(inputStream))
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
    }

}