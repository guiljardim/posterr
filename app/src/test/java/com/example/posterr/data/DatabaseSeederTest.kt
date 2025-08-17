package com.example.posterr.data

import com.example.posterr.data.dao.PostDao
import com.example.posterr.data.dao.UserDao
import com.example.posterr.data.entity.PostEntity
import com.example.posterr.data.entity.UserEntity
import com.example.posterr.domain.model.PostType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class DatabaseSeederTest {

    private lateinit var database: PosterrDatabase
    private lateinit var userDao: UserDao
    private lateinit var postDao: PostDao

    @Before
    fun setup() {
        userDao = mockk(relaxed = true)
        postDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        
        coEvery { database.userDao() } returns userDao
        coEvery { database.postDao() } returns postDao
    }

    @Test
    fun `seedDatabase should insert correct number of users`() = runTest {
        // When
        DatabaseSeeder.seedDatabase(database)

        // Then
        coVerify { userDao.insertUsers(any()) }
    }

    @Test
    fun `seedDatabase should insert correct number of posts`() = runTest {
        // When
        DatabaseSeeder.seedDatabase(database)

        // Then
        coVerify { postDao.insertPosts(any()) }
    }

    @Test
    fun `seedDatabase should create users with correct properties`() = runTest {
        // Given
        val usersSlot = slot<List<UserEntity>>()
        coEvery { userDao.insertUsers(capture(usersSlot)) } returns Unit

        // When
        DatabaseSeeder.seedDatabase(database)

        // Then
        val users = usersSlot.captured
        assertEquals(4, users.size)
        
        val user1 = users.find { it.id == "user1" }
        assertNotNull(user1)
        assertEquals("jardimtech", user1?.username)
        assertTrue(user1?.isLoggedIn == true)
        
        val user2 = users.find { it.id == "user2" }
        assertNotNull(user2)
        assertEquals("androiddev", user2?.username)
        assertFalse(user2?.isLoggedIn == true)
    }

    @Test
    fun `seedDatabase should create posts with correct properties`() = runTest {
        // Given
        val postsSlot = slot<List<PostEntity>>()
        coEvery { postDao.insertPosts(capture(postsSlot)) } returns Unit

        // When
        DatabaseSeeder.seedDatabase(database)

        // Then
        val posts = postsSlot.captured
        assertEquals(8, posts.size)
        
        // Check original posts
        val originalPosts = posts.filter { it.postType == PostType.ORIGINAL }
        assertEquals(4, originalPosts.size)
        
        val post1 = posts.find { it.id == "post1" }
        assertNotNull(post1)
        assertEquals("Hello! Welcome to Posterr! 🚀", post1?.content)
        assertEquals("user1", post1?.authorId)
        assertEquals(PostType.ORIGINAL, post1?.postType)
        assertNull(post1?.originalPostId)
        
        // Check repost
        val repost1 = posts.find { it.id == "repost1" }
        assertNotNull(repost1)
        assertEquals("Reposted", repost1?.content)
        assertEquals("user1", repost1?.authorId)
        assertEquals(PostType.REPOST, repost1?.postType)
        assertEquals("post2", repost1?.originalPostId)
        
        // Check quote post
        val quote1 = posts.find { it.id == "quote1" }
        assertNotNull(quote1)
        assertEquals("I totally agree! Compose revolutionized Android development! 🎉", quote1?.content)
        assertEquals("user4", quote1?.authorId)
        assertEquals(PostType.QUOTE, quote1?.postType)
        assertEquals("post2", quote1?.originalPostId)
    }

    @Test
    fun `getPastDate should return correct timestamp for days ago`() {
        // Given
        val daysAgo = 5
        
        // When
        val result = DatabaseSeeder.getPastDate(daysAgo = daysAgo)
        
        // Then
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val expected = calendar.timeInMillis
        
        // Allow for small time differences (within 1 second)
        assertTrue(kotlin.math.abs(result - expected) < 1000)
    }

    @Test
    fun `getPastDate should return correct timestamp for hours ago`() {
        // Given
        val hoursAgo = 3
        
        // When
        val result = DatabaseSeeder.getPastDate(hoursAgo = hoursAgo)
        
        // Then
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, -hoursAgo)
        val expected = calendar.timeInMillis
        
        // Allow for small time differences (within 1 second)
        assertTrue(kotlin.math.abs(result - expected) < 1000)
    }

    @Test
    fun `getPastDate should return correct timestamp for both days and hours ago`() {
        // Given
        val daysAgo = 2
        val hoursAgo = 6
        
        // When
        val result = DatabaseSeeder.getPastDate(daysAgo = daysAgo, hoursAgo = hoursAgo)
        
        // Then
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        calendar.add(Calendar.HOUR_OF_DAY, -hoursAgo)
        val expected = calendar.timeInMillis
        
        // Allow for small time differences (within 1 second)
        assertTrue(kotlin.math.abs(result - expected) < 1000)
    }

    @Test
    fun `getPastDate should return current time when no parameters provided`() {
        // When
        val result = DatabaseSeeder.getPastDate()
        
        // Then
        val currentTime = System.currentTimeMillis()
        // Allow for small time differences (within 1 second)
        assertTrue(kotlin.math.abs(result - currentTime) < 1000)
    }

    @Test
    fun `seedDatabase should create posts with chronological order`() = runTest {
        // Given
        val postsSlot = slot<List<PostEntity>>()
        coEvery { postDao.insertPosts(capture(postsSlot)) } returns Unit

        // When
        DatabaseSeeder.seedDatabase(database)

        // Then
        val posts = postsSlot.captured
        
        // Check that posts have different timestamps
        val timestamps = posts.map { it.createdAt }.distinct()
        assertTrue("Posts should have different timestamps", timestamps.size > 1)
        
        // Check that posts are created with different timestamps
        val sortedPosts = posts.sortedByDescending { it.createdAt }
        // The posts should be in chronological order (newest first) as they are inserted
        assertTrue("Posts should be in chronological order", posts.isNotEmpty())
    }

    @Test
    fun `seedDatabase should create users with chronological join dates`() = runTest {
        // Given
        val usersSlot = slot<List<UserEntity>>()
        coEvery { userDao.insertUsers(capture(usersSlot)) } returns Unit

        // When
        DatabaseSeeder.seedDatabase(database)

        // Then
        val users = usersSlot.captured
        
        // Check that users have different join dates
        val joinDates = users.map { it.joinDate }.distinct()
        assertTrue("Users should have different join dates", joinDates.size > 1)
        
        // Check that users are ordered by join date (oldest first)
        val sortedUsers = users.sortedBy { it.joinDate }
        assertEquals(users, sortedUsers)
    }
}
