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

        // 1. Podłączenie współdzielonego ViewModelu (używamy requireActivity(), aby współgrał ze skanerem)
        viewModel = ViewModelProvider(requireActivity())[ScannerViewModel::class.java]

        // 2. Znalezienie wszystkich elementów z pliku XML
        val etBarcode = view.findViewById<EditText>(R.id.etBarcode)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val rvIngredients = view.findViewById<RecyclerView>(R.id.rvIngredients)
        val btnGoToHistory = view.findViewById<Button>(R.id.btnGoToHistory)
        val tvCommunityRating = view.findViewById<TextView>(R.id.tvCommunityRating)
        val etUserRating = view.findViewById<EditText>(R.id.etUserRating)
        val btnSubmitRating = view.findViewById<Button>(R.id.btnSubmitRating)
        val btnOpenScanner = view.findViewById<Button>(R.id.btnOpenScanner)
        val tvAllergenWarning = view.findViewById<TextView>(R.id.tvAllergenWarning)

        // Konfiguracja RecyclerView (lista potrzebuje LayoutManagera)
        rvIngredients.layoutManager = LinearLayoutManager(context)

        // Przejście do ekranu Historii
        btnGoToHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        // Przejście do ekranu Skanera (Aparatu)
        btnOpenScanner.setOnClickListener {
            findNavController().navigate(R.id.scannerFragment)
        }

        // 3. Akcja wyszukiwania po kodzie kreskowym
        btnSearch.setOnClickListener {
            val barcode = etBarcode.text.toString()
            if (barcode.isNotEmpty()) {
                currentScannedBarcode = barcode
                viewModel.searchBarcode(barcode)
            }
        }

        // Akcja wysyłania oceny użytkownika do Firebase
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

        // 4. MVVM: Obserwowanie danych (Ekran na żywo reaguje na zmiany w ViewModelu)

        // Obserwujemy sukces pobrania produktu:
        viewModel.productResult.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                // Wyświetlamy ogólne informacje o produkcie
                tvResult.text = "ZNALEZIONO!\n\nProdukt: ${product.productName}\nMarka: ${product.brands}"

                // Przetwarzamy tekst składników na listę
                val ingredientsString = product.ingredientsText ?: ""
                val ingredientsList = ingredientsString.split(",").filter { it.isNotBlank() }

                // INTELIGENTNA LOGIKA OSTRZEGANIA:
                // Sprawdzamy, czy jakikolwiek składnik znajduje się na czarnej liście w Adapterze
                val hasAllergens = ingredientsList.any { ingredient ->
                    val lower = ingredient.lowercase()
                    IngredientsAdapter.badIngredients.any { badItem -> lower.contains(badItem) }
                }

                // Pokazujemy baner ostrzegawczy tylko dla szkodliwych (czerwonych) substancji
                if (hasAllergens) {
                    tvAllergenWarning.visibility = View.VISIBLE
                } else {
                    tvAllergenWarning.visibility = View.GONE
                }

                // Wrzucamy listę do adaptera i podpinamy go do widoku
                val adapter = IngredientsAdapter(ingredientsList)
                rvIngredients.adapter = adapter

                // Pokazujemy sekcję ocen społeczności
                tvCommunityRating.visibility = View.VISIBLE
                etUserRating.visibility = View.VISIBLE
                btnSubmitRating.visibility = View.VISIBLE
            }
        }

        // Obserwujemy średnią ocenę społeczności z Firebase:
        viewModel.communityRating.observe(viewLifecycleOwner) { rating ->
            if (rating != null) {
                val formattedRating = String.format("%.1f", rating)
                tvCommunityRating.text = "Ocena społeczności: $formattedRating / 10"
            } else {
                tvCommunityRating.text = "Brak ocen. Bądź pierwszy!"
            }
        }

        // Obserwujemy błędy:
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            tvResult.text = error
            rvIngredients.adapter = IngredientsAdapter(emptyList()) // czyścimy listę
            tvCommunityRating.visibility = View.GONE
            etUserRating.visibility = View.GONE
            btnSubmitRating.visibility = View.GONE
            tvAllergenWarning.visibility = View.GONE
        }

        // Obserwujemy stan ładowania:
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                tvResult.text = ""
                rvIngredients.adapter = IngredientsAdapter(emptyList())
                tvAllergenWarning.visibility = View.GONE
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