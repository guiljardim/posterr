package com.example.posterr.data.datasource

import com.example.posterr.data.dao.UserDao
import com.example.posterr.data.dao.UserStatsDao
import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserDataSource @Inject constructor(
    private val userDao: UserDao,
    private val userStatsDao: UserStatsDao
) {
    suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()?.toDomainModel()
    }

    suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)?.toDomainModel()
    }

    suspend fun getUserStats(userId: String): UserStats {
        val userStatsEntity = userStatsDao.getUserStats(userId)
        return userStatsEntity?.toDomainModel() ?: UserStats(userId)
    }
}