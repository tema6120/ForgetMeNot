package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat

sealed class FileFormatEvent{
    class FileFormatRadioButtonClicked(val fileFormat: FileFormat) : FileFormatEvent()
    class ViewFileFormatSettingsButtonClicked(val fileFormat: FileFormat) : FileFormatEvent()
    class EditFileFormatSettingsButtonClicked(val fileFormat: FileFormat) : FileFormatEvent()
    object AddFileFormatSettingsButtonClicked : FileFormatEvent()
}