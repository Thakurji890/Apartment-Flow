package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "apartment_prefs")

class PreferenceManager(private val context: Context) {
    companion object {
        private val APARTMENT_ID_KEY = stringPreferencesKey("apartment_id")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val CURRENCY_KEY = stringPreferencesKey("base_currency")
        
        @Volatile
        private var INSTANCE: PreferenceManager? = null
        
        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val apartmentId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APARTMENT_ID_KEY]
    }

    suspend fun saveApartmentId(apartmentId: String?) {
        context.dataStore.edit { preferences ->
            if (apartmentId != null) {
                preferences[APARTMENT_ID_KEY] = apartmentId
            } else {
                preferences.remove(APARTMENT_ID_KEY)
            }
        }
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = lang
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system"
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    val currency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: "USD"
    }

    suspend fun saveCurrency(curr: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = curr
        }
    }
}
