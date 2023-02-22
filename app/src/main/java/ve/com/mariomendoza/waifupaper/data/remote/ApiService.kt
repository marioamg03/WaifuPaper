package ve.com.mariomendoza.waifupaper.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ve.com.mariomendoza.waifupaper.models.Post

interface ApiService {

    @Multipart
    @POST("api/upload_waifu")
    suspend fun uploadAttachment(@Part("author") author: RequestBody, @Part("tags") tags: RequestBody,
                                 @Part urlImageHD: MultipartBody.Part, @Part urlImageSD: MultipartBody.Part): Response<Void>

    @GET("api/get_all_waifus")
    suspend fun getPosts(): Response<List<Post>>

}