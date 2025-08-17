package com.example.posterr.domain.model

data class Post(
    val id: String,
    val content: String,
    val authorId: String,
    val type: PostType,
    val createdAt: Long,
    val originalPost: Post? = null
) {
    init {
        when (type) {
            PostType.ORIGINAL -> {
                require(content.isNotBlank()) { "Content cannot be empty" }
                require(content.length <= 777) { "Content cannot exceed 777 characters" }
                require(originalPost == null) { "Original post cannot have originalPost reference" }
            }

            PostType.REPOST -> {
                require(originalPost != null) { "Repost must reference original post" }
            }

            PostType.QUOTE -> {
                require(content.isNotBlank()) { "Content cannot be empty" }
                require(content.length <= 777) { "Content cannot exceed 777 characters" }
                require(originalPost != null) { "Quote must reference original post" }
            }
        }
    }
}