package com.obelab.repace.model

import java.util.*

data class CalendarModel(
    var timeCalendar: Calendar?
) {
    companion object {
       var empty = CalendarModel(null)
    }
}