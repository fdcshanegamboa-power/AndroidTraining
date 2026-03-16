package com.learn.androidtraining.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Top-level extension — only one DataStore instance ever created
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "photo_prefs")

class DataStoreManager private constructor(private val context: Context) {

    suspend fun saveLastPhotoUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_PHOTO_KEY] = url
        }
    }

    suspend fun getLastPhotoUrl(): String? {
        return context.dataStore.data.first()[LAST_PHOTO_KEY]
    }

    companion object {
        private val LAST_PHOTO_KEY = stringPreferencesKey("last_photo_url")

        @Volatile
        private var instance: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager =
            instance ?: synchronized(this) {
                instance ?: DataStoreManager(context.applicationContext).also { instance = it }
            }
    }
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}