package com.example.posterr.data.repository

import com.example.posterr.data.datasource.LocalUserDataSource
import com.example.posterr.domain.model.User
import com.example.posterr.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalUserDataSource
) : UserRepository {

    override suspend fun getLoggedInUser(): User? = localDataSource.getLoggedInUser()
}