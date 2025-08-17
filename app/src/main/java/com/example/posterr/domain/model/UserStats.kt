package com.example.posterr.domain.model

data class UserStats(
    val userId: String,
    val originalPostsCount: Int = 0,
    val repostCount: Int = 0,
    val quoteCount: Int = 0
) {
    val totalPosts: Int
        get() = originalPostsCount + repostCount + quoteCount

    fun incrementCount(postType: PostType): UserStats {
        return when (postType) {
            PostType.ORIGINAL -> copy(originalPostsCount = originalPostsCount + 1)
            PostType.REPOST -> copy(repostCount = repostCount + 1)
            PostType.QUOTE -> copy(quoteCount = quoteCount + 1)
        }
    }
}