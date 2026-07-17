package com.example.aplicacionlistadecompras.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val BACKEND_URL_KEY = stringPreferencesKey("backend_url")

    val backendUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[BACKEND_URL_KEY]
    }

    suspend fun saveBackendUrl(url: String) {
        // Normaliza para que siempre acabe en "/", Retrofit lo exige
        val normalized = if (url.endsWith("/")) url else "$url/"
        context.dataStore.edit { prefs ->
            prefs[BACKEND_URL_KEY] = normalized
        }
    }

    suspend fun clearBackendUrl() {
        context.dataStore.edit { prefs ->
            prefs.remove(BACKEND_URL_KEY)
        }
    }
}

