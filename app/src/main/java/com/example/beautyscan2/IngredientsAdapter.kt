package com.example.beautyscan2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredientsAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    // Klasa przechowująca odniesienie do naszego pola tekstowego w kafelku
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIngredientName: TextView = view.findViewById(R.id.tvIngredientName)
    }

    // Tworzenie nowego kafelka (na podstawie pliku item_ingredient.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    // Uzupełnianie kafelka danymi z naszej listy
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvIngredientName.text = ingredients[position].trim()
    }

    // Informowanie listy, ile ma wszystkich elementów
    override fun getItemCount(): Int {
        return ingredients.size
    }
}