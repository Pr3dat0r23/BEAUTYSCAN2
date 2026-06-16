package com.example.beautyscan2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BeautyRepository()
    private val historyDao = AppDatabase.getDatabase(application).historyDao()

    // TRZYMAMY KOD W PAMIĘCI - to naprawia błąd skanera!
    var currentBarcode: String? = null

    private val _productResult = MutableLiveData<ProductData?>()
    val productResult: LiveData<ProductData?> get() = _productResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _communityRating = MutableLiveData<Double?>()
    val communityRating: LiveData<Double?> get() = _communityRating

    // Własna ocena użytkownika
    private val _userRating = MutableLiveData<Float?>()
    val userRating: LiveData<Float?> get() = _userRating

    fun searchBarcode(barcode: String) {
        currentBarcode = barcode // Zapisujemy z czym pracujemy!

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Na starcie logujemy anonimowo, jeśli nie ma konta
                repository.signInAnonymouslyIfNeeded()

                val response = repository.fetchProductInfo(barcode)

                if (response.status == 1) {
                    val product = response.product
                    _productResult.value = product

                    // Pobieramy średnią ORAZ ocenę naszego usera z bazy
                    _communityRating.value = repository.getCommunityRating(barcode)
                    _userRating.value = repository.getUserSpecificRating(barcode)

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
                    _communityRating.value = null
                    _userRating.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rateProduct(rating: Float) {
        val barcode = currentBarcode ?: return // Zabezpieczenie

        viewModelScope.launch {
            try {
                repository.submitRating(barcode, rating)
                // Odświeżamy dane po wystawieniu oceny
                _communityRating.value = repository.getCommunityRating(barcode)
                _userRating.value = repository.getUserSpecificRating(barcode)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd podczas wysyłania oceny: ${e.message}"
            }
        }
    }
}