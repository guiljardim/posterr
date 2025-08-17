package com.example.posterr.di

import android.content.Context
import com.example.posterr.data.PosterrDatabase
import com.example.posterr.data.dao.DailyPostCountDao
import com.example.posterr.data.dao.PostDao
import com.example.posterr.data.dao.UserDao
import com.example.posterr.data.datasource.LocalPostDataSource
import com.example.posterr.data.datasource.LocalUserDataSource
import com.example.posterr.data.repository.PostRepositoryImpl
import com.example.posterr.data.repository.UserRepositoryImpl
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import com.example.posterr.domain.useCase.CreatePostUseCase
import com.example.posterr.domain.useCase.GetAllPostsUseCase
import com.example.posterr.domain.useCase.PostValidationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun providePosterrDatabase(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope
    ): PosterrDatabase {
        return PosterrDatabase.getDatabase(context, applicationScope)
    }

    @Provides
    fun provideUserDao(database: PosterrDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun providePostDao(database: PosterrDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    fun provideDailyPostCountDao(database: PosterrDatabase): DailyPostCountDao {
        return database.dailyPostCountDao()
    }

    @Provides
    @Singleton
    fun provideLocalPostDataSource(
        postDao: PostDao,
        dailyPostCountDao: DailyPostCountDao
    ): LocalPostDataSource = LocalPostDataSource(postDao, dailyPostCountDao)

    @Provides
    @Singleton
    fun provideUserRepository(
        localDataSource: LocalUserDataSource
    ): UserRepository = UserRepositoryImpl(localDataSource)

    @Provides
    @Singleton
    fun providePostRepository(
        localDataSource: LocalPostDataSource
    ): PostRepository = PostRepositoryImpl(localDataSource)

    @Provides
    @Singleton
    fun provideGetAllPostsUseCase(
        postRepository: PostRepository
    ): GetAllPostsUseCase = GetAllPostsUseCase(postRepository)

    @Provides
    @Singleton
    fun providePostValidationUseCase(
        postRepository: PostRepository,
        userRepository: UserRepository
    ): PostValidationUseCase = PostValidationUseCase(postRepository, userRepository)

    @Provides
    @Singleton
    fun provideCreatePostUseCase(
        postRepository: PostRepository,
        userRepository: UserRepository,
        validationUseCase: PostValidationUseCase
    ): CreatePostUseCase = CreatePostUseCase(postRepository, userRepository, validationUseCase)
}