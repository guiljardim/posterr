package com.example.posterr.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["originalPostId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("authorId"),
        Index("originalPostId"),
        Index("createdAt")
    ]
)
data class PostEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val authorId: String,
    val createdAt: Long,
    val originalPostId: String? = null
)
