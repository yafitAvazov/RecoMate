package com.example.project2.ui.all_recommendation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.RecommendationLayoutBinding

class ItemAdapter(
    var items: List<Item>,
    private val callBack: ItemListener
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    interface ItemListener {
        fun onItemClicked(index: Int)
        fun onItemLongClicked(index: Int)
    }

    inner class ItemViewHolder(private val binding: RecommendationLayoutBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)

            binding.editBtn.setOnClickListener {
                val item = items[adapterPosition]
                val bundle = Bundle().apply {
                    putParcelable("item", item)
                }
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_allItemsFragment_to_updateItemFragment, bundle)
            }
        }

        override fun onClick(v: View?) {
            val item = items[adapterPosition]
            val bundle = bundleOf("itemId" to item.id)
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
        }
        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(item: Item) {
            binding.itemTitle.text = if (item.title == "") binding.root.context.getString(R.string.no_title) else item.title

            if (item.photo.isNullOrEmpty()) {
                binding.itemImage.setImageResource(R.drawable.baseline_hide_image_24)
            } else {
                binding.itemImage.setImageURI(Uri.parse(item.photo))
            }

            binding.priceTitle.text = if (item.price == 0.0) binding.root.context.getString(R.string.no_price) else "$${item.price}"

            val stars = listOf(
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
            )

            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < item.rating) R.drawable.star_full else R.drawable.star_empty
                )
            }
        }
    }
    fun itemAt(position: Int): Item {
        return items[position]
    }

    fun updateList(newItems: List<Item>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newItems[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RecommendationLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
