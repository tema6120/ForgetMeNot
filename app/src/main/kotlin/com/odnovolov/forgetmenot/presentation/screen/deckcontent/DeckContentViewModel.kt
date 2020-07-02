package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DeckContentViewModel {
    val cards: Flow<List<Card>> = flowOf(
        listOf(
            Card(0, "What is your name?", "Artem"),
            Card(1, "Where do you live?", "Vovchansk")
        )
    )
}