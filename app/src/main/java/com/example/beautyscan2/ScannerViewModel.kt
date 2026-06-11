package com.example.beautyscan2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Dziedziczymy po AndroidViewModel, by móc pobrać kontekst aplikacji dla bazy
class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BeautyRepository()
    // Uruchamiamy połączenie z bazą Room
    private val historyDao = AppDatabase.getDatabase(application).historyDao()

    private val _productResult = MutableLiveData<ProductData?>()
    val productResult: LiveData<ProductData?> get() = _productResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _communityRating = MutableLiveData<Double?>()
    val communityRating: LiveData<Double?> get() = _communityRating

    fun searchBarcode(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Pobieranie danych z Open Beauty Facts
                val response = repository.fetchProductInfo(barcode)

                if (response.status == 1) {
                    val product = response.product
                    _productResult.value = product

                    // 2. Pobieranie oceny społeczności z Firebase
                    val rating = repository.getCommunityRating(barcode)
                    _communityRating.value = rating

                    if (product != null) {
                        val historyEntity = HistoryEntity(
                            barcode = barcode,
                            productName = product.productName ?: "Nieznany produkt",
                            brand = product.brands ?: "Nieznana marka",
                            rating = 0,
                            scanDate = System.currentTimeMillis()
                        )
                        historyDao.insertScan(historyEntity)
                    }
                } else {
                    _errorMessage.value = "Nie znaleziono produktu o kodzie: $barcode"
                    _communityRating.value = null // resetujemy ocenę
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rateProduct(barcode: String, rating: Int) {
        viewModelScope.launch {
            try {
                repository.submitRating(barcode, rating)
                // Odświeżamy ocenę, żeby od razu pokazać zaktualizowaną średnią
                _communityRating.value = repository.getCommunityRating(barcode)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd podczas wysyłania oceny: ${e.message}"
            }
        }
    }
}