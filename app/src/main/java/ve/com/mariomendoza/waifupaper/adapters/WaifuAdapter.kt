package ve.com.mariomendoza.waifupaper.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import me.gujun.android.taggroup.TagGroup
import ve.com.mariomendoza.waifupaper.R
import ve.com.mariomendoza.waifupaper.models.Post


class WaifusHomeAdapter(private val mContext: Context) : RecyclerView.Adapter<WaifusHomeAdapter.ViewHolder>() {

    private var waifusList: List<Post>? = null
    private val lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_waifu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post:Post = waifusList!![position]

        holder.author.text = "Autor: ${post.author}"

        val storageRef = FirebaseStorage.getInstance().reference

        val dateRef = storageRef.child("myImages/${post.urlImageHD}")
        dateRef.downloadUrl.addOnSuccessListener {
            //do something with downloadurl
            Picasso.get()
                .load(it)
                .fit()
                .centerCrop()
                .into(holder.imagePlace)
            Toast.makeText(mContext,"MAMA",Toast.LENGTH_SHORT).show()
        }



    }

    override fun getItemCount(): Int {
        return if (waifusList != null) {
            waifusList!!.size
        } else {
            0
        }
    }

    fun setData(newData: List<Post>?) {
        waifusList = newData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagePlace:ImageView = itemView.findViewById(R.id.imageView)
        val author:TextView = itemView.findViewById(R.id.textView3)
        val tagGroup:TagGroup = itemView.findViewById(R.id.tag_group)
    }
}