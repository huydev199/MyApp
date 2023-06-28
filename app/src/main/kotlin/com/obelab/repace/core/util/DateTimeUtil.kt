package com.obelab.repace.core.util

import java.util.concurrent.TimeUnit

object DateTimeUtil {
    fun convertLongtoSecondString(timeInLong : Long): String{
        var secondStringValue =""
        var secondIntValue =   TimeUnit.MILLISECONDS.toSeconds(timeInLong) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        timeInLong
                    )
                )
        if(secondIntValue>=10){
            secondStringValue = "$secondIntValue"
        }
        else{
            secondStringValue = "0$secondIntValue"
        }


        return secondStringValue
    }
    fun convertLongtoSecondValue(timeInLong : Long): Long{

        var secondIntValue =   TimeUnit.MILLISECONDS.toSeconds(timeInLong) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        timeInLong
                    )
                )



        return secondIntValue
    }

    fun convertLongtoMinuteSecondString(timeInLong : Long): String{
        var secondStringValue = ""
        var secondIntValue =   TimeUnit.MILLISECONDS.toSeconds(timeInLong) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        timeInLong
                    )
                )
        var minuteStringValue = ""
        var minuteIntValue =  TimeUnit.MILLISECONDS.toMinutes( timeInLong)
        if(secondIntValue>=10){
            secondStringValue = "$secondIntValue"
        }
        else{
            secondStringValue = "0$secondIntValue"
        }


        if(minuteIntValue>=10){
            minuteStringValue = "$minuteIntValue"
        }
        else{
            minuteStringValue = "0$minuteIntValue"
        }



        return "$minuteStringValue : $secondStringValue"
    }
}