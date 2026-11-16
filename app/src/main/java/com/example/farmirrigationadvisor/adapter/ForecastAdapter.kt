package com.example.farmirrigationadvisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.farmirrigationadvisor.R
import com.example.farmirrigationadvisor.model.ForecastDay

class ForecastAdapter(private var list: List<ForecastDay>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvForecastDate)
        val tvTemp: TextView = itemView.findViewById(R.id.tvForecastTemp)
        val tvRain: TextView = itemView.findViewById(R.id.tvForecastRain)
        val tvET0: TextView = itemView.findViewById(R.id.tvForecastET0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = list[position]
        holder.tvDate.text = item.date
        holder.tvTemp.text = "${item.maxTemp.toInt()}° / ${item.minTemp.toInt()}°C"
        holder.tvRain.text = "🌧️ ${item.rainfall} mm"
        holder.tvET0.text = "☀️ ET0: ${item.et0} mm"
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ForecastDay>) {
        list = newList
        notifyDataSetChanged()
    }
}