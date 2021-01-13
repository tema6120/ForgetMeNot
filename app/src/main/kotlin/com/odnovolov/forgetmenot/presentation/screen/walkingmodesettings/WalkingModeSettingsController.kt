package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModeSettingsEvent.HelpButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModeSettingsEvent.KeyGestureActionSelected

class WalkingModeSettingsController(
    private val walkingModePreference: WalkingModePreference,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<WalkingModeSettingsEvent, Nothing>() {
    override fun handle(event: WalkingModeSettingsEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromWalkingModeSettings {
                    HelpArticleDiScope(HelpArticle.WalkingMode)
                }
            }

            is KeyGestureActionSelected -> {
                with(walkingModePreference) {
                    keyGestureMap = keyGestureMap.toMutableMap()
                        .apply { this[event.keyGesture] = event.keyGestureAction }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}