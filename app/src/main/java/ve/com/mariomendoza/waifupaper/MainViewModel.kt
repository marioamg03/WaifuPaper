package ve.com.mariomendoza.waifupaper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ve.com.mariomendoza.waifupaper.data.SafeDatabase
import ve.com.mariomendoza.waifupaper.data.repositorys.PostRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository:PostRepository
    var loading: MutableLiveData<Boolean> = MutableLiveData(null)
    var alert: MutableLiveData<String> = MutableLiveData(null)
    var clean: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        val database = SafeDatabase.getInstance(application)
        val postDao = database.postDao()
        postRepository = PostRepository(postDao)
    }

    fun sendData(author: RequestBody, tags: RequestBody, imageHDMultipart: MultipartBody.Part, imageSDMultipart: MultipartBody.Part) {
        loading.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            val response =  postRepository.sendData(author, tags, imageHDMultipart, imageSDMultipart)

            if (response) {
                loading.postValue(false)
                clean.postValue(true)
            } else {
                loading.postValue(false)
                alert.postValue("¡Ocurrio un Error al subir la imagen!")
            }

        }

    }


}