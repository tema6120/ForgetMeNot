package com.odnovolov.forgetmenot.domain.feature.adddeck

import java.lang.Exception

class IllegalCardFormatException(override val message: String) : Exception(message)