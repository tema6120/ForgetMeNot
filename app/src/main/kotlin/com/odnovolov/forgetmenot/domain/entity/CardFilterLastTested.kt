package com.odnovolov.forgetmenot.domain.entity

import com.soywiz.klock.DateTimeSpan

interface CardFilterLastTested {
    var lastTestedFromTimeAgo: DateTimeSpan? // null means zero time
    var lastTestedToTimeAgo: DateTimeSpan? // null means now
}