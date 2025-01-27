package com.example.progettolam.struct

class Record {
    var nameActivity: String? = null
    var duration: Int? = null
    var step: Int? = null
    var startDay: String? = null

    var startTime: String? = null
    var endTime: String? = null
    var endDay: String? = null


    override fun toString(): String {
        return "Record{" +
                "nameActivity='" + nameActivity + '\'' +
                ", duration=" + duration +
                ", step=" + step +
                ", startDay='" + startDay + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", endDay='" + endDay + '\'' +
                '}'
    }
}
