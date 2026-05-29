package com.example.data

import android.content.Context
import com.example.data.local.SpamDatabase
import com.example.data.repository.SpamRepository

object ServiceLocator {
    @Volatile
    private var database: SpamDatabase? = null
    
    @Volatile
    private var repository: SpamRepository? = null

    fun getRepository(context: Context): SpamRepository {
        return repository ?: synchronized(this) {
            val db = database ?: SpamDatabase.getDatabase(context).also { database = it }
            SpamRepository(db.spamDao()).also { repository = it }
        }
    }
}
