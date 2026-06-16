package com.example.beautyscan2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController

class ManualSearchFragment : Fragment() {

    private lateinit var viewModel: ScannerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ScannerViewModel::class.java]

        val etBarcode = view.findViewById<EditText>(R.id.etBarcode)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val rvIngredients = view.findViewById<RecyclerView>(R.id.rvIngredients)
        val btnGoToHistory = view.findViewById<Button>(R.id.btnGoToHistory)
        val tvCommunityRating = view.findViewById<TextView>(R.id.tvCommunityRating)

        // NOWE ZMIENNE Z XML
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val btnSubmitRating = view.findViewById<Button>(R.id.btnSubmitRating)

        val btnOpenScanner = view.findViewById<Button>(R.id.btnOpenScanner)
        val tvAllergenWarning = view.findViewById<TextView>(R.id.tvAllergenWarning)

        rvIngredients.layoutManager = LinearLayoutManager(context)

        btnGoToHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        btnOpenScanner.setOnClickListener {
            findNavController().navigate(R.id.scannerFragment)
        }

        btnSearch.setOnClickListener {
            val barcode = etBarcode.text.toString()
            if (barcode.isNotEmpty()) {
                // Nie musimy już lokalnie zapisywać kodu, ViewModel to ogarnia!
                viewModel.searchBarcode(barcode)
            }
        }

        // Akcja wysyłania oceny z gwiazdek do Firebase
        btnSubmitRating.setOnClickListener {
            val rating = ratingBar.rating // Pobiera float od 0.0 do 5.0

            if (rating > 0) {
                viewModel.rateProduct(rating)
                Toast.makeText(context, "Zapisano ocenę!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Musisz zaznaczyć przynajmniej pół gwiazdki", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.productResult.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                tvResult.text = "ZNALEZIONO!\n\nProdukt: ${product.productName}\nMarka: ${product.brands}"

                val ingredientsString = product.ingredientsText ?: ""
                val ingredientsList = ingredientsString.split(",").filter { it.isNotBlank() }

                val hasAllergens = ingredientsList.any { ingredient ->
                    val lower = ingredient.lowercase()
                    IngredientsAdapter.badIngredients.any { badItem -> lower.contains(badItem) }
                }

                if (hasAllergens) {
                    tvAllergenWarning.visibility = View.VISIBLE
                } else {
                    tvAllergenWarning.visibility = View.GONE
                }

                rvIngredients.adapter = IngredientsAdapter(ingredientsList)

                // Ustawiamy gwiazdki jako widoczne
                tvCommunityRating.visibility = View.VISIBLE
                ratingBar.visibility = View.VISIBLE
                btnSubmitRating.visibility = View.VISIBLE
            }
        }

        viewModel.communityRating.observe(viewLifecycleOwner) { rating ->
            if (rating != null) {
                val formattedRating = String.format("%.2f", rating) // 2 miejsca po przecinku (np. 4.50)
                tvCommunityRating.text = "Ocena społeczności: $formattedRating / 5.0"
            } else {
                tvCommunityRating.text = "Brak ocen. Bądź pierwszy!"
            }
        }

        // Zaznaczamy na gwiazdkach to, co użytkownik dał wcześniej!
        viewModel.userRating.observe(viewLifecycleOwner) { rating ->
            if (rating != null) {
                ratingBar.rating = rating
                btnSubmitRating.text = "Zmień swoją ocenę" // Zmieniamy napis, by było jasne, że nadpisuje
            } else {
                ratingBar.rating = 0f
                btnSubmitRating.text = "Zapisz moją ocenę"
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            tvResult.text = error
            rvIngredients.adapter = IngredientsAdapter(emptyList())
            tvCommunityRating.visibility = View.GONE
            ratingBar.visibility = View.GONE
            btnSubmitRating.visibility = View.GONE
            tvAllergenWarning.visibility = View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                tvResult.text = ""
                rvIngredients.adapter = IngredientsAdapter(emptyList())
                tvAllergenWarning.visibility = View.GONE
                tvCommunityRating.text = "Ładowanie ocen..."
                tvCommunityRating.visibility = View.VISIBLE
                ratingBar.visibility = View.GONE
                btnSubmitRating.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }
}