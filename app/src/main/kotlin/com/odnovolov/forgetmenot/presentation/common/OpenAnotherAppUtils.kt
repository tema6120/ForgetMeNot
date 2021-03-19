package com.odnovolov.forgetmenot.presentation.common

import android.content.ActivityNotFoundException
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
    val mimeTypes = arrayOf(
        "text/plain", "text/txt",
        "text/comma-separated-values", "text/csv",
        "text/tab-separated-values", "text/tsv",
        "application/zip"
    )
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("*/*")
        .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    try {
        startActivityForResult(intent, requestCode)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.toast_no_file_manager_to_load)
    }
}

fun Fragment.openDocumentTree(requestCode: Int) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    try {
        startActivityForResult(intent, requestCode)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.toast_no_file_manager_to_create)
    }
}