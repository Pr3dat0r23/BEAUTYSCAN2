package com.example.beautyscan2

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BeautyRepository {

    // Instancja bazy Firestore
    private val db = FirebaseFirestore.getInstance()

    // Funkcja odpytująca API Open Beauty Facts
    suspend fun fetchProductInfo(barcode: String): OpenBeautyResponse {
        return RetrofitClient.api.getProductInfo(barcode)
    }

    // Pobieranie średniej oceny społeczności dla danego kodu
    suspend fun getCommunityRating(barcode: String): Double? {
        return try {
            // Szukamy dokumentu o nazwie takiej jak nasz kod kreskowy w kolekcji "product_ratings"
            val document = db.collection("product_ratings").document(barcode).get().await()
            if (document.exists()) {
                document.getDouble("averageRating")
            } else {
                null // Produkt nie ma jeszcze żadnych ocen
            }
        } catch (e: Exception) {
            null
        }
    }

    // Dodawanie nowej oceny (używamy transakcji, aby unikać konfliktów gdy kilka osób głosuje naraz)
    suspend fun submitRating(barcode: String, rating: Int) {
        val docRef = db.collection("product_ratings").document(barcode)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (snapshot.exists()) {
                // Produkt już istnieje w bazie, aktualizujemy dane
                val totalScore = snapshot.getLong("totalScore") ?: 0L
                val numberOfRatings = snapshot.getLong("numberOfRatings") ?: 0L

                val newTotalScore = totalScore + rating
                val newNumberOfRatings = numberOfRatings + 1
                val newAverage = newTotalScore.toDouble() / newNumberOfRatings

                transaction.update(docRef, "totalScore", newTotalScore)
                transaction.update(docRef, "numberOfRatings", newNumberOfRatings)
                transaction.update(docRef, "averageRating", newAverage)
            } else {
                // Pierwsza ocena tego produktu
                val data = hashMapOf(
                    "totalScore" to rating.toLong(),
                    "numberOfRatings" to 1L,
                    "averageRating" to rating.toDouble()
                )
                transaction.set(docRef, data)
            }
        }.await()
    }
}