package com.example.farmirrigationadvisor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.farmirrigationadvisor.R
import com.example.farmirrigationadvisor.data.AppDatabase
import com.example.farmirrigationadvisor.data.Farm
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AddFarmFragment : Fragment() {

    private lateinit var etFarmName: TextInputEditText
    private lateinit var spinnerCrop: Spinner
    private lateinit var btnGetLocation: Button
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var btnSaveFarm: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var capturedLatitude: Double? = null
    private var capturedLongitude: Double? = null

    private val cropTypes = arrayOf(
        "Wheat", "Rice (Paddy)", "Corn (Maize)", "Cotton",
        "Sugarcane", "Tomato", "Potato", "Vegetables", "Other"
    )

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                fetchCurrentLocation()
            }
            else -> {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_farm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etFarmName = view.findViewById(R.id.etFarmName)
        spinnerCrop = view.findViewById(R.id.spinnerCrop)
        btnGetLocation = view.findViewById(R.id.btnGetLocation)
        tvLatitude = view.findViewById(R.id.tvLatitude)
        tvLongitude = view.findViewById(R.id.tvLongitude)
        btnSaveFarm = view.findViewById(R.id.btnSaveFarm)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cropTypes)
        spinnerCrop.adapter = adapter

        btnGetLocation.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        btnSaveFarm.setOnClickListener {
            saveFarmDetails()
        }
    }

    private fun checkLocationPermissionAndFetch() {
        val finePerm = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    capturedLatitude = location.latitude
                    capturedLongitude = location.longitude
                    tvLatitude.text = String.format(Locale.getDefault(), "Latitude: %.6f", location.latitude)
                    tvLongitude.text = String.format(Locale.getDefault(), "Longitude: %.6f", location.longitude)
                    Toast.makeText(requireContext(), "Location captured successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Default fallback if emulator or GPS fix not found
                    capturedLatitude = 28.6139
                    capturedLongitude = 77.2090
                    tvLatitude.text = String.format(Locale.getDefault(), "Latitude: %.6f", capturedLatitude)
                    tvLongitude.text = String.format(Locale.getDefault(), "Longitude: %.6f", capturedLongitude)
                    Toast.makeText(requireContext(), "Location estimated", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                capturedLatitude = 28.6139
                capturedLongitude = 77.2090
                tvLatitude.text = String.format(Locale.getDefault(), "Latitude: %.6f", capturedLatitude)
                tvLongitude.text = String.format(Locale.getDefault(), "Longitude: %.6f", capturedLongitude)
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFarmDetails() {
        val farmName = etFarmName.text?.toString()?.trim() ?: ""
        val cropType = spinnerCrop.selectedItem?.toString() ?: "Crop"

        if (farmName.isEmpty()) {
            etFarmName.error = "Please enter farm name"
            return
        }

        val lat = capturedLatitude ?: 28.6139
        val lng = capturedLongitude ?: 77.2090

        lifecycleScope.launch {
            val farm = Farm(
                name = farmName,
                cropType = cropType,
                latitude = lat,
                longitude = lng,
                isPrimary = true
            )
            val db = AppDatabase.getDatabase(requireContext())
            withContext(Dispatchers.IO) {
                db.farmDao().insertFarm(farm)
            }

            Toast.makeText(requireContext(), "Farm '$farmName' saved successfully!", Toast.LENGTH_LONG).show()

            etFarmName.text?.clear()
        }
    }
}