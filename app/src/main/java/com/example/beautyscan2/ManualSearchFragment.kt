package com.example.beautyscan2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ManualSearchFragment : Fragment() {

    // Tworzymy zmienną dla naszego "mózgu" (ViewModelu)
    private lateinit var viewModel: ScannerViewModel

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

        // 3. Co ma się stać po kliknięciu przycisku?
        btnSearch.setOnClickListener {
            val barcode = etBarcode.text.toString()
            if (barcode.isNotEmpty()) {
                // Przekazujemy kod do ViewModelu i... zapominamy o sprawie!
                viewModel.searchBarcode(barcode)
            }
        }

        // 4. MAGIA MVVM: Obserwowanie danych (Ekran sam reaguje na zmiany z ViewModelu)

        // Obserwujemy sukces:
        viewModel.productResult.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                tvResult.text = "ZNALEZIONO!\n\nProdukt: ${product.productName}\nMarka: ${product.brands}"
            }
        }

        // Obserwujemy błędy:
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            tvResult.text = error
        }

        // Obserwujemy stan ładowania (pokazuje lub ukrywa kółko):
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                tvResult.text = "" // Czyścimy stary tekst
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }
}