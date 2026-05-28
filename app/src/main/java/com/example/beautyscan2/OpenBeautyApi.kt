package com.example.beautyscan2

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenBeautyApi {


    // Słowo {barcode} zostanie dynamicznie podmienione na kod zeskanowany przez użytkownika.
    @GET("product/{barcode}.json")
    suspend fun getProductInfo(
        @Path("barcode") barcode: String
    ): OpenBeautyResponse
}