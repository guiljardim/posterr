package com.example.posterr.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.posterr.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val joinDate: Long,
    val isLoggedIn: Boolean = false
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            username = username,
            joinDate = joinDate,
            isLoggedIn = isLoggedIn
        )
    }

}