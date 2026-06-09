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

    fun searchBarcode(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.fetchProductInfo(barcode)

                if (response.status == 1) {
                    val product = response.product
                    _productResult.value = product

                    // ZAPIS DO BAZY DANYCH ROOM (Nowy kod!)
                    if (product != null) {
                        val historyEntity = HistoryEntity(
                            barcode = barcode,
                            productName = product.productName ?: "Nieznany produkt",
                            brand = product.brands ?: "Nieznana marka",
                            rating = 0, // na razie brak oceny
                            scanDate = System.currentTimeMillis() // obecna data
                        )
                        // Funkcja z pliku HistoryDao
                        historyDao.insertScan(historyEntity)
                    }
                } else {
                    _errorMessage.value = "Nie znaleziono produktu o kodzie: $barcode"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}