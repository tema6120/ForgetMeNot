package com.odnovolov.forgetmenot.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import leakcanary.LeakSentry

class ExerciseCardFragment : Fragment() {

    companion object {
        private const val ARG_POSITION = "position"

        fun create(position: Int) =
            ExerciseCardFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(ARG_POSITION, position)
                }
            }
    }

    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt(ARG_POSITION) ?: throw IllegalStateException()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_card, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }
}