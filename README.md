![Logo](https://github.com/nayciyilmaz/MoodMate/blob/main/moodmate_collage.png?raw=true)

A modern Android mood tracking application with a Spring Boot backend, built with Jetpack Compose and powered by Google Gemini AI. Users can log their daily moods, track emotional patterns, and receive personalized AI-generated mental health advice — all with full offline support.

## Features

- **AI-Powered Mood Advice:** Generate personalized mental health advice based on recent mood entries using Google Gemini API
- **User Authentication:** Secure JWT-based login and registration with Spring Security and BCrypt
- **Daily Mood Tracking:** Log moods with emoji, score (1-10), notes, and date/time selection
- **Mood History & Search:** Browse, search, and filter past mood entries by text and date
- **Offline-First Architecture:** Full CRUD operations work offline with automatic background sync
- **Smart Notifications:** Daily morning (10:00) and evening (22:00) reminders via WorkManager
- **Profile & Settings:** Change password, toggle notifications, switch languages
- **Multi-Language Support:** Turkish, English, Spanish, and Italian (both frontend and backend)
- **Material 3 Design:** Modern UI built entirely with Jetpack Compose

## Frontend

- **Jetpack Compose** - Declarative UI toolkit, no XML layouts
- **Material 3** - Latest Material Design components
- **Navigation Compose** - Navigation between 9 screens with argument passing
- **Material Icons Extended** - Extended icon set
- **Lottie Compose** - Splash screen animation with character-by-character text reveal
- **AnimatedVisibility** - Expand/collapse animations for AI advice card
- **Spring & Tween Animations** - Bouncy mood selection and smooth color transitions

## Backend

- **Spring Boot** - REST API framework running on Java 17
- **Spring Security** - Stateless JWT authentication with BCrypt password hashing
- **Spring Data JPA / Hibernate** - ORM with PostgreSQL dialect
- **PostgreSQL** - Production database with HikariCP connection pooling
- **Google Gemini API** - AI advice generation with empathetic Turkish prompts
- **SpringDoc OpenAPI** - Swagger UI for API documentation
- **MapStruct** - DTO-Entity mapping with Lombok integration
- **Bean Validation** - Jakarta validation with internationalized error messages

## Architecture & DI

- **Dagger Hilt** - Dependency injection (Android)
- **Spring IoC** - Dependency injection (Backend)
- **MVVM Pattern** - Model-View-ViewModel architecture on Android
- **Layered Architecture** - Controller → Service → Repository → Entity on backend
- **Repository Pattern** - AuthRepository, MoodRepository, AdviceRepository
- **Offline-First Sync** - Local Room DB as source of truth with background server sync
- **Coroutines & Flow** - Reactive state management with StateFlow
- **Resource Sealed Class** - Unified success/error/loading pattern across all layers

## Data & Storage

- **Room** - Local SQLite database with MoodEntity and AdviceLocalEntity
- **DataStore Preferences** - Token, user info, and login state persistence
- **SharedPreferences** - Notification and language preferences
- **Retrofit + OkHttp** - Network layer with auth interceptor and logging
- **Gson** - JSON serialization with @SerializedName mapping

## Sync System

- **SyncStatus Enum** - SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE
- **SyncManager** - Processes pending operations and merges server data
- **SyncScheduler** - WorkManager with network constraint and exponential backoff
- **SyncWorker** - HiltWorker with 3 retry attempts
- **NetworkMonitor** - ConnectivityManager callback with Flow-based online state
- **Conflict Resolution** - Last-write-wins strategy using timestamp comparison
- **SyncState UI** - 5 states: Idle, PendingOffline, Syncing, Synced, SyncFailed

## Testing

- **JUnit 4 & 5** - Unit testing framework (Android & Backend)
- **MockK** - Mocking library for Android layers
- **Mockito** - Mocking library for Spring Boot layers
- **Kotlinx Coroutines Test** - Coroutine testing with UnconfinedTestDispatcher
- **MockMvc** - Spring MVC integration tests with @WithMockUser
- **H2 Database** - In-memory database for backend tests
- **~70+ Android Unit Tests** covering repositories, sync manager, and viewmodels
- **~40+ Backend Unit Tests** covering services, controllers, and repositories

## Design & UI

- **Modern Compose UI:** No XML layouts, fully declarative
- **Material 3 Design:** Latest Material Design guidelines
- **Responsive Layout:** Portrait orientation locked
- **Color Scheme:** Blue palette with light background tones
- **Smooth Animations:** Spring bounce on mood selection, animated color transitions, Lottie splash
- **Custom Components:** EditScaffold, EditBottomBar, EditTopBar, EditDatePicker, EditTimePicker, MoodCard, ValidationErrorText
- **Transparent System Bars:** Light status and navigation bars
- **13 Reusable Components:** Consistent design language across all screens

## Google Gemini API

- Generates personalized mood advice based on last 3 days of entries
- Considers emoji, score (1-10), date, and user notes
- Empathetic Turkish prompt with mental health counselor persona
- Identifies positive and negative emotional patterns
- Suggests professional help when negative emotions are intense
- Returns 2-3 paragraph warm, motivational responses

## Notification System

- 2 daily scheduled notifications via WorkManager PeriodicWorkRequest
- Morning reminder at 10:00: "Günün nasıl geçiyor"
- Evening reminder at 22:00: "Günün notunu almayı unutma"
- NotificationChannel with IMPORTANCE_DEFAULT
- Android 13+ POST_NOTIFICATIONS permission handling
- SCHEDULE_EXACT_ALARM permission for precise timing
- SharedPreferences toggle for enable/disable
- HiltWorker integration for dependency injection
