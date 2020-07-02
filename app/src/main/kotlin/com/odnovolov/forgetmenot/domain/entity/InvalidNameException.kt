package com.odnovolov.forgetmenot.domain.entity

class InvalidNameException(val nameCheckResult: NameCheckResult) : Exception()