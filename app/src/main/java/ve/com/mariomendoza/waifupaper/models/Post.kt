package ve.com.mariomendoza.waifupaper.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(val author: String? = null, val tags: List<String>? = null,
                val urlImageSD: String? = null, val urlImageHD: String? = null,
                val urlLivePaper: String? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "author" to author,
            "tags" to tags,
            "urlImageHD" to urlImageHD,
            "urlImageSD" to urlImageSD,
            "urlLivePaper" to urlLivePaper
        )
    }
}