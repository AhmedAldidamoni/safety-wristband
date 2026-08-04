package com.safetywristband.tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_events ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Insert
    suspend fun insert(alert: AlertEntity)

    @Query("DELETE FROM alert_events")
    suspend fun clearAll()
}
