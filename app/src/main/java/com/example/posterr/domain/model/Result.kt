package com.example.posterr.domain.model

sealed class ResponseResult<out T> {
    data class Success<T>(val data: T) : ResponseResult<T>()
    data class Error(val message: String) : ResponseResult<Nothing>()
}

sealed class PostResult {
    data class Success(val post: Post) : PostResult()
    data class Error(val message: String) : PostResult()

    companion object {
        fun success(post: Post): PostResult = Success(post)
        fun error(message: String): PostResult = Error(message)
    }
}