package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
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
            val context = binding.root.context
            attachmentName.text = attachment.name

            val statusText = when(attachment.status) {
                PostAttachment.STATUS_CREATED -> context.getString(R.string.upload_status_new)
                PostAttachment.STATUS_READY_TO_UPLOAD -> context.getString(R.string.upload_status_ready)
                PostAttachment.STATUS_COMPRESSING -> context.getString(R.string.upload_status_compressing)
                PostAttachment.STATUS_UPLOADING -> context.getString(R.string.upload_status_uploading)
                PostAttachment.STATUS_UPLOADED -> context.getString(R.string.upload_status_uploaded)

                PostAttachment.STATUS_ERROR_NOT_FOUND -> context.getString(R.string.upload_status_not_found)
                PostAttachment.STATUS_ERROR_COMPRESSING -> context.getString(R.string.upload_status_compressiion_failed)
                PostAttachment.STATUS_ERROR_UPLOADING -> context.getString(R.string.upload_status_failed)

                else -> { context.getString(R.string.upload_status_unknown) }
            }

            attachmentStatus.text = context.getString(R.string.upload_status) + statusText

            attachment.localPath?.let {
                attachmentImage.load(it)
            } ?: attachment.previewUrl?.let {
                attachmentImage.load(it)
            }

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