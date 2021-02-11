package com.odnovolov.forgetmenot.presentation.screen.fileimport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate.FilePageTransition.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileFragment
import kotlinx.android.synthetic.main.fragment_file_import.*
import kotlinx.coroutines.launch

class FileImportFragment : BaseFragment() {
    init {
        FileImportDiScope.reopenIfClosed()
    }

    private var controller: FileImportController? = null
    private lateinit var viewModel: FileImportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            controller = diScope.fileImportController
            viewModel = diScope.fileImportViewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun executeCommand(command: FileImportController.Command) {
        when (command) {
            is Navigate -> {
                val enterAnim: Int
                val exitAnim: Int
                when (command.filePageTransition) {
                    SwipeToNext -> {
                        enterAnim = R.anim.slide_in_left
                        exitAnim = R.anim.slide_out_left
                    }
                    SwipeToPrevious -> {
                        enterAnim = R.anim.slide_in_right
                        exitAnim = R.anim.slide_out_right
                    }
                    SwipeToNextDroppingCurrent -> {
                        enterAnim = R.anim.slide_in_left
                        exitAnim = R.anim.slide_out_down_fading
                    }
                    SwipeToPreviousDroppingCurrent -> {
                        enterAnim = R.anim.slide_in_right
                        exitAnim = R.anim.slide_out_down_fading
                    }
                }
                val fragment = CardsFileFragment.create(command.cardsFileId)
                childFragmentManager.beginTransaction()
                    .setCustomAnimations(enterAnim, exitAnim)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
            }
        }
    }

    private fun observeViewModel() {
        if (childFragmentManager.fragments.isEmpty()) {
            val fragment = CardsFileFragment.create(viewModel.currentCardsFileId)
            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commitNow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            FileImportDiScope.close()
        }
    }
}