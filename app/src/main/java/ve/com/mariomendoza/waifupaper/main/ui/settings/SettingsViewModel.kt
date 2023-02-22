package ve.com.mariomendoza.waifupaper.main.ui.settings

import android.app.Application
import android.widget.Switch
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.*
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ve.com.mariomendoza.waifupaper.data.DataStorePreferences
import ve.com.mariomendoza.waifupaper.data.SafeDatabase
import ve.com.mariomendoza.waifupaper.data.repositorys.PostRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository: PostRepository
    private val dataStorePreferences:DataStorePreferences

    init {
        val database = SafeDatabase.getInstance(application)
        val postDao = database.postDao()

        postRepository = PostRepository(postDao)
        dataStorePreferences = DataStorePreferences()
    }

    fun getColumns(switch: SwitchMaterial) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreferences.getValueBoolean(getApplication(), "columns").collect() {
                withContext(Dispatchers.Main) {
                    switch.isChecked = it
                }
            }
        }
    }

    fun setColumns(checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreferences.saveValueBoolean(getApplication(), "columns", checked)
        }
    }

    fun getSharePreferences(switch2: SwitchMaterial) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreferences.getValueBoolean(getApplication(), "share_preferences").collect() {
                withContext(Dispatchers.Main) {
                    switch2.isChecked = !it
                }
            }
        }
    }

    fun setSharePreferences(checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreferences.saveValueBoolean(getApplication(), "share_preferences", !checked)
        }
    }

}