package com.example.posterr.domain.repository

import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats

interface UserRepository {
    suspend fun getLoggedInUser(): User?
    suspend fun getUserById(id: String): User?
    suspend fun getUserStats(userId: String): UserStats
}