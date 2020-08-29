package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.text.*
import android.text.Annotation
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import kotlinx.android.synthetic.main.article_walking_mode.*

class WalkingModeHelpArticleFragment : Fragment() {
    private val navigator by lazy { AppDiScope.get().navigator }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_walking_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val articleText = getText(R.string.article_walking_mode) as SpannedString
        val spannableString = SpannableString(articleText)
        val annotation: Annotation =
            articleText.getSpans(0, articleText.length, Annotation::class.java).first()
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                navigator.navigateToWalkingModeSettingsFromWalkingModeArticle()
            }

            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = true
            }
        }
        spannableString.setSpan(
            clickableSpan,
            articleText.getSpanStart(annotation),
            articleText.getSpanEnd(annotation),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        articleContentTextView.text = spannableString
        articleContentTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}