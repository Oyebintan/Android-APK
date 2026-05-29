package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamDao {
    @Query("SELECT * FROM spam_records ORDER BY timestamp DESC LIMIT 10")
    fun getRecentRecords(): Flow<List<SpamRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SpamRecord)

    @Query("DELETE FROM spam_records WHERE id NOT IN (SELECT id FROM spam_records ORDER BY timestamp DESC LIMIT 10)")
    suspend fun pruneRecords()

    @Query("DELETE FROM spam_records")
    suspend fun clearAllRecords()
}
