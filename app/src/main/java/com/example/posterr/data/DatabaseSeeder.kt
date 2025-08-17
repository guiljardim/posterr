package com.example.posterr.data

import com.example.posterr.data.entity.PostEntity
import com.example.posterr.data.entity.UserEntity
import com.example.posterr.domain.model.PostType
import java.util.Calendar

object DatabaseSeeder {

    suspend fun seedDatabase(database: PosterrDatabase) {
        val userDao = database.userDao()
        val postDao = database.postDao()

        val users = listOf(
            UserEntity(
                id = "user1",
                username = "jardimtech",
                joinDate = getPastDate(daysAgo = 1000),
                isLoggedIn = true
            ),
            UserEntity(
                id = "user2",
                username = "androiddev",
                joinDate = getPastDate(daysAgo = 800),
                isLoggedIn = false
            ),
            UserEntity(
                id = "user3",
                username = "kotlinlover",
                joinDate = getPastDate(daysAgo = 700),
                isLoggedIn = false
            ),
            UserEntity(
                id = "user4",
                username = "composefan",
                joinDate = getPastDate(daysAgo = 600),
                isLoggedIn = false
            )
        )

        userDao.insertUsers(users)

        val posts = listOf(
            PostEntity(
                id = "post1",
                content = "Hello! Welcome to Posterr! 🚀",
                authorId = "user1",
                createdAt = getPastDate(daysAgo = 2),
                postType = PostType.ORIGINAL
            ),
            PostEntity(
                id = "post2",
                content = "Jetpack Compose is amazing for Android development! #AndroidDev #Compose",
                authorId = "user2",
                createdAt = getPastDate(daysAgo = 1),
                postType = PostType.ORIGINAL
            ),
            PostEntity(
                id = "post3",
                content = "Kotlin is the best language for Android! Null safety is fantastic! 🎯",
                authorId = "user3",
                createdAt = getPastDate(hoursAgo = 6),
                postType = PostType.QUOTE
            ),
            PostEntity(
                id = "post4",
                content = "Clean Architecture + Jetpack Compose = Quality Android applications! ✨",
                authorId = "user4",
                createdAt = getPastDate(hoursAgo = 3),
                postType = PostType.REPOST
            ),
            PostEntity(
                id = "repost1",
                content = "",
                authorId = "user1",
                createdAt = getPastDate(hoursAgo = 12),
                originalPostId = "post2",
                postType = PostType.QUOTE
            ),
            PostEntity(
                id = "repost2",
                content = "",
                authorId = "user3",
                createdAt = getPastDate(hoursAgo = 8),
                originalPostId = "post1",
                postType = PostType.QUOTE
            ),
            PostEntity(
                id = "quote1",
                content = "I totally agree! Compose revolutionized Android development! 🎉",
                authorId = "user4",
                createdAt = getPastDate(hoursAgo = 4),
                originalPostId = "post2",
                postType = PostType.REPOST
            ),
            PostEntity(
                id = "quote2",
                content = "Kotlin really is superior! Null safety + Coroutines = maximum productivity! 💪",
                authorId = "user2",
                createdAt = getPastDate(hoursAgo = 2),
                originalPostId = "post3",
                postType = PostType.QUOTE
            )
        )

        postDao.insertPosts(posts)
    }

    fun getPastDate(hoursAgo: Int = 0, daysAgo: Int = 0): Long {
        val calendar = Calendar.getInstance()
        if (daysAgo != 0) calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        if (hoursAgo != 0) calendar.add(Calendar.HOUR_OF_DAY, -hoursAgo)
        return calendar.timeInMillis
    }
}
