package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.databinding.ItemFeedPostBinding
import club.electro.dto.FeedPost
import club.electro.utils.load
import club.electro.utils.loadCircleCrop


interface OnFeedPostInteractionListener {
    fun onClick(feedPost: FeedPost) {}
}

class FeedPostAdapter (
    private val onInteractionListener: OnFeedPostInteractionListener,
) : ListAdapter<FeedPost, FeedPostViewHolder>(FeedPostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
        val binding = ItemFeedPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedPostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class FeedPostViewHolder(
    private val binding: ItemFeedPostBinding,
    private val onInteractionListener: OnFeedPostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
    }

    fun bind(feedPost: FeedPost) {
        binding.apply {
            val date = java.util.Date(feedPost.published * 1000)

            channelName.text = feedPost.channelName
            content.text = feedPost.content
            published.text = sdf.format(date).toString()

            partPostStats.views.text = feedPost.views.toString()
            partPostStats.comments.text = feedPost.comments.toString()

            if (!feedPost.channelAvatar.isEmpty()) {
                channelAvatar.isVisible = true
                channelAvatar.loadCircleCrop(feedPost.channelAvatar)
            } else {
                channelAvatar.isVisible = false
            }

            if (!feedPost.image.isEmpty()) {
                titleImage.isVisible = true
                titleImage.load(feedPost.image)
            } else {
                titleImage.isVisible = false
            }


            root.setOnClickListener {
                onInteractionListener.onClick(feedPost) // В этой строке isChecked уже изменился после нажатия!!
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