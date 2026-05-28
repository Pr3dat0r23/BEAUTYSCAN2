package com.example.beautyscan2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // Funkcja do zapisywania zeskanowanego produktu


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(historyEntity: HistoryEntity)

    // Funkcja do pobierania całej historii, posortowanej od najnowszych
    // zwraca Flow wiec lista w aplikacji autualizuje się na żywo raczej?
    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>
}