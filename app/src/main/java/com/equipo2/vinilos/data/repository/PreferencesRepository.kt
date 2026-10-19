package com.equipo2.vinilos.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PreferencesRepository {
    val role: Flow<Role?>
    val themeMode: Flow<ThemeMode>
    suspend fun setRole(role: Role)
    suspend fun setThemeMode(mode: ThemeMode)
}

private val Context.dataStore by preferencesDataStore(name = "vinilos_prefs")

class DataStorePreferencesRepository(private val context: Context) : PreferencesRepository {

    override val role: Flow<Role?> = context.dataStore.data.map { prefs ->
        prefs[ROLE_KEY]?.let { name -> Role.entries.find { it.name == name } }
    }

    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.entries.find { it.name == prefs[THEME_KEY] } ?: ThemeMode.SYSTEM
    }

    override suspend fun setRole(role: Role) {
        context.dataStore.edit { it[ROLE_KEY] = role.name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_KEY] = mode.name }
    }

    private companion object {
        val ROLE_KEY = stringPreferencesKey("role")
        val THEME_KEY = stringPreferencesKey("theme_mode")
    }
}
