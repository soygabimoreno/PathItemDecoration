package soy.gabimoreno.pathitemdecoration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import soy.gabimoreno.pathitemdecoration.databinding.ItemFakeBinding

class FakeAdapter(
    private val onItemSelected: (fakeItem: FakeItem, selectedPosition: Int) -> Unit
) : ListAdapter<FakeItem, FakeAdapter.ViewHolder>(diffUtilCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(ItemFakeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemSelected(item, holder.adapterPosition) }
    }

    class ViewHolder(private val binding: ItemFakeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            fakeItem: FakeItem
        ) {
            binding.apply {
                tvValue.text = fakeItem.value.toString()
                tvLabel.text = fakeItem.label
            }
        }
    }

    companion object {
        private val diffUtilCallback = object : DiffUtil.ItemCallback<FakeItem>() {
            override fun areItemsTheSame(
                oldItem: FakeItem,
                newItem: FakeItem
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: FakeItem,
                newItem: FakeItem
            ): Boolean =
                oldItem == newItem
        }
    }
}
