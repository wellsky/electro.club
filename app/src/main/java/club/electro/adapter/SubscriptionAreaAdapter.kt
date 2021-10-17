package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
import club.electro.databinding.SubscriptionItemBinding
import club.electro.dto.SubscriptionArea
import com.bumptech.glide.Glide

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
            areaLastMessage.text = area.last_text

            if (!area.image.isEmpty()) {
                Glide.with(areaImage.context)
                    .load(area.image)
                    .circleCrop()
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(areaImage)
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