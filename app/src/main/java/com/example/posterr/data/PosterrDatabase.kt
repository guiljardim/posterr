package com.example.posterr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.posterr.data.dao.DailyPostCountDao
import com.example.posterr.data.dao.PostDao
import com.example.posterr.data.dao.UserDao
import com.example.posterr.data.entity.DailyPostCountEntity
import com.example.posterr.data.entity.PostEntity
import com.example.posterr.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        DailyPostCountEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PosterrDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
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
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: PosterrDatabase) {
            val userDao = database.userDao()
            val postDao = database.postDao()

            userDao.deleteAllUsers()
            postDao.deleteAllPosts()

            DatabaseSeeder.seedDatabase(database)
        }
    }
}