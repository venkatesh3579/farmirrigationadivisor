package com.example.farmirrigationadvisor.network

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String? = null,
    val hourly: HourlyData? = null,
    val daily: DailyData? = null
)

data class HourlyData(
    val time: List<String>? = null,
    @SerializedName("soil_moisture_0_to_7cm")
    val soilMoisture: List<Double>? = null
)

data class DailyData(
    val time: List<String>? = null,
    @SerializedName("temperature_2m_max")
    val tempMax: List<Double>? = null,
    @SerializedName("temperature_2m_min")
    val tempMin: List<Double>? = null,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Double>? = null,
    @SerializedName("et0_fao_evapotranspiration")
    val et0Evapotranspiration: List<Double>? = null
)