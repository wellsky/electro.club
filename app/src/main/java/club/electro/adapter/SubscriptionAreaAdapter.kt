package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.databinding.SubscriptionItemBinding
import club.electro.dto.SubscriptionArea
import club.electro.utils.areaLastActivityTime
import club.electro.utils.htmlToText
import club.electro.utils.loadCircleCrop

interface SubscriptionAreaInteractionListener {
    fun onClick(area: SubscriptionArea) {}
}

class SubscriptionAreaAdapter (
    private val onInteractionListener: SubscriptionAreaInteractionListener,
) : ListAdapter<SubscriptionArea, SubscriptionAreaViewHolder>(SubscriptionAreaDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionAreaViewHolder {
        val binding = SubscriptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionAreaViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: SubscriptionAreaViewHolder, position: Int) {
        val area = getItem(position)
        holder.bind(area)
    }
}

class SubscriptionAreaViewHolder(
    private val binding: SubscriptionItemBinding,
    private val onInteractionListener: SubscriptionAreaInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(area: SubscriptionArea) {
        binding.apply {
            areaName.text = area.name
            areaLastMessage.text = htmlToText(area.last_text)

            if (area.last_name.isNotBlank()) {
                areaLastName.text = area.last_name + ": "
            } else {
                areaLastName.text = ""
            }

            areaLastMessageTime.text = areaLastActivityTime(area.last_time, this.root.context)

            areaUnreadMessagesCount.isVisible = (area.count > 0)
            areaUnreadMessagesCount.text = area.count.toString()

            if (!area.image.isEmpty()) {
                areaImage.loadCircleCrop(area.image)
            }

            root.setOnClickListener {
                onInteractionListener.onClick(area)
            }
        }
    }
}



class SubscriptionAreaDiffCallback : DiffUtil.ItemCallback<SubscriptionArea>() {
    override fun areItemsTheSame(oldItem: SubscriptionArea, newItem: SubscriptionArea): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SubscriptionArea, newItem: SubscriptionArea): Boolean {
        return oldItem == newItem
    }
}