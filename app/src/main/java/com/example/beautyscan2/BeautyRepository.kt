package com.example.beautyscan2


class BeautyRepository {

    // Funkcja odpytująca  API Open Beauty Facts
    suspend fun fetchProductInfo(barcode: String): OpenBeautyResponse {
        return RetrofitClient.api.getProductInfo(barcode)
    }
}