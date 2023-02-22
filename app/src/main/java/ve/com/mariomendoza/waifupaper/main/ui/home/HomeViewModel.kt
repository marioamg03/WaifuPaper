package ve.com.mariomendoza.waifupaper.main.ui.home

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ve.com.mariomendoza.waifupaper.data.DataStorePreferences
import ve.com.mariomendoza.waifupaper.data.SafeDatabase
import ve.com.mariomendoza.waifupaper.data.repositorys.PostRepository
import ve.com.mariomendoza.waifupaper.models.Post

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository: PostRepository
    private val dataStorePreferences: DataStorePreferences
    var spanCount:MutableLiveData<Int> = MutableLiveData(2)

    var mListPost = MutableLiveData<List<Post>>()

    init {
        val database = SafeDatabase.getInstance(application)
        val postDao = database.postDao()
        postRepository = PostRepository(postDao)
        dataStorePreferences = DataStorePreferences()
        getColumns()
        getAllPost()
    }


    fun getAllPost() {
        viewModelScope.launch(Dispatchers.IO) {
            val allPosts =  postRepository.getAllPosts()
            val likedPost =  postRepository.getAllFavoritesEdit()

            for (post in allPosts) {
                val exist = likedPost.find { actor -> post.id == actor.id }
                if (exist != null) {
                    post.isLiked = true
                }
            }

            mListPost.postValue(allPosts)
        }
    }

    private fun getColumns() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStorePreferences.getValueBoolean(getApplication(), "columns"). collect {
                withContext(Dispatchers.Main) {
                    spanCount.value = if (it) {
                        3
                    } else {
                        2
                    }
                }
            }
        }
    }

    fun addPostToFavorite(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.addFavoritePost(post)
        }
    }

    fun deletePostToFavorite(postId:Int) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.deleteFavoritePost(postId)
        }
    }
}