package com.example.farmirrigationadvisor.model

data class ForecastDay(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val rainfall: Double,
    val et0: Double
)