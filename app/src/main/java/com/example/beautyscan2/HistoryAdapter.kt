package com.example.beautyscan2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<HistoryEntity>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvHistoryName)
        val tvBrand: TextView = view.findViewById(R.id.tvHistoryBrand)
        val tvBarcode: TextView = view.findViewById(R.id.tvHistoryBarcode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.tvName.text = item.productName
        holder.tvBrand.text = item.brand
        holder.tvBarcode.text = "EAN: ${item.barcode}"
    }

    override fun getItemCount() = historyList.size
}