package com.example.beautyscan2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class HistoryEntity(
    @PrimaryKey
    val barcode: String, // Kod kreskowy (EAN): Unikalny identyfikator numeryczny.
    val productName: String, // Nazwa handlowa.
    val brand: String, // Producent lub brand kosmetyczny
    val rating: Int, // Wynik punktowy oceny
    val scanDate: Long // Data użycia/zeskanowania
)