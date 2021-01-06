package androidx.fragment.app;

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray

class FragmentStateRestorer(
    private val fragment: Fragment
) {
    private var savedFragmentState: Bundle? = null
    private var savedViewState: SparseArray<Parcelable>? = null

    fun interceptSavedState() = fragment.interceptSavedState()

    fun restoreState() = fragment.restoreState()

    private fun Fragment.interceptSavedState() {
        savedFragmentState = mSavedFragmentState
        savedViewState = mSavedViewState
    }

    private fun Fragment.restoreState() {
        mSavedViewState = savedViewState
        restoreViewState(savedFragmentState)
    }
}