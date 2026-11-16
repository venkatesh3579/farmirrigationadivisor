package com.example.farmirrigationadvisor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "irrigation_records")
data class IrrigationRecord(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val date: String,
    val durationMinutes: Int,
    val farmName: String,
    val notes: String = ""
)