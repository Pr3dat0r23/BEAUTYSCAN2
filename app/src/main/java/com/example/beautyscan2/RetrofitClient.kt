package com.example.beautyscan2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Główny, bazowy adres API Open Beauty Facts dla Polski
    private const val BASE_URL = "https://pl.openbeautyfacts.org/api/v1/"

    val api: OpenBeautyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Dodajemy konwerte, żeby Retrofit sam zamieniał JSON nae klasy
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenBeautyApi::class.java)
    }
}