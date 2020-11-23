package com.odnovolov.forgetmenot.presentation.screen.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        appVersionTextView.text = "v" + BuildConfig.VERSION_NAME
        developerButton.setOnClickListener {
            startComposingMail()
        }
        sourceCodeButton.setOnClickListener {
            openUrl(SOURCE_CODE_URL)
        }
        privacyPolicyButton.setOnClickListener {
            openUrl(PRIVACY_POLICY_URL)
        }
        supportAppButton.setOnClickListener {

        }
    }

    private fun startComposingMail() {
        val uri = Uri.fromParts("mailto", DEVELOPER_EMAIL, null)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(Intent.createChooser(intent, null))
    }

    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    companion object {
        const val DEVELOPER_EMAIL = "odnovolov.artem@gmail.com"
        const val SOURCE_CODE_URL = "https://github.com/tema6120/ForgetMeNot"
        const val PRIVACY_POLICY_URL =
            "https://github.com/tema6120/ForgetMeNot/blob/master/PRIVACY_POLICY.md"
    }
}