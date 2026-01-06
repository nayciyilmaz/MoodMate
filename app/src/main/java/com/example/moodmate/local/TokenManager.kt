package com.example.moodmate.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val IS_LOGGED_IN_KEY = stringPreferencesKey("is_logged_in")
    }

    suspend fun saveUser(userId: Int, email: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[IS_LOGGED_IN_KEY] = "true"
        }
    }

    val userId: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] == "true"
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}