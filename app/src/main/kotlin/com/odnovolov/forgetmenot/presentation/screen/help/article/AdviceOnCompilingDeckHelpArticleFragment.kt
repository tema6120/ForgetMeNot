package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.article_advice_on_compiling_deck.*

class AdviceOnCompilingDeckHelpArticleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_advice_on_compiling_deck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        articleContentTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}