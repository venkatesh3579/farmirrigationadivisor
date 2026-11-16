package com.example.farmirrigationadvisor.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApiService {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,et0_fao_evapotranspiration",
        @Query("hourly") hourly: String = "soil_moisture_0_to_7cm",
        @Query("timezone") timezone: String = "auto"
    ): Response<OpenMeteoResponse>
}