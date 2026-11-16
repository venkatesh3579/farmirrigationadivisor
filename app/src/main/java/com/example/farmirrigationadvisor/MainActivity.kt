package com.example.farmirrigationadvisor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.farmirrigationadvisor.ui.AddFarmFragment
import com.example.farmirrigationadvisor.ui.DashboardFragment
import com.example.farmirrigationadvisor.ui.HistoryFragment
import com.example.farmirrigationadvisor.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navigation_dashboard -> DashboardFragment()
                R.id.navigation_add_farm -> AddFarmFragment()
                R.id.navigation_history -> HistoryFragment()
                R.id.navigation_profile -> ProfileFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}