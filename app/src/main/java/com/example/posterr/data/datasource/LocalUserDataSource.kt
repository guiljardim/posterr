package com.example.posterr.data.datasource

import com.example.posterr.data.dao.UserDao
import com.example.posterr.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserDataSource @Inject constructor(
    private val userDao: UserDao,
) {
    suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()?.toDomainModel()
    }
}