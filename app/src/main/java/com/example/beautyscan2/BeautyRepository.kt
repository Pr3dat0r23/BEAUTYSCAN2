package com.example.beautyscan2

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BeautyRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Logowanie  w tle,
    //  Unikalne ID dla kazdego z fonów
    suspend fun signInAnonymouslyIfNeeded() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    suspend fun fetchProductInfo(barcode: String): OpenBeautyResponse {
        return RetrofitClient.api.getProductInfo(barcode)
    }

    suspend fun getCommunityRating(barcode: String): Double? {
        return try {
            val document = db.collection("product_ratings").document(barcode).get().await()
            if (document.exists()) document.getDouble("averageRating") else null
        } catch (e: Exception) { null }
    }

    // Pobieranie indywidualnej oceny konkretnego użytkownika
    suspend fun getUserSpecificRating(barcode: String): Float? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val userDoc = db.collection("product_ratings").document(barcode)
                .collection("user_ratings").document(userId).get().await()

            if (userDoc.exists()) userDoc.getDouble("rating")?.toFloat() else null
        } catch (e: Exception) { null }
    }

    // Dodawanie/Aktualizacja oceny
    suspend fun submitRating(barcode: String, rating: Float) {
        val userId = auth.currentUser?.uid ?: throw Exception("Brak autoryzacji")
        val docRef = db.collection("product_ratings").document(barcode)
        val userRatingRef = docRef.collection("user_ratings").document(userId)

        db.runTransaction { transaction ->
            val productSnapshot = transaction.get(docRef)
            val userSnapshot = transaction.get(userRatingRef)

            var totalScore = productSnapshot.getDouble("totalScore") ?: 0.0
            var numberOfRatings = productSnapshot.getLong("numberOfRatings") ?: 0L

            if (userSnapshot.exists()) {
                // Użytkownik już ocenił - aktualizacja oceny
                val oldRating = userSnapshot.getDouble("rating") ?: 0.0
                totalScore = totalScore - oldRating + rating
            } else {
                // Pierwszy głos użytkownika na ten produkt
                totalScore += rating
                numberOfRatings += 1
            }

            val newAverage = if (numberOfRatings > 0) totalScore / numberOfRatings else 0.0

            // Zapis danych ogólnych produktu
            val productData = hashMapOf(
                "totalScore" to totalScore,
                "numberOfRatings" to numberOfRatings,
                "averageRating" to newAverage
            )
            transaction.set(docRef, productData)

            // Zapis indywidualnej oceny usera
            transaction.set(userRatingRef, hashMapOf("rating" to rating))
        }.await()
    }
}