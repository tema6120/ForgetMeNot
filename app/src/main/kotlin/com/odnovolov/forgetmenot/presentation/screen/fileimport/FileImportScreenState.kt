package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class FileImportScreenState(
    wasAskedToUseSelectedDeckForImportNextFiles: Boolean = false
) : FlowMaker<FileImportScreenState>() {
    var wasAskedToUseSelectedDeckForImportNextFiles: Boolean
            by flowMaker(wasAskedToUseSelectedDeckForImportNextFiles)
}