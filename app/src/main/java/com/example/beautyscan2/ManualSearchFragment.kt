package com.example.beautyscan2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController

class ManualSearchFragment : Fragment() {

    // Tworzymy zmienną dla naszego "mózgu" (ViewModelu)
    private lateinit var viewModel: ScannerViewModel
    private var currentScannedBarcode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Podłączenie ViewModelu do tego fragmentu
        viewModel = ViewModelProvider(this)[ScannerViewModel::class.java]

        // 2. Znalezienie elementów z pliku XML
        val etBarcode = view.findViewById<EditText>(R.id.etBarcode)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val rvIngredients = view.findViewById<RecyclerView>(R.id.rvIngredients)
        val btnGoToHistory = view.findViewById<Button>(R.id.btnGoToHistory)
        val tvCommunityRating = view.findViewById<TextView>(R.id.tvCommunityRating)
        val etUserRating = view.findViewById<EditText>(R.id.etUserRating)
        val btnSubmitRating = view.findViewById<Button>(R.id.btnSubmitRating)
        btnGoToHistory.setOnClickListener {
            // Magia Navigation Component - natychmiastowe przeniesienie na inny ekran!
            findNavController().navigate(R.id.historyFragment)
        }


        // Konfiguracja RecyclerView (lista potrzebuje LayoutManagera)
        rvIngredients.layoutManager = LinearLayoutManager(context)

        // 3. Co ma się stać po kliknięciu przycisku?
        btnSearch.setOnClickListener {
            val barcode = etBarcode.text.toString()
            if (barcode.isNotEmpty()) {
                // Przekazujemy kod do ViewModelu i... zapominamy o sprawie!
                currentScannedBarcode = barcode
                viewModel.searchBarcode(barcode)
            }
        }
        btnSubmitRating.setOnClickListener {
            val ratingStr = etUserRating.text.toString()
            val barcode = currentScannedBarcode

            if (barcode != null && ratingStr.isNotEmpty()) {
                val rating = ratingStr.toIntOrNull()
                if (rating != null && rating in 1..10) { // Walidacja skali 1-10
                    viewModel.rateProduct(barcode, rating)
                    etUserRating.text.clear()
                    Toast.makeText(context, "Dzięki za ocenę!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Podaj ocenę od 1 do 10", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Najpierw wyszukaj produkt", Toast.LENGTH_SHORT).show()
            }
        }


        // 4. MVVM: Obserwowanie danych (Ekran sam reaguje na zmiany z ViewModelu)

        // Obserwujemy sukces:
        viewModel.productResult.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                // Wyświetlamy ogólne info
                tvResult.text = "ZNALEZIONO!\n\nProdukt: ${product.productName}\nMarka: ${product.brands}"

                // Bierzemy długi tekst ze składem, dzielimy go na przecinkach (split) i usuwamy puste (filter)
                val ingredientsString = product.ingredientsText ?: ""
                val ingredientsList = ingredientsString.split(",").filter { it.isNotBlank() }

                // Tworzymy tłumacza (Adapter) i przekazujemy mu naszą listę składników
                val adapter = IngredientsAdapter(ingredientsList)
                // Przypinamy tłumacza do listy na ekranie
                rvIngredients.adapter = adapter

                tvCommunityRating.visibility = View.VISIBLE
                etUserRating.visibility = View.VISIBLE
                btnSubmitRating.visibility = View.VISIBLE
            }
        }

        viewModel.communityRating.observe(viewLifecycleOwner) { rating ->
            if (rating != null) {
                // Zaokrąglamy do 1 miejsca po przecinku
                val formattedRating = String.format("%.1f", rating)
                tvCommunityRating.text = "Ocena społeczności: $formattedRating / 10"
            } else {
                tvCommunityRating.text = "Brak ocen. Bądź pierwszy!"
            }
        }

        // Obserwujemy błędy:
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            tvResult.text = error
            // Czyścimy listę w razie błędu, wrzucając jej pusty Adapter
            rvIngredients.adapter = IngredientsAdapter(emptyList())
            tvCommunityRating.visibility = View.GONE
            etUserRating.visibility = View.GONE
            btnSubmitRating.visibility = View.GONE
        }

        // Obserwujemy stan ładowania:
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                tvResult.text = "" // Czyścimy stary tekst
                rvIngredients.adapter = IngredientsAdapter(emptyList()) // Czyścimy starą listę
                tvCommunityRating.text = "Ładowanie ocen..."
                tvCommunityRating.visibility = View.VISIBLE
                etUserRating.visibility = View.GONE
                btnSubmitRating.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }
}