package com.example.posterr.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.posterr.data.entity.DailyPostCountEntity


@Dao
interface DailyPostCountDao {

    @Query("SELECT count FROM daily_post_counts WHERE userId = :userId AND date = :date")
    suspend fun getPostCountForDate(userId: String, date: String): Int?

    @Query("SELECT COALESCE(count, 0) FROM daily_post_counts WHERE userId = :userId AND date = :date")
    suspend fun getPostCountForDateOrZero(userId: String, date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPostCount(dailyPostCount: DailyPostCountEntity)

    @Update
    suspend fun updateDailyPostCount(dailyPostCount: DailyPostCountEntity)

    @Query("""
        INSERT OR REPLACE INTO daily_post_counts (userId, date, count) 
        VALUES (:userId, :date, COALESCE((SELECT count FROM daily_post_counts WHERE userId = :userId AND date = :date), 0) + 1)
    """)
    suspend fun incrementPostCountForDate(userId: String, date: String)

    @Delete
    suspend fun deleteDailyPostCount(dailyPostCount: DailyPostCountEntity)

    @Query("DELETE FROM daily_post_counts")
    suspend fun deleteAllDailyPostCounts()
}