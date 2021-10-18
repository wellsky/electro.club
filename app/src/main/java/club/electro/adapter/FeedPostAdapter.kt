package club.electro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
import club.electro.databinding.FeedPostItemBinding
import club.electro.dto.FeedPost
import com.bumptech.glide.Glide

interface OnFeedPostInteractionListener {
    fun onClick(feedPost: FeedPost) {}
}

class FeedPostAdapter (
    private val onInteractionListener: OnFeedPostInteractionListener,
) : ListAdapter<FeedPost, FeedPostViewHolder>(FeedPostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
        val binding = FeedPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedPostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class FeedPostViewHolder(
    private val binding: FeedPostItemBinding,
    private val onInteractionListener: OnFeedPostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(feedPost: FeedPost) {
        binding.apply {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
            val date = java.util.Date(feedPost.published * 1000)

            feedPostAuthor.text = feedPost.authorName
            feedPostText.text = feedPost.text
            published.text = sdf.format(date).toString()

            if (!feedPost.authorAvatar.isEmpty()) {
                Glide.with(feedPostAvatar.context)
                    .load(feedPost.authorAvatar)
                    .circleCrop()
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(feedPostAvatar)
            }

            if (!feedPost.image.isEmpty()) {
                Glide.with(feedPostImage.context)
                    .load(feedPost.image)
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(feedPostImage)
            }



            feedPostImage.setOnClickListener {
                //onInteractionListener.onLike(post, like.isChecked) // В этой строке isChecked уже изменился после нажатия!!
            }
        }
    }
}

class FeedPostDiffCallback : DiffUtil.ItemCallback<FeedPost>() {
    override fun areItemsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean {
        return oldItem == newItem
    }
}