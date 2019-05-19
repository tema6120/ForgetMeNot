package com.odnovolov.forgetmenot.presentation.screen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.parser.IllegalCardFormatException
import com.odnovolov.forgetmenot.domain.feature.parser.Parser
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.InputStream

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
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
            parse(inputStream)
        }
    }

    fun parse(inputStream: InputStream) {
        textView.text = try {
            val deck: Deck = Parser.parse(inputStream)
            deck.toString()
        } catch (e: IllegalCardFormatException) {
            e.message
        }
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
    }

}