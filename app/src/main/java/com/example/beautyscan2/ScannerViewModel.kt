package com.example.beautyscan2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {

    // Tworzymy instancję naszego repozytorium
    private val repository = BeautyRepository()

    // LiveData przechowująca wynik pobranego produktu
    private val _productResult = MutableLiveData<ProductData?>()
    val productResult: LiveData<ProductData?> get() = _productResult

    // LiveData przechowująca komunikaty o błędach
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData do pokazywania paska ładowania (kręcącego się kółka)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Główna funkcja wywoływana po zeskanowaniu kodu
    fun searchBarcode(barcode: String) {
        // Uruchamiamy proces w tle (Coroutines)
        viewModelScope.launch {
            _isLoading.value = true // Włączamy ładowanie
            try {
                // Prosimy repozytorium o dane
                val response = repository.fetchProductInfo(barcode)

                if (response.status == 1) {
                    // Sukces - znaleziono produkt
                    _productResult.value = response.product
                } else {
                    // Produktu nie ma w bazie
                    _errorMessage.value = "Nie znaleziono produktu o kodzie: $barcode"
                }
            } catch (e: Exception) {
                // Błąd sieci (np. brak internetu)
                _errorMessage.value = "Błąd połączenia: ${e.message}"
            } finally {
                _isLoading.value = false // Wyłączamy ładowanie
            }
        }
    }
}