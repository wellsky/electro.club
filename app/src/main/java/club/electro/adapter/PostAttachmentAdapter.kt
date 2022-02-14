package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.databinding.ItemAttachmentBinding
import club.electro.dto.PostAttachment

interface PostAttachmentInteractionListener {
    fun onClick(attachment: PostAttachment) {}
}

class PostAttachmentAdapter (
    private val onInteractionListener: PostAttachmentInteractionListener,
) : ListAdapter<PostAttachment, PostAttachmentViewHolder>(PostAttachmentDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAttachmentViewHolder {
        val binding = ItemAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostAttachmentViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostAttachmentViewHolder, position: Int) {
        val attachment = getItem(position)
        holder.bind(attachment)
    }
}

class PostAttachmentViewHolder(
    private val binding: ItemAttachmentBinding,
    private val onInteractionListener: PostAttachmentInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(attachment: PostAttachment) {
        binding.apply {
            attachmentPath.text = attachment.localFile

            root.setOnClickListener {
                onInteractionListener.onClick(attachment)
            }
        }
    }
}



class PostAttachmentDiffCallback : DiffUtil.ItemCallback<PostAttachment>() {
    override fun areItemsTheSame(oldItem: PostAttachment, newItem: PostAttachment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostAttachment, newItem: PostAttachment): Boolean {
        return oldItem == newItem
    }
}