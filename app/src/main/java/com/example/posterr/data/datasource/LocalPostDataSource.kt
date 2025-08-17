package com.example.posterr.data.datasource

import com.example.posterr.data.dao.DailyPostCountDao
import com.example.posterr.data.dao.PostDao
import com.example.posterr.data.entity.DailyPostCountEntity
import com.example.posterr.data.entity.PostEntity
import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.ResponseResult
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPostDataSource @Inject constructor(
    private val postDao: PostDao,
    private val dailyPostCountDao: DailyPostCountDao
) {

    suspend fun getAllPosts(): List<Post> {
        val postEntities = postDao.getAllPosts()
        return convertToDomainModels(postEntities)
    }

    suspend fun createOriginalPost(content: String, authorId: String): ResponseResult<Post> {
        if (!canCreatePostToday(authorId)) {
            return ResponseResult.Error("Daily limit of 5 posts reached")
        }

        val postEntity = PostEntity(
            id = UUID.randomUUID().toString(),
            content = content,
            authorId = authorId,
            postType = PostType.ORIGINAL,
            createdAt = System.currentTimeMillis()
        )

        try {
            postDao.insertPost(postEntity)
            incrementPostsToday(authorId)
            return ResponseResult.Success(postEntity.toDomainModel())
        } catch (e: Exception) {
            return ResponseResult.Error("Failed to create post: ${e.message}")
        }
    }

    suspend fun createRepost(originalPostId: String, authorId: String): ResponseResult<Post> {
        val originalPostEntity = postDao.getPostById(originalPostId) ?: return ResponseResult.Error(
            "Original post not found"
        )

        if (!canCreatePostToday(authorId)) {
            return ResponseResult.Error("Daily limit of 5 posts reached")
        }

        val repostEntity = PostEntity(
            id = UUID.randomUUID().toString(),
            content = "",
            authorId = authorId,
            postType = PostType.REPOST,
            createdAt = System.currentTimeMillis(),
            originalPostId = originalPostId
        )

        try {
            postDao.insertPost(repostEntity)
            incrementPostsToday(authorId)
            val originalPost = originalPostEntity.toDomainModel()
            return ResponseResult.Success(repostEntity.toDomainModel(originalPost))
        } catch (e: Exception) {
            return ResponseResult.Error("Failed to create repost: ${e.message}")
        }
    }

    suspend fun createQuotePost(
        content: String,
        originalPostId: String,
        authorId: String
    ): ResponseResult<Post> {
        val originalPostEntity = postDao.getPostById(originalPostId) ?: return ResponseResult.Error(
            "Original post not found"
        )

        if (!canCreatePostToday(authorId)) {
            return ResponseResult.Error("Daily limit of 5 posts reached")
        }

        val quoteEntity = PostEntity(
            id = UUID.randomUUID().toString(),
            content = content,
            authorId = authorId,
            postType = PostType.QUOTE,
            createdAt = System.currentTimeMillis(),
            originalPostId = originalPostId
        )

        try {
            postDao.insertPost(quoteEntity)
            incrementPostsToday(authorId)
            val originalPost = originalPostEntity.toDomainModel()
            return ResponseResult.Success(quoteEntity.toDomainModel(originalPost))
        } catch (e: Exception) {
            return ResponseResult.Error("Failed to create quote post: ${e.message}")
        }
    }

    suspend fun getPostsCountToday(userId: String): Int {
        val today = getTodayMillis().toString()
        return try {
            dailyPostCountDao.getPostCountForDateOrZero(userId, today)
        } catch (e: Exception) {
            0
        }
    }

    suspend fun canCreatePostToday(userId: String): Boolean = getPostsCountToday(userId) < 5

    private suspend fun incrementPostsToday(userId: String) {
        val today = getTodayMillis().toString()
        try {
            val currentCount = dailyPostCountDao.getPostCountForDate(userId, today)
            val newCount = (currentCount ?: 0) + 1
            
            val dailyPostCount = DailyPostCountEntity(
                userId = userId,
                date = today,
                count = newCount
            )
            dailyPostCountDao.insertDailyPostCount(dailyPostCount)
        } catch (e: Exception) {
            try {
                val dailyPostCount = DailyPostCountEntity(
                    userId = userId,
                    date = today,
                    count = 1
                )
                dailyPostCountDao.insertDailyPostCount(dailyPostCount)
            } catch (e2: Exception) {
                // Silently handle error
            }
        }
    }

    private suspend fun convertToDomainModels(postEntities: List<PostEntity>): List<Post> {
        return postEntities.mapNotNull { postEntity ->
            try {
                val originalPost = if (postEntity.originalPostId != null) {
                    postDao.getPostById(postEntity.originalPostId)?.toDomainModel()
                } else null

                val domainPost = postEntity.toDomainModel(originalPost)
                domainPost
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPostsByUser(userId: String): List<Post> {
        val postEntities = postDao.getPostsByUser(userId)
        return convertToDomainModels(postEntities)
    }

    fun getTodayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}