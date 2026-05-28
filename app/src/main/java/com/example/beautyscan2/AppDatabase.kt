package com.example.beautyscan2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // połączenie z Dao(zapytaniami)
    abstract fun historyDao(): HistoryDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beautyscan_database" // Nazwa pliku bazy na urządzeniu
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}