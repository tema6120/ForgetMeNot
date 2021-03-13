package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.DeckList
import kotlinx.coroutines.flow.*

class ColorChooserViewModel(
    screenState: DeckListEditorScreenState
) {
    val selectedColor: Flow<Int> =
        screenState.flowOf(DeckListEditorScreenState::deckListForColorChooser)
            .filterNotNull()
            .flatMapLatest { deckListForColorChooser: DeckList ->
                deckListForColorChooser.flowOf(DeckList::color)
            }
            .share()

    val predefinedColors: Flow<List<SelectableColor>> =
        selectedColor.map { selectedColor: Int ->
            predefinedDeckListColors.map { predefinedColor: Int ->
                SelectableColor(predefinedColor, isSelected = predefinedColor == selectedColor)
            }
        }

    val hex: Flow<String> =
        selectedColor.map { color: Int -> String.format("%06X", (0xFFFFFF and color)) }
}