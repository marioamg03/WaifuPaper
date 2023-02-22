package ve.com.mariomendoza.waifupaper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "SETTINGS_PREFERENCES")

class DataStorePreferences {

    suspend fun saveValueString (context: Context, key:String, value:String) {
        context.dataStore.edit {
            it[stringPreferencesKey(key)] = value
        }
    }

    suspend fun saveValueBoolean (context: Context, key:String, value:Boolean) {
        context.dataStore.edit {
            it[booleanPreferencesKey(key)] = value
        }
    }


    fun getValueString (context: Context, key:String) = context.dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(key)].orEmpty()
    }

    fun getValueBoolean (context: Context, key:String) = context.dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(key)] ?: false
    }


}