package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.*
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDiScope
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState

class BackupController(
    private val navigator: Navigator
) : BaseController<BackupEvent, Nothing>() {
    override val autoSave: Boolean = false

    override fun handle(event: BackupEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromNavHost {
                    val screenState = HelpArticleScreenState(HelpArticle.Backup)
                    HelpArticleDiScope.create(screenState)
                }
            }

            ImportButtonClicked -> {
                navigator.showBackupImportDialog(::BackupImportDiScope)
            }

            ExportButtonClicked -> {
                navigator.showBackupExportDialog(::BackupExportDiScope)
            }
        }
    }

    override fun saveState() {}
}