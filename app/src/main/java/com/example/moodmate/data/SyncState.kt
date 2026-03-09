package com.example.moodmate.data

sealed class SyncState {
    object Idle : SyncState()
    data class PendingOffline(val count: Int) : SyncState()
    object Syncing : SyncState()
    data class Synced(val lastSyncTime: String) : SyncState()
    data class SyncFailed(val pendingCount: Int) : SyncState()
}