package com.example.posterr.domain.model

data class User(
    val id: String,
    val username: String,
    val joinDate: Long,
    val isLoggedIn: Boolean = false
) {
    init {
        require(username.isNotBlank()) { "Username cannot be empty" }
        require(username.length <= 14) { "Username cannot exceed 14 characters" }
        require(username.all { it.isLetterOrDigit() }) { "Username must contain only alphanumeric characters" }
    }
}