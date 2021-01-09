package com.odnovolov.forgetmenot.presentation.screen.testingmethod

sealed class TestingMethodEvent {
    object HelpButtonClicked : TestingMethodEvent()
    object CloseTipButtonClicked : TestingMethodEvent()
    object WithoutTestingRadioButtonClicked : TestingMethodEvent()
    object SelfTestingRadioButtonClicked : TestingMethodEvent()
    object TestingWithVariantsRadioButtonClicked : TestingMethodEvent()
    object SpellCheckRadioButtonClicked : TestingMethodEvent()
}