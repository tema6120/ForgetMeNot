package com.odnovolov.forgetmenot.presentation.screen.supportapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.openEmailComposer
import com.odnovolov.forgetmenot.presentation.common.openShareWithChooser
import com.odnovolov.forgetmenot.presentation.common.openUrl
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
            openEmailComposer(AboutFragment.DEVELOPER_EMAIL)
        }
        helpTranslateButton.setOnClickListener {
            openUrl(HELP_TRANSLATE_URL)
        }
        shareWithFrindsButton.setOnClickListener {
            val shareText = "${getString(R.string.text_share_with_friends)}\n\n${GOOGLE_PLAY_URL}"
            openShareWithChooser(shareText)
        }
        facebookButton.setOnClickListener {
            openUrl(FACEBOOK_URL)
        }
        instagramButton.setOnClickListener {
            openUrl(INSTAGRAM_URL)
        }
        twitterButton.setOnClickListener {
            openUrl(TWITTER_URL)
        }
        youtubeButton.setOnClickListener {
            openUrl(YOUTUBE_URL)
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    companion object {
        const val GOOGLE_PLAY_URL =
            "https://play.google.com/store/apps/details?id=com.odnovolov.forgetmenot"
        const val NEW_ISSUE_ON_GITHUB_URL = "https://github.com/tema6120/ForgetMeNot/issues/new"
        const val HELP_TRANSLATE_URL =
            "https://github.com/tema6120/ForgetMeNot/blob/master/.github/readme/HOW_TO_TRANSLATE.md"
        const val FACEBOOK_URL = "https://www.facebook.com/Forgetmenot-Flashcards-103107271588768"
        const val INSTAGRAM_URL = "https://www.instagram.com/forgetmenot_flashcards"
        const val TWITTER_URL = "https://twitter.com/ForgetMeNot_FC"
        const val YOUTUBE_URL = "https://www.youtube.com/channel/UC5Hst5gp1HPLqCeGgKbAI9g"
    }
}