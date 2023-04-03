package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CsvParser
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.DsvFormatEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat.Companion.EXTENSION_CSV
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatDiScope
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatScreenState
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatScreenState.Purpose
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.fileformat.FileFormatEvent.*
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import org.apache.commons.csv.CSVFormat

class FileFormatController(
    private val cardsImporter: CardsImporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsImporterStateProvider: ShortTermStateProvider<CardsImporter.State>
) : BaseController<FileFormatEvent, Nothing>() {
    private val currentFileFormat: CardsFileFormat
        get() = with(cardsImporter.state) {
            files[currentPosition].format
        }

    override fun handle(event: FileFormatEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromCardsImport {
                    val screenState = HelpArticleScreenState(HelpArticle.ImportOfFile)
                    HelpArticleDiScope.create(screenState)
                }
            }

            is FileFormatRadioButtonClicked -> {
                cardsImporter.setFormat(event.fileFormat)
            }

            is ViewFileFormatSettingsButtonClicked -> {
                navigateToDsvFormat(event.fileFormat, Purpose.View)
            }

            is EditFileFormatSettingsButtonClicked -> {
                navigateToDsvFormat(event.fileFormat, Purpose.EditExisting)
            }

            AddFileFormatSettingsButtonClicked -> {
                val parser: CsvParser = currentFileFormat.parser as? CsvParser
                    ?: CsvParser(CSVFormat.DEFAULT)
                val newFileFormat = CardsFileFormat(
                    id = generateId(),
                    name = "",
                    extension = EXTENSION_CSV,
                    parser = parser,
                    isPredefined = false
                )
                navigateToDsvFormat(newFileFormat, Purpose.CreateNew)
            }
        }
    }

    private fun navigateToDsvFormat(fileFormat: CardsFileFormat, purpose: Purpose) {
        navigator.navigateToDsvFormat {
            val dsvFormatEditorState = State.createFrom(fileFormat)
            val screenState = DsvFormatScreenState(purpose)
            DsvFormatDiScope.create(dsvFormatEditorState, screenState)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsImporterStateProvider.save(cardsImporter.state)
    }
}