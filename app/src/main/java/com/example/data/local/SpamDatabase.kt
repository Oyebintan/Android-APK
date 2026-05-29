package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SpamRecord::class], version = 1, exportSchema = false)
abstract class SpamDatabase : RoomDatabase() {
    abstract fun spamDao(): SpamDao

    companion object {
        @Volatile
        private var INSTANCE: SpamDatabase? = null

        fun getDatabase(context: Context): SpamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpamDatabase::class.java,
                    "spam_classifier_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
