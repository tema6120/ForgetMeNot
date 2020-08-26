package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SimpleHelpArticleFragment : Fragment() {
    companion object {
        private const val LAYOUT_RES_ARG_KEY = "LAYOUT_RES_ARG_KEY"

        fun create(layoutRes: Int) = SimpleHelpArticleFragment().apply {
            arguments = Bundle(1).apply {
                putInt(LAYOUT_RES_ARG_KEY, layoutRes)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutRes: Int = requireArguments().getInt(LAYOUT_RES_ARG_KEY)
        return inflater.inflate(layoutRes, container, false)
    }
}