package com.example.farmirrigationadvisor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cropType: String,
    val latitude: Double,
    val longitude: Double,
    val isPrimary: Boolean = true
)