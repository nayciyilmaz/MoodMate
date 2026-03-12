![Logo](https://github.com/nayciyilmaz/MoodMate/blob/main/moodmate_collage.png?raw=true)

👉 **[Click here to view Backend code](https://github.com/nayciyilmaz/MoodMateBackend)**

---

A modern Android mood tracking application built with Jetpack Compose. Users can log daily moods, track emotional patterns, and receive AI-generated mental health advice — all with full offline support.

## Features

- **AI-Powered Advice:** Personalized mental health advice based on recent mood entries
- **Daily Mood Tracking:** Log moods with emoji, score (1-10), notes, and date/time
- **Mood History & Search:** Browse, search, and filter past entries by text and date
- **Offline-First Architecture:** Full CRUD works offline with automatic background sync
- **Smart Notifications:** Daily morning and evening reminders via WorkManager
- **Profile & Settings:** Change password, toggle notifications, switch languages
- **Multi-Language:** Turkish, English, Spanish, and Italian
- **Material 3 Design:** Modern UI built entirely with Jetpack Compose

## Tech Stack

- **Jetpack Compose** - Declarative UI toolkit, no XML layouts
- **Material 3** - Latest Material Design components
- **Navigation Compose** - 9 screens with argument passing
- **Dagger Hilt** - Dependency injection
- **Room** - Local SQLite database
- **Retrofit + OkHttp** - Network layer with auth interceptor
- **DataStore Preferences** - Token and user info persistence
- **WorkManager** - Background sync and scheduled notifications
- **Lottie Compose** - Splash screen animation
- **Timber** - Structured logging
- **Gson** - JSON serialization

## Architecture

- **MVVM Pattern** - ViewModel with StateFlow for reactive UI
- **Repository Pattern** - AuthRepository, MoodRepository, AdviceRepository
- **Offline-First** - Room DB as source of truth, background server sync
- **Resource Sealed Class** - Unified success/error/loading across all layers
- **Coroutines & Flow** - Asynchronous programming and reactive streams

## Sync System

- **SyncStatus** - SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE
- **SyncManager** - Processes pending operations and merges server data
- **SyncScheduler** - WorkManager with network constraint and exponential backoff
- **NetworkMonitor** - ConnectivityManager callback with Flow-based state
- **Conflict Resolution** - Last-write-wins using timestamp comparison
- **SyncState UI** - Idle, PendingOffline, Syncing, Synced, SyncFailed

## Design & UI

- **Modern Compose UI:** Fully declarative, no XML
- **Color Scheme:** Blue palette with light background tones
- **Smooth Animations:** Spring bounce, color transitions, Lottie splash
- **13 Reusable Components:** EditScaffold, EditDatePicker, EditTimePicker, MoodCard, etc.
- **Transparent System Bars:** Light status and navigation bars
- **Portrait Locked:** Consistent mobile experience

## Notification System

- 2 daily notifications via WorkManager PeriodicWorkRequest
- Morning (10:00) and evening (22:00) reminders
- Android 13+ POST_NOTIFICATIONS permission handling
- SharedPreferences toggle for enable/disable
- HiltWorker integration for dependency injection

## Testing

- **JUnit 4** - Unit testing framework
- **MockK** - Mocking library for repositories and ViewModels
- **Kotlinx Coroutines Test** - Coroutine testing with UnconfinedTestDispatcher
- **~70+ Unit Tests** covering repositories, sync manager, and viewmodels
