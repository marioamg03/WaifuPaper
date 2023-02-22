package ve.com.mariomendoza.waifupaper.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.like.LikeButton
import com.like.OnLikeListener
import me.gujun.android.taggroup.TagGroup
import ve.com.mariomendoza.waifupaper.BuildConfig
import ve.com.mariomendoza.waifupaper.R
import ve.com.mariomendoza.waifupaper.models.Post


class WaifusHomeAdapter(private val mContext: Context,
                        private val mFavorites: Boolean,
                        private val onAddToFavoritesListener: OnAddToFavoritesListener,
                        private val onDeleteToFavoriteListener: OnDeleteToFavoriteListener,
                        private val onClickListener: OnClickListener
                        ) : RecyclerView.Adapter<WaifusHomeAdapter.ViewHolder>() {

    private var waifusList: List<Post>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_waifu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post:Post = waifusList!![position]
        holder.bind(post, mContext, mFavorites, onAddToFavoritesListener, onDeleteToFavoriteListener, onClickListener)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return if (waifusList != null) {
            waifusList!!.size
        } else {
            0
        }
    }

    fun setData(newData:List<Post>?) {
        waifusList = newData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imagePlace:ImageView = itemView.findViewById(R.id.imageView)
        private val likeButton: LikeButton = itemView.findViewById(R.id.like_button)

        fun bind(
            post: Post,
            mContext: Context,
            mFavorites: Boolean,
            onAddToFavoritesListener: OnAddToFavoritesListener,
            onDeleteToFavoriteListener: OnDeleteToFavoriteListener,
            onClickListener: OnClickListener
        ) {

            if (mFavorites) {
                likeButton.isLiked = mFavorites
            } else {
                likeButton.isLiked = post.isLiked!!
            }

            likeButton.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton) {
                    onAddToFavoritesListener.onClick(post, adapterPosition)
                }
                override fun unLiked(likeButton: LikeButton) {
                    onDeleteToFavoriteListener.onClick(post, adapterPosition)
                }
            })

            val url = BuildConfig.BASE_URL_IMG + post.imagenHD

            Glide.with(mContext)
                .load(url)
                .override(800,800)
                .transform(RoundedCorners(45))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imagePlace)

            imagePlace.transitionName = url

            itemView.setOnClickListener {
                onClickListener.onClick(post, imagePlace)
            }
        }
    }

    class OnClickListener(val clickListener: (Post, ImageView) -> Unit) {
        fun onClick(
            post: Post,
            imagePlace: ImageView,
        ) = clickListener(post, imagePlace)
    }

    class OnAddToFavoritesListener(val clickListener: (Post, Int) -> Unit) {
        fun onClick(
            post: Post,
            position: Int,
        ) = clickListener(post, position)
    }

    class OnDeleteToFavoriteListener(val clickListener: (Post, Int) -> Unit) {
        fun onClick(
            post: Post,
            position: Int,
        ) = clickListener(post, position)
    }


}