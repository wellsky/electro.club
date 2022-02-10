package club.electro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import club.electro.R
import club.electro.databinding.ItemTransportListBinding
import club.electro.dto.TransportPreview
import club.electro.utils.load

interface TransportPreviewInteractionListener {
    fun onClick(item: TransportPreview)
}

class TransportPreviewAdapter (
    private val onInteractionListener: TransportPreviewInteractionListener
) : ListAdapter<TransportPreview, TransportPreviewViewHolder>(TransportPreviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransportPreviewViewHolder {
        val binding = ItemTransportListBinding.inflate(LayoutInflater.from(parent.context))
        return TransportPreviewViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: TransportPreviewViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

}

class TransportPreviewViewHolder(
    private val binding: ItemTransportListBinding,
    private val onInteractionListener: TransportPreviewInteractionListener
): RecyclerView.ViewHolder(binding.root) {

    fun bind(transport: TransportPreview) {
        binding.apply {
            transportName.text = transport.name

            usersValue.text = transport.users.toString()
            ratingValue.text = transport.rating.toString()

            transportText.text = ""
                .plus(transport.specs.weight)
                .plus(this.root.context.getString(R.string.measure_weight))
                .plus(", ")
                .plus(transport.specs.power)
                .plus(this.root.context.getString(R.string.measure_power))
                .plus(", ")
                .plus(transport.specs.batteryCapacityAh)
                .plus(this.root.context.getString(R.string.measure_battery_capacity))


            transport.image?.let {
                transportImage.load(it)
            }

            root.setOnClickListener {
                onInteractionListener.onClick(transport)
            }
        }
    }
}

class TransportPreviewDiffCallback: DiffUtil.ItemCallback<TransportPreview>() {
    override fun areItemsTheSame(oldItem: TransportPreview, newItem: TransportPreview): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TransportPreview, newItem: TransportPreview): Boolean {
        return oldItem == newItem
    }
}