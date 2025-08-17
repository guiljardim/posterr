package com.example.posterr.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.posterr.domain.model.UserStats

@Entity(
    tableName = "user_stats",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserStatsEntity(
    @PrimaryKey
    val userId: String,
    val originalPostsCount: Int = 0,
    val repostCount: Int = 0,
    val quoteCount: Int = 0
) {
    fun toDomainModel(): UserStats {
        return UserStats(
            userId = userId,
            originalPostsCount = originalPostsCount,
            repostCount = repostCount,
            quoteCount = quoteCount
        )
    }

}