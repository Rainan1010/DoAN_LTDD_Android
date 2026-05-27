package com.example.doan_ltdd

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NotificationDAO {
    @Insert
    suspend fun insert(notification: NotificationLog)

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationLog>>

    @Query("DELETE FROM notification_logs")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationLog>)
    @Query("SELECT COUNT(*) FROM notification_logs WHERE title = :title AND timestamp >= :startOfDay")
    suspend fun checkExistsToday(title: String, startOfDay: LocalDateTime): Int
}