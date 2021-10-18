package club.electro.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
import club.electro.databinding.PostItemBinding
import club.electro.dto.Post
import com.bumptech.glide.Glide


interface PostInteractionListener {
    fun onClick(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: PostInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: PostItemBinding,
    private val onInteractionListener: PostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            //val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
            val date = java.util.Date(post.published * 1000)

            authorName.text = post.authorName
            published.text = sdf.format(date).toString()

            // TODO Вставить изображения в текст поста
            //val imageGetter = HtmlImageGetter(scope, resources, glide, content)
            //val imageGetter = ImageGetter(, content)

            content.setText(HtmlCompat.fromHtml(post.content, HtmlCompat.FROM_HTML_MODE_LEGACY));
            content.setMovementMethod(LinkMovementMethod.getInstance());

            if (!post.authorAvatar.isEmpty()) {
                Glide.with(authorAvatar.context)
                    .load(post.authorAvatar)
                    .circleCrop()
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(authorAvatar)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}