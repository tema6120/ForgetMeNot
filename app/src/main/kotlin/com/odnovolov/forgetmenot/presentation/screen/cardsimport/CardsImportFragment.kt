package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.setTransparentStatusBar
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportController.Command.Navigate.FilePageTransition.*
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportEvent.BackButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.CardsFileFragment
import kotlinx.coroutines.launch

class CardsImportFragment : BaseFragment() {
    init {
        CardsImportDiScope.reopenIfClosed()
    }

    private var controller: CardsImportController? = null
    private lateinit var viewModel: CardsImportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTransparentStatusBar(requireActivity())
        return inflater.inflate(R.layout.fragment_cards_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = CardsImportDiScope.getAsync() ?: return@launch
            controller = diScope.cardsImportController
            viewModel = diScope.cardsImportViewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun executeCommand(command: CardsImportController.Command) {
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
                fragment.isAppearingWithAnimation = true
                childFragmentManager.beginTransaction()
                    .setCustomAnimations(enterAnim, exitAnim)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
            }
            is ShowMessageNumberOfImportedCards -> {
                val message = resources.getQuantityString(
                    R.plurals.toast_number_of_imported_cards,
                    command.numberOfImportedCards,
                    command.numberOfImportedCards
                )
                showToast(message)
            }
            ShowMessageNoCardsToImport -> {
                showToast(R.string.toast_no_cards_to_import)
            }
            ShowMessageInvalidDeckName -> {
                showToast(R.string.toast_invalid_deck_name)
            }
            AskToImportIgnoringErrors -> {
                CardsImportErrorsBottomSheet().show(childFragmentManager, "ImportErrorsBottomSheet")
            }
            AskToConfirmExit -> {
                QuitCardsImportBottomSheet().show(childFragmentManager, "QuitFileImportBottomSheet")
            }
        }
    }

    private fun observeViewModel() {
        if (childFragmentManager.fragments.isEmpty()) {
            val fragment = CardsFileFragment.create(viewModel.currentCardsFileId)
            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            CardsImportDiScope.close()
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        controller?.dispatch(BackButtonClicked)
        true
    }
}