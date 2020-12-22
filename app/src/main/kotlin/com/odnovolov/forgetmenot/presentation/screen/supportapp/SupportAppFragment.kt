package com.odnovolov.forgetmenot.presentation.screen.supportapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.about.AboutFragment
import kotlinx.android.synthetic.main.fragment_support_app.*

class SupportAppFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_support_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        rateUsButton.setOnClickListener {
            openUrl(GOOGLE_PLAY_URL)
        }
        writeReviewOnGooglePlayButton.setOnClickListener {
            openUrl(GOOGLE_PLAY_URL)
        }
        submitNewIssueOnGithubButton.setOnClickListener {
            openUrl(NEW_ISSUE_ON_GITHUB_URL)
        }
        sendEmailToAuthorButton.setOnClickListener {
            startComposingMail()
        }
        helpTranslateButton.setOnClickListener {
            // TODO
        }
        shareWithFrindsButton.setOnClickListener {
            shareWithFriends()
        }
        facebookButton.setOnClickListener {
            openUrl(FACEBOOK_URL)
        }
        instagramButton.setOnClickListener {
            // TODO
        }
        twitterButton.setOnClickListener {
            // TODO
        }
        youtubeButton.setOnClickListener {
            // TODO
        }
    }

    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun startComposingMail() {
        val uri = Uri.fromParts("mailto", AboutFragment.DEVELOPER_EMAIL, null)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(Intent.createChooser(intent, null))
    }

    private fun shareWithFriends() {
        val shareText = "${getString(R.string.text_share_with_friends)}\n\n$GOOGLE_PLAY_URL"
        val shareWithText = getString(R.string.share_with)
        val sharingIntent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(sharingIntent, shareWithText))
    }

    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null

    override fun onResume() {
        super.onResume()
        scrollListener = object : ViewTreeObserver.OnScrollChangedListener {
            private var canScrollUp = false

            override fun onScrollChanged() {
                val canScrollUp = contentScrollView?.canScrollVertically(-1) ?: return
                if (this.canScrollUp != canScrollUp) {
                    this.canScrollUp = canScrollUp
                    appBar?.isActivated = canScrollUp
                }
            }
        }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        scrollListener = null
    }

    companion object {
        const val GOOGLE_PLAY_URL =
            "https://play.google.com/store/apps/details?id=com.odnovolov.forgetmenot"
        const val NEW_ISSUE_ON_GITHUB_URL = "https://github.com/tema6120/ForgetMeNot/issues/new"
        const val FACEBOOK_URL = "https://www.facebook.com/Forgetmenot-Flashcards-103107271588768"
    }
}