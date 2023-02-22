package ve.com.mariomendoza.waifupaper.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ve.com.mariomendoza.waifupaper.models.Post

@Dao
interface PostDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoritePost(vararg post: Post)

    @Query("DELETE FROM post WHERE id=:id")
    fun deleteFavoritePost(id: Int)

    @Query("SELECT * FROM post")
    fun getAllFavorites(): LiveData<List<Post>>

    @Query("SELECT * FROM post")
    fun getAllFavoritesEdit(): List<Post>

}