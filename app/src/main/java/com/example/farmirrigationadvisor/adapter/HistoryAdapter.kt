package com.example.farmirrigationadvisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.farmirrigationadvisor.R
import com.example.farmirrigationadvisor.data.IrrigationRecord

class HistoryAdapter(private var list: List<IrrigationRecord>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFarmName: TextView = itemView.findViewById(R.id.tvHistoryFarmName)
        val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        val tvDuration: TextView = itemView.findViewById(R.id.tvHistoryDuration)
        val tvNotes: TextView = itemView.findViewById(R.id.tvHistoryNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]
        holder.tvFarmName.text = item.farmName
        holder.tvDate.text = item.date
        holder.tvDuration.text = "⏱️ Duration: ${item.durationMinutes} min"
        if (item.notes.isNotEmpty()) {
            holder.tvNotes.visibility = View.VISIBLE
            holder.tvNotes.text = "Notes: ${item.notes}"
        } else {
            holder.tvNotes.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<IrrigationRecord>) {
        list = newList
        notifyDataSetChanged()
    }
}