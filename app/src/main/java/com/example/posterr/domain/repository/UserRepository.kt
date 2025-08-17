package com.example.posterr.domain.repository

import com.example.posterr.domain.model.User

interface UserRepository {
    suspend fun getLoggedInUser(): User?
}