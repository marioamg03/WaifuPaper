package ve.com.mariomendoza.waifupaper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ve.com.mariomendoza.waifupaper.data.dao.PostDao
import ve.com.mariomendoza.waifupaper.models.Post

@Database(entities = [Post::class], version = 2, exportSchema = false)
abstract class SafeDatabase : RoomDatabase() {

    abstract fun postDao():PostDao

    companion object {
        @Volatile
        private var INSTANCE : SafeDatabase ?= null

        fun getInstance(context: Context): SafeDatabase {

            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeDatabase::class.java,
                    "database.waifupaper"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }

        }

    }

}