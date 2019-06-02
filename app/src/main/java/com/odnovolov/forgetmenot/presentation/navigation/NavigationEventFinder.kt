package com.odnovolov.forgetmenot.presentation.navigation

import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.News.ExerciseIsPrepared
import com.odnovolov.forgetmenot.presentation.navigation.Navigator.Event.NAVIGATE_TO_EXERCISE

object NavigationEventFinder {

    val fromDecksPreviewFeature: (DecksPreviewFeature.News) -> Navigator.Event? = { news ->
        when (news) {
            is ExerciseIsPrepared -> NAVIGATE_TO_EXERCISE
            else -> null
        }
    }
}