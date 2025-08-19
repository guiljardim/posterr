# Posterr - Social Media Application

**Developer:** Guilherme Jardim

## 📱 Project Overview

Posterr is a social media application similar to Twitter, built as a mobile app using Android/Kotlin. The application features a simplified social media experience with two main screens: Home and User Profile.

### 🎯 Key Features

- **Home Screen**: Displays a feed of all posts (original, reposts, and quotes) from all users
- **Profile Screen**: Shows user information and their personal post feed
- **Post Creation**: Users can create original posts, repost others' content, and quote posts with comments
- **Real-time Validation**: Character counter, daily post limits, and input validation
- **Persistent Data**: Local database storage with Room
- **Modern UI**: Built with Jetpack Compose and Material 3

## 🏗️ Architecture & Technologies

### Architecture Pattern
- **Clean Architecture** with MVVM pattern
- **Separation of Concerns**: Data, Domain, and Presentation layers
- **Dependency Injection**: Dagger Hilt for dependency management

### Technologies Used
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room Persistence Library
- **Dependency Injection**: Dagger Hilt
- **Asynchronous Programming**: Kotlin Coroutines
- **Testing**: JUnit, MockK, Coroutines Test
- **Material Design**: Material 3 components

### Project Structure
```
app/src/main/java/com/example/posterr/
├── data/                    # Data layer
│   ├── dao/                # Room DAOs
│   ├── datasource/         # Data sources
│   ├── entity/             # Database entities
│   ├── repository/         # Repository implementations
│   ├── DatabaseSeeder.kt   # Initial data population
│   └── PosterrDatabase.kt  # Room database configuration
├── domain/                 # Domain layer
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── useCase/            # Business logic use cases
├── presentation/           # Presentation layer
│   ├── common/             # Shared UI components
│   ├── home/               # Home screen
│   └── profile/            # Profile screen
└── di/                     # Dependency injection modules
```

## 🚀 Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (API Level 24)
- Java 11 or later

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd posterr
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project folder and select it

3. **Sync and Build**
   - Wait for Gradle sync to complete
   - Build the project (Build → Make Project)

4. **Run the Application**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon)
   - Select your device/emulator and click "OK"

### Build Configuration
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36
- **Java Version**: 11

## 📋 Features Implementation

### ✅ Core Features
- [x] **Home Screen**: Feed of all posts with navigation
- [x] **Profile Screen**: User information and personal posts
- [x] **Original Posts**: Create new text posts (max 777 characters)
- [x] **Reposts**: Share other users' posts
- [x] **Quote Posts**: Repost with additional commentary
- [x] **Character Counter**: Real-time character count display
- [x] **Daily Limit**: Maximum 5 posts per day per user
- [x] **Data Persistence**: Local database storage
- [x] **User Management**: Pre-defined users with hard-coded default

### ✅ Validation & Constraints
- [x] **Character Limit**: Maximum 777 characters per post
- [x] **Daily Post Limit**: 5 posts per day (including reposts and quotes)
- [x] **Username Validation**: Alphanumeric characters only, max 14 characters
- [x] **Content Validation**: Empty/blank content prevention
- [x] **Real-time Feedback**: Character counter and limit indicators

### ✅ UI/UX Features
- [x] **Material 3 Design**: Modern, accessible interface
- [x] **Responsive Layout**: Adapts to different screen sizes
- [x] **Loading States**: Progress indicators during operations
- [x] **Error Handling**: User-friendly error messages
- [x] **Date Formatting**: US-style date format ("March 25, 2021")
- [x] **Post Statistics**: User post counts by type

## 👥 Pre-defined Users

The application comes with 4 pre-defined users:

1. **jardimtech** (Default logged-in user)
   - Join Date: March 25, 2021
   - Status: Logged in

2. **androiddev**
   - Join Date: March 25, 2021
   - Status: Not logged in

3. **kotlinlover**
   - Join Date: March 25, 2021
   - Status: Not logged in

4. **composefan**
   - Join Date: March 25, 2021
   - Status: Not logged in

**Note**: All usernames are alphanumeric and under 14 characters as per requirements.

## 🧪 Testing

### Running Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "HomeViewModelTest"

# Run tests with coverage
./gradlew testDebugUnitTest --info
```

### Test Coverage
- **Total Test Files**: 13
- **Total Tests**: 149
- **Coverage Areas**:
  - Data Layer (DataSources, Repositories, Database)
  - Domain Layer (Use Cases, Models)
  - Presentation Layer (ViewModels, UI Components)
  - Extensions and Utilities

### Test Architecture
- **Unit Tests**: JUnit 4
- **Mocking**: MockK
- **Coroutines Testing**: Coroutines Test
- **Test Organization**: Mirrors production package structure

## 🎯 Technical Decisions

### Architecture Choices
1. **Clean Architecture**: Ensures separation of concerns and testability
2. **MVVM Pattern**: Provides reactive UI updates and lifecycle management
3. **Repository Pattern**: Abstracts data sources and enables easy testing
4. **Use Cases**: Encapsulates business logic and makes it reusable

### Technology Choices
1. **Jetpack Compose**: Modern declarative UI framework
2. **Room Database**: Robust local data persistence
3. **Dagger Hilt**: Simplified dependency injection
4. **Kotlin Coroutines**: Efficient asynchronous programming

### Data Management
1. **Local-First Approach**: All data stored locally for demo purposes
2. **Room Database**: SQLite abstraction with compile-time verification
3. **Entity Relationships**: Proper foreign key relationships for posts
4. **Data Seeding**: Automatic population of sample data

## 🎬 Demo

**[Video Demo Link - To be added]**

The demo video showcases:
- Home screen with post feed
- Creating original posts with character counter
- Reposting functionality
- Quote posting with commentary
- Daily post limit enforcement
- Profile screen with user statistics
- Data persistence across app restarts
- Error handling and validation

## 📊 Critique

### What I Would Improve with More Time

1. **User Experience Enhancements**
   - Add pull-to-refresh functionality
   - Implement search and filtering capabilities
   - Add post editing capabilities
   - Improve error messages and user feedback

2. **Technical Improvements**
   - Add comprehensive error handling
   - Implement proper logging and analytics
   - Add unit tests for edge cases
   - Implement UI tests with Espresso

### Scaling Strategy for Thousands of Users

1. **Data Management**
   - Implement proper pagination (20-50 posts per page)
   - Add search and filtering capabilities
   - Implement data synchronization strategies
   - Use background jobs for data updates


2. **User Experience**
   - Add infinite scrolling
   - Implement proper loading states
   - Implement proper error recovery