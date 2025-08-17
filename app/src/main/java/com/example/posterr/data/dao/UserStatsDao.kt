package com.example.posterr.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.posterr.data.entity.UserStatsEntity


@Dao
interface UserStatsDao {

    @Query("DELETE FROM user_stats")
    suspend fun deleteAllUserStats()

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    suspend fun getUserStats(userId: String): UserStatsEntity?
}
