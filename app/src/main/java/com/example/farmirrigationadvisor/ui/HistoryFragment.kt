package com.example.farmirrigationadvisor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmirrigationadvisor.R
import com.example.farmirrigationadvisor.adapter.HistoryAdapter
import com.example.farmirrigationadvisor.data.AppDatabase
import com.example.farmirrigationadvisor.data.IrrigationRecord
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmptyHistory: TextView
    private lateinit var fabAddIrrigation: FloatingActionButton
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHistory = view.findViewById(R.id.rvHistory)
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory)
        fabAddIrrigation = view.findViewById(R.id.fabAddIrrigation)

        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(emptyList())
        rvHistory.adapter = adapter

        fabAddIrrigation.setOnClickListener {
            showAddIrrigationDialog()
        }

        loadHistoryRecords()
    }

    override fun onResume() {
        super.onResume()
        loadHistoryRecords()
    }

    private fun loadHistoryRecords() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val records = withContext(Dispatchers.IO) {
                db.irrigationDao().getAllRecords()
            }

            if (records.isEmpty()) {
                tvEmptyHistory.visibility = View.VISIBLE
                rvHistory.visibility = View.GONE
            } else {
                tvEmptyHistory.visibility = View.GONE
                rvHistory.visibility = View.VISIBLE
                adapter.updateList(records)
            }
        }
    }

    private fun showAddIrrigationDialog() {
        val context = requireContext()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_irrigation, null)
        val etDuration = dialogView.findViewById<TextInputEditText>(R.id.etIrrigationDuration)
        val etNotes = dialogView.findViewById<TextInputEditText>(R.id.etIrrigationNotes)

        AlertDialog.Builder(context)
            .setTitle("Log Irrigation Record")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val durationStr = etDuration.text?.toString()?.trim() ?: ""
                val notes = etNotes.text?.toString()?.trim() ?: ""

                if (durationStr.isNotEmpty()) {
                    val duration = durationStr.toIntOrNull() ?: 0
                    saveIrrigationRecord(duration, notes)
                } else {
                    Toast.makeText(context, "Duration is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveIrrigationRecord(durationMinutes: Int, notes: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val latestFarm = withContext(Dispatchers.IO) {
                db.farmDao().getLatestFarm()
            }
            val farmName = latestFarm?.name ?: "Main Field"

            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val record = IrrigationRecord(
                date = currentDate,
                durationMinutes = durationMinutes,
                farmName = farmName,
                notes = notes
            )

            withContext(Dispatchers.IO) {
                db.irrigationDao().insertRecord(record)
            }
            Toast.makeText(requireContext(), "Irrigation record saved!", Toast.LENGTH_SHORT).show()
            loadHistoryRecords()
        }
    }
}