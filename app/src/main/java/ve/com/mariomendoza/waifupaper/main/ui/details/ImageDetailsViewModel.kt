package ve.com.mariomendoza.waifupaper.main.ui.details

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ve.com.mariomendoza.waifupaper.data.DataStorePreferences
import ve.com.mariomendoza.waifupaper.data.SafeDatabase
import ve.com.mariomendoza.waifupaper.data.repositorys.PostRepository
import ve.com.mariomendoza.waifupaper.models.Post

class ImageDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository: PostRepository
    private val dataStorePreferences: DataStorePreferences
    var sharePreferences:MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        val database = SafeDatabase.getInstance(application)
        val postDao = database.postDao()
        postRepository = PostRepository(postDao)
        dataStorePreferences = DataStorePreferences()
        getSharePreferences()
    }

    private fun getSharePreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreferences.getValueBoolean(getApplication(), "share_preferences"). collect {
                withContext(Dispatchers.Main) {
                    sharePreferences.value = !it
                }
            }
        }
    }

}