package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat

sealed class FileFormatEvent{
    object HelpButtonClicked : FileFormatEvent()
    class FileFormatRadioButtonClicked(val fileFormat: CardsFileFormat) : FileFormatEvent()
    class ViewFileFormatSettingsButtonClicked(val fileFormat: CardsFileFormat) : FileFormatEvent()
    class EditFileFormatSettingsButtonClicked(val fileFormat: CardsFileFormat) : FileFormatEvent()
    object AddFileFormatSettingsButtonClicked : FileFormatEvent()
}