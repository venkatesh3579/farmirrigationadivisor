package com.example.farmirrigationadvisor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmirrigationadvisor.R
import com.example.farmirrigationadvisor.adapter.ForecastAdapter
import com.example.farmirrigationadvisor.data.AppDatabase
import com.example.farmirrigationadvisor.data.Farm
import com.example.farmirrigationadvisor.model.ForecastDay
import com.example.farmirrigationadvisor.network.OpenMeteoResponse
import com.example.farmirrigationadvisor.network.RetrofitClient
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var tvFarmerName: TextView
    private lateinit var tvFarmDetails: TextView
    private lateinit var cardRecommendation: MaterialCardView
    private lateinit var tvRecommendationStatus: TextView
    private lateinit var tvRecommendationReason: TextView
    private lateinit var tvRainfallValue: TextView
    private lateinit var tvEt0Value: TextView
    private lateinit var tvSoilMoistureValue: TextView
    private lateinit var rvForecast: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var forecastAdapter: ForecastAdapter
    private var currentFarm: Farm? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvFarmerName = view.findViewById(R.id.tvFarmerName)
        tvFarmDetails = view.findViewById(R.id.tvFarmDetails)
        cardRecommendation = view.findViewById(R.id.cardRecommendation)
        tvRecommendationStatus = view.findViewById(R.id.tvRecommendationStatus)
        tvRecommendationReason = view.findViewById(R.id.tvRecommendationReason)
        tvRainfallValue = view.findViewById(R.id.tvRainfallValue)
        tvEt0Value = view.findViewById(R.id.tvEt0Value)
        tvSoilMoistureValue = view.findViewById(R.id.tvSoilMoistureValue)
        rvForecast = view.findViewById(R.id.rvForecast)
        progressBar = view.findViewById(R.id.progressBarDashboard)

        rvForecast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        forecastAdapter = ForecastAdapter(emptyList())
        rvForecast.adapter = forecastAdapter

        setupFarmerInfo()
    }

    override fun onResume() {
        super.onResume()
        loadFarmAndWeatherData()
    }

    private fun setupFarmerInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: user?.email?.substringBefore("@") ?: "Farmer"
        tvFarmerName.text = "Welcome, $name!"
    }

    private fun loadFarmAndWeatherData() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val db = AppDatabase.getDatabase(requireContext())
            currentFarm = withContext(Dispatchers.IO) {
                db.farmDao().getLatestFarm()
            }

            val lat: Double
            val lng: Double

            val farm = currentFarm
            if (farm != null) {
                tvFarmDetails.text = "Farm: ${farm.name} (${farm.cropType})"
                lat = farm.latitude
                lng = farm.longitude
            } else {
                tvFarmDetails.text = "No farm registered. Please tap 'Add Farm' tab."
                lat = 28.61
                lng = 77.23
            }

            fetchWeatherData(lat, lng)
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val response: Response<OpenMeteoResponse> = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getWeatherForecast(latitude, longitude)
                }
                progressBar.visibility = View.GONE

                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val daily = body.daily
                    val hourly = body.hourly

                    val todayRainfall = daily?.precipitationSum?.firstOrNull() ?: 0.0
                    val todayEt0 = daily?.et0Evapotranspiration?.firstOrNull() ?: 3.5
                    val soilList = hourly?.soilMoisture ?: emptyList()
                    val currentSoilMoisture = soilList.firstOrNull { it > 0.0 } ?: 0.18

                    tvRainfallValue.text = String.format(Locale.getDefault(), "%.1f mm", todayRainfall)
                    tvEt0Value.text = String.format(Locale.getDefault(), "%.1f mm", todayEt0)
                    tvSoilMoistureValue.text = String.format(Locale.getDefault(), "%.2f", currentSoilMoisture)

                    // Apply Irrigation Decision Rule:
                    // If ET0 > 3 mm AND soil moisture < 0.2 -> IRRIGATE, else NOT NEEDED
                    calculateIrrigationRecommendation(todayEt0, currentSoilMoisture)

                    // Build 7-day forecast
                    val forecastList = mutableListOf<ForecastDay>()
                    val timeList: List<String> = daily?.time ?: emptyList()
                    val maxTempList: List<Double> = daily?.tempMax ?: emptyList()
                    val minTempList: List<Double> = daily?.tempMin ?: emptyList()
                    val rainList: List<Double> = daily?.precipitationSum ?: emptyList()
                    val et0List: List<Double> = daily?.et0Evapotranspiration ?: emptyList()

                    for (i in timeList.indices) {
                        val date = timeList.getOrNull(i) ?: ""
                        val maxT = maxTempList.getOrNull(i) ?: 30.0
                        val minT = minTempList.getOrNull(i) ?: 18.0
                        val rain = rainList.getOrNull(i) ?: 0.0
                        val et0 = et0List.getOrNull(i) ?: 3.0
                        forecastList.add(ForecastDay(date, maxT, minT, rain, et0))
                    }
                    forecastAdapter.updateList(forecastList)

                } else {
                    useOfflineFallbackData("Failed to connect to weather server.")
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                useOfflineFallbackData("Offline mode: ${e.message}")
            }
        }
    }

    private fun calculateIrrigationRecommendation(et0: Double, soilMoisture: Double) {
        val context = context ?: return
        if (et0 > 3.0 && soilMoisture < 0.20) {
            tvRecommendationStatus.text = "IRRIGATE"
            tvRecommendationStatus.setTextColor(ContextCompat.getColor(context, R.color.orange_warning))
            cardRecommendation.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange_light))
            cardRecommendation.strokeColor = ContextCompat.getColor(context, R.color.orange_warning)

            tvRecommendationReason.text = String.format(
                Locale.getDefault(),
                "Evapotranspiration (ET0 = %.1f mm) > 3.0 mm AND soil moisture (%.2f) < 0.20. Watering recommended.",
                et0, soilMoisture
            )
        } else {
            tvRecommendationStatus.text = "NOT NEEDED"
            tvRecommendationStatus.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
            cardRecommendation.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
            cardRecommendation.strokeColor = ContextCompat.getColor(context, R.color.green_primary)

            if (et0 <= 3.0) {
                tvRecommendationReason.text = String.format(
                    Locale.getDefault(),
                    "ET0 (%.1f mm) <= 3.0 mm. Evapotranspiration water loss is low.",
                    et0
                )
            } else {
                tvRecommendationReason.text = String.format(
                    Locale.getDefault(),
                    "Soil moisture (%.2f) is adequate (>= 0.20). Irrigation is not needed right now.",
                    soilMoisture
                )
            }
        }
    }

    private fun useOfflineFallbackData(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            val fallbackRain = 0.5
            val fallbackEt0 = 4.2
            val fallbackSoil = 0.15

            tvRainfallValue.text = "$fallbackRain mm"
            tvEt0Value.text = "$fallbackEt0 mm"
            tvSoilMoistureValue.text = "$fallbackSoil"

            calculateIrrigationRecommendation(fallbackEt0, fallbackSoil)
        }
    }
}