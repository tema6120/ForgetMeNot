package com.odnovolov.forgetmenot.screen.intervals

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.entity.PresetNameInputDialogStatus
import com.odnovolov.forgetmenot.common.entity.PresetNameInputDialogStatus.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.SetDialogStatus
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.ShowModifyIntervalDialog

class IntervalsController : BaseController<IntervalsEvent, IntervalsOrder>() {
    private val queries: IntervalsControllerQueries = database.intervalsControllerQueries

    override fun handleEvent(event: IntervalsEvent) {
        when (event) {
            SaveIntervalSchemeButtonClicked -> {
                setPresetNameInputDialogStatus(VisibleToMakeIndividualPresetAsShared)
            }

            is SetIntervalSchemeButtonClicked -> {
                queries.setIntervalScheme(event.intervalSchemeId)
            }

            is RenameIntervalSchemeButtonClicked -> {
                val name: String? = queries.getIntervalSchemeNameById().executeAsOneOrNull()
                if (name != null && name.isNotEmpty()) {
                    queries.setRenameIntervalSchemeId(event.intervalSchemeId)
                    setPresetNameInputDialogStatus(VisibleToRenameSharedPreset)
                    issueOrder(SetDialogStatus(name))
                }
            }

            is DeleteIntervalSchemeButtonClicked -> {
                queries.deleteSharedIntervalScheme(event.intervalSchemeId)
            }

            AddNewIntervalSchemeButtonClicked -> {
                setPresetNameInputDialogStatus(VisibleToCreateNewSharedPreset)
            }

            is DialogTextChanged -> {
                queries.setTypedIntervalSchemeName(event.text)
                checkName()
            }

            PositiveDialogButtonClicked -> {
                if (checkName() === OK) {
                    when (getNameInputDialogStatus()) {
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
                    setPresetNameInputDialogStatus(Invisible)
                }
            }

            NegativeDialogButtonClicked -> {
                setPresetNameInputDialogStatus(Invisible)
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

    private fun getNameInputDialogStatus(): PresetNameInputDialogStatus {
        val databaseValue = queries.getPresetNameInputDialogStatus().executeAsOne()
        return presetNameInputDialogStatusAdapter.decode(databaseValue)
    }

    private fun setPresetNameInputDialogStatus(status: PresetNameInputDialogStatus) {
        val databaseValue = presetNameInputDialogStatusAdapter.encode(status)
        queries.setPresetNameInputDialogStatus(databaseValue)
    }

    private fun checkName(): NameCheckResult {
        val nameCheckResult = when {
            queries.isTypedIntervalSchemeNameEmpty().executeAsOne() -> EMPTY
            queries.isTypedIntervalSchemeNameOccupied().executeAsOne() -> OCCUPIED
            else -> OK
        }
        queries.setNameCheckResult(nameCheckResultAdapter.encode(nameCheckResult))
        return nameCheckResult
    }
}