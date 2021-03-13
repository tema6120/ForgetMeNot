package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ColorChooserViewModel(
    screenState: DeckListEditorScreenState
) {
    val selectedColor: Flow<Int> =
        screenState.flowOf(DeckListEditorScreenState::editableDeckListForColorChooser)
            .filterNotNull()
            .flatMapLatest { editableDeckListForColorChooser: EditableDeckList ->
                editableDeckListForColorChooser.flowOf(EditableDeckList::color)
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