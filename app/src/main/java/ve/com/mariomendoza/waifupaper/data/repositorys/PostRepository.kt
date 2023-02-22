package ve.com.mariomendoza.waifupaper.data.repositorys

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ve.com.mariomendoza.waifupaper.BuildConfig
import ve.com.mariomendoza.waifupaper.data.dao.PostDao
import ve.com.mariomendoza.waifupaper.data.remote.ApiService
import ve.com.mariomendoza.waifupaper.models.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepository(private val postDao: PostDao) {

    fun addFavoritePost(post: Post) {
        postDao.insertFavoritePost(post)
    }

    fun deleteFavoritePost(id: Int) {
        postDao.deleteFavoritePost(id)
    }

    fun getFavoritesPosts(): LiveData<List<Post>> {
        return postDao.getAllFavorites()
    }

    fun getAllFavoritesEdit(): List<Post> {
        return postDao.getAllFavoritesEdit()
    }

    private fun getRetrofit(): Retrofit {

        val clientSetup = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES) // write timeout
            .readTimeout(1, TimeUnit.MINUTES) // read timeout
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientSetup)
            .build()
    }

    suspend fun sendData(author: RequestBody, tags: RequestBody, imageHDMultipart: MultipartBody.Part, imageSDMultipart: MultipartBody.Part):Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val call: Response<Void> = getRetrofit().create(ApiService::class.java).uploadAttachment(author, tags, imageHDMultipart, imageSDMultipart)
                call.isSuccessful
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> false
                    is HttpException -> false
                    else -> false
                }
            }
        }
    }

    suspend fun getAllPosts():List<Post> {
        return withContext(Dispatchers.IO) {
            try {
                val call: Response<List<Post>> = getRetrofit().create(ApiService::class.java).getPosts()
                call.body() ?: emptyList()
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> emptyList()
                    is HttpException -> emptyList()
                    else -> emptyList()
                }
            }
        }
    }

}