package com.odnovolov.forgetmenot.presentation.common

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R

fun Fragment.openTtsSettings() {
    val intent = Intent().apply {
        action = "com.android.settings.TTS_SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (intent.resolveActivity(requireContext().packageManager) != null) {
        startActivity(intent)
    } else {
        showToast(R.string.toast_no_tts_settings)
    }
}

fun Fragment.openUrl(url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(requireContext().packageManager) != null) {
        startActivity(Intent.createChooser(intent, null))
    } else {
        showToast(R.string.toast_no_browser)
    }
}

fun Fragment.openEmailComposer(receiver: String) {
    val uri = Uri.fromParts("mailto", receiver, null)
    val intent = Intent(Intent.ACTION_SENDTO, uri)
    if (intent.resolveActivity(requireContext().packageManager) != null) {
        startActivity(Intent.createChooser(intent, null))
    } else {
        showToast(R.string.toast_no_email_client)
    }
}

fun Fragment.openShareWithChooser(shareText: String) {
    val shareWithText = getString(R.string.share_with)
    val sharingIntent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, shareText)
    startActivity(Intent.createChooser(sharingIntent, shareWithText))
}

fun Fragment.openFileChooser(requestCode: Int) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("text/plain")
    if (intent.resolveActivity(requireContext().packageManager) != null) {
        val chooserIntent = Intent.createChooser(intent, null)
        startActivityForResult(chooserIntent, requestCode)
    } else {
        showToast(R.string.toast_no_file_manager_to_load)
    }
}

fun Fragment.openFileCreator(requestCode: Int, fileName: String) {
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TITLE, fileName)
    if (intent.resolveActivity(requireContext().packageManager) != null) {
        val chooserIntent = Intent.createChooser(intent, null)
        startActivityForResult(chooserIntent, requestCode)
    } else {
        showToast(R.string.toast_no_file_manager_to_create)
    }
}