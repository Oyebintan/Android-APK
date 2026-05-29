package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam_records")
data class SpamRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emailText: String,
    val prediction: String, // "spam" or "ham"
    val confidence: Double,
    val timestamp: Long = System.currentTimeMillis()
)
