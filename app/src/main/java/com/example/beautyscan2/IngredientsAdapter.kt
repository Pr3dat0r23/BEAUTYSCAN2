package com.example.beautyscan2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredientsAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    // Listy w companion object, żeby inne pliki też miały do nich łatwy dostęp
    companion object {
        // CZARNA LISTA (czerwony) - szkodliwe, mikroplastiki, silne konserwanty, pochodne ropy naftowej
        val badIngredients = listOf(
            "paraben", "sls", "sles", "peg", "dimethicone", "bht", "edta",
            "triclosan", "formaldehyde", "phthalate", "petrolatum", "mineral oil",
            "paraffin", "methylisothiazolinone", "cyclopentasiloxane", "toluene",
            "polyacrylamide", "bha", "coal tar", "p-phenylenediamine"
        )

        // ŻÓŁTA LISTA (pomarańczowy) - potencjalnie drażniące, popularne alergeny zapachowe, wysuszające alkohole
        val warningIngredients = listOf(
            "parfum", "fragrance", "phenoxyethanol", "alcohol denat",
            "propylene glycol", "limonene", "linalool", "geraniol",
            "citronellol", "citral", "eugenol", "cocamidopropyl betaine",
            "talc", "benzyl alcohol", "aluminum"
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIngredientName: TextView = view.findViewById(R.id.tvIngredientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position].trim()
        holder.tvIngredientName.text = ingredient

        val lowerCaseIngredient = ingredient.lowercase()

        // Sprawdzamy, na której liście znajduje się składnik
        val isBad = badIngredients.any { lowerCaseIngredient.contains(it) }
        val isWarning = warningIngredients.any { lowerCaseIngredient.contains(it) }

        // Przypisywanie kolorów na podstawie list
        when {
            isBad -> {
                // Czerwony
                holder.tvIngredientName.setTextColor(Color.parseColor("#D32F2F"))
            }
            isWarning -> {
                // Pomarańczowy
                holder.tvIngredientName.setTextColor(Color.parseColor("#F57F17"))
            }
            else -> {
                // Zielony
                holder.tvIngredientName.setTextColor(Color.parseColor("#388E3C"))
            }
        }
    }

    override fun getItemCount() = ingredients.size
}