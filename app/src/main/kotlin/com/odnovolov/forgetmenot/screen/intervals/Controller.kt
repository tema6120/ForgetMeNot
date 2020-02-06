package com.odnovolov.forgetmenot.screen.intervals

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.SetDialogStatus
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.ShowModifyIntervalDialog

class IntervalsController : BaseController<IntervalsEvent, IntervalsOrder>() {
    private val queries: IntervalsControllerQueries = database.intervalsControllerQueries

    override fun handleEvent(event: IntervalsEvent) {
        when (event) {
            SaveIntervalSchemeButtonClicked -> {
                setNamePresetDialogStatus(VisibleToMakeIndividualPresetAsShared)
            }

            is SetIntervalSchemeButtonClicked -> {
                queries.setIntervalScheme(event.intervalSchemeId)
            }

            is RenameIntervalSchemeButtonClicked -> {
                val name: String? = queries.getIntervalSchemeNameById().executeAsOneOrNull()
                if (name != null && name.isNotEmpty()) {
                    queries.setRenameIntervalSchemeId(event.intervalSchemeId)
                    setNamePresetDialogStatus(VisibleToRenameSharedPreset)
                    issueOrder(SetDialogStatus(name))
                }
            }

            is DeleteIntervalSchemeButtonClicked -> {
                queries.deleteSharedIntervalScheme(event.intervalSchemeId)
            }

            AddNewIntervalSchemeButtonClicked -> {
                setNamePresetDialogStatus(VisibleToCreateNewSharedPreset)
            }

            is DialogTextChanged -> {
                queries.setTypedIntervalSchemeName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                if (checkName() === Ok) {
                    when (getNamePresetDialogStatus()) {
                        VisibleToMakeIndividualPresetAsShared -> {
                            queries.renameCurrent()
                        }
                        VisibleToCreateNewSharedPreset -> {
                            queries.createNewSharedIntervalScheme()
                            queries.bindNewSharedIntervalSchemeToCurrentExercisePreference()
                            queries.createNewIntervals()
                        }
                        VisibleToRenameSharedPreset -> {
                            queries.renameShared()
                        }
                        else -> {
                        }
                    }
                    setNamePresetDialogStatus(Invisible)
                }
            }

            NegativeDialogButtonClicked -> {
                setNamePresetDialogStatus(Invisible)
            }

            is ModifyIntervalButtonClicked -> {
                val interval = queries
                    .getIntervalByTargetLevelOfKnowledge(event.targetLevelOfKnowledge)
                    .executeAsOne()
                val chunks = interval.value.split(" ")
                val intervalNumber: Long = chunks[0].toLong()
                val intervalUnit: String = chunks[1]
                val modifyIntervalState = ModifyIntervalState.Impl(
                    interval.id,
                    intervalNumber,
                    intervalUnit
                )
                queries.cleanModifyIntervalState()
                queries.initModifyIntervalState(modifyIntervalState)
                issueOrder(ShowModifyIntervalDialog)
            }

            AddIntervalButtonClicked -> {
                queries.addInterval()
            }

            RemoveIntervalButtonClicked -> {
                queries.deleteLastInterval()
            }
        }
    }

    private fun getNamePresetDialogStatus(): NamePresetDialogStatus {
        val databaseValue = queries.getNamePresetDialogStatus().executeAsOne()
        return namePresetDialogStatusAdapter.decode(databaseValue)
    }

    private fun setNamePresetDialogStatus(status: NamePresetDialogStatus) {
        val databaseValue = namePresetDialogStatusAdapter.encode(status)
        queries.setNamePresetDialogStatus(databaseValue)
    }

    private fun checkName(): NameCheckResult {
        val nameCheckResult = when {
            queries.isTypedIntervalSchemeNameEmpty().executeAsOne() -> Empty
            queries.isTypedIntervalSchemeNameOccupied().executeAsOne() -> Occupied
            else -> Ok
        }
        queries.setNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }
}