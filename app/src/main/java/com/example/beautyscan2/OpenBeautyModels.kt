package com.example.beautyscan2

import com.google.gson.annotations.SerializedName

// Główna odpowiedź z serwera
data class OpenBeautyResponse(
    val code: String, // Zwrócony kod EAN
    val status: Int, // 1 oznacza, że znaleziono produkt
    val product: ProductData? // Dane produktu (może być null, jeśli nie ma go w bazie)
)

// Konkretne dane o kosmetyku
data class ProductData(
    @SerializedName("product_name")
    val productName: String?, // Nazwa kosmetyku

    val brands: String?, // Marka

    @SerializedName("ingredients_text_pl")
    val ingredientsTextPl: String?, // Skład w języku polskim (jeśli jest)

    @SerializedName("ingredients_text")
    val ingredientsText: String?, // Skład bazowy

    @SerializedName("ingredients_analysis_tags")
    val analysisTags: List<String>? // Tagi (wegański, mikroplastik)
)