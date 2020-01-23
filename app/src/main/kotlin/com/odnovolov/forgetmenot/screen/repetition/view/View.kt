package com.odnovolov.forgetmenot.screen.repetition.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionService

class RepetitionFragment : BaseFragment() {

    private val viewModel = RepetitionViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repetition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        with(viewModel) {
            isPlaying.observe { isPlaying ->
                if (isPlaying) {
                    val intent = Intent(context, RepetitionService::class.java)
                    ContextCompat.startForegroundService(requireContext(), intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving) {
            val intent = Intent(context, RepetitionService::class.java)
            requireContext().stopService(intent)
        }
    }

}