package com.odnovolov.forgetmenot.domain.feature.addnewdeck

import java.lang.Exception

class IllegalCardFormatException(override val message: String) : Exception(message)