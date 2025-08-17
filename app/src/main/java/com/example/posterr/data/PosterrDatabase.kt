package com.example.posterr.data

import com.example.posterr.data.converters.Converters
import com.example.posterr.data.dao.DailyPostCountDao
import com.example.posterr.data.dao.PostDao
import com.example.posterr.data.dao.UserDao
import com.example.posterr.data.dao.UserStatsDao
import com.example.posterr.data.entity.DailyPostCountEntity
import com.example.posterr.data.entity.PostEntity
import com.example.posterr.data.entity.UserEntity
import com.example.posterr.data.entity.UserStatsEntity

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        UserStatsEntity::class,
        DailyPostCountEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PosterrDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun dailyPostCountDao(): DailyPostCountDao

    companion object {
        @Volatile
        private var INSTANCE: PosterrDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): PosterrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosterrDatabase::class.java,
                    "posterr_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PosterrDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PosterrDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            populateDatabase(database)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            populateDatabase(database)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        suspend fun populateDatabase(database: PosterrDatabase) {
            val userDao = database.userDao()
            val postDao = database.postDao()
            val userStatsDao = database.userStatsDao()

            val existingUsers = userDao.getLoggedInUser()
            if (existingUsers == null) {
                userDao.deleteAllUsers()
                postDao.deleteAllPosts()
                userStatsDao.deleteAllUserStats()

                DatabaseSeeder.seedDatabase(database)
            }
        }
    }
}
