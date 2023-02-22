package ve.com.mariomendoza.waifupaper.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Post(var autor: String? = null, @Ignore var etiquetas: String? = null,
                var imagenSD: String? = null, var imagenHD: String? = null, var fecha_subida: String? = null,
                var isLiked: Boolean? = false) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    ) {
        id = parcel.readInt()
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "autor" to autor,
            "etiquetas" to etiquetas,
            "imagenSD" to imagenSD,
            "imagenHD" to imagenHD,
            "fecha_subida" to fecha_subida,
            "isLiked" to isLiked
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(autor)
        parcel.writeString(etiquetas)
        parcel.writeString(imagenSD)
        parcel.writeString(imagenHD)
        parcel.writeString(fecha_subida)
        parcel.writeValue(isLiked)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}