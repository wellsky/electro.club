package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.databinding.ItemAttachmentBinding
import club.electro.dto.PostAttachment
import club.electro.utils.load

interface PostAttachmentInteractionListener {
    fun onRemoveClick(attachment: PostAttachment) {}
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
            attachmentName.text = attachment.name

            val statusText = when(attachment.status) {
                PostAttachment.STATUS_CREATED -> "New"
                PostAttachment.STATUS_READY_TO_UPLOAD -> "ready to upload"
                PostAttachment.STATUS_COMPRESSING -> "compressing"
                PostAttachment.STATUS_UPLOADING -> "uploading"
                PostAttachment.STATUS_UPLOADED -> "uploaded"

                PostAttachment.STATUS_ERROR_NOT_FOUND -> "error: file not found"
                PostAttachment.STATUS_ERROR_COMPRESSING -> "error: compression failed"
                PostAttachment.STATUS_ERROR_UPLOADING -> "error: uploading failed"

                else -> { "Unknown" }
            }

            attachmentStatus.text = "Status: " + statusText

            attachmentImage.load(attachment.localPath)

            attachmentRemove.setOnClickListener {
                onInteractionListener.onRemoveClick(attachment)
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