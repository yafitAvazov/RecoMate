package com.example.project2.ui.all_recommendation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.RecommendationLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ItemAdapter(
    var items: List<Item>,
    private val callBack: ItemListener
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    interface ItemListener {
        fun onItemClicked(index: Int)
        fun onItemLongClicked(index: Int)
        fun onItemDeleted(item: Item)
        fun onItemLiked(item: Item)
        fun onItemUnliked(item: Item)
    }

    inner class ItemViewHolder(val binding: RecommendationLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)

            binding.editBtn.setOnClickListener {
                val item = items[adapterPosition]
                val bundle = bundleOf("itemId" to item.id)
                binding.root.findNavController()
                    .navigate(R.id.action_myRecommendationsFragment_to_updateItemFragment, bundle)
            }
        }

        private fun updateLikeButton(isLiked: Boolean) {
            binding.likeBtn.setImageResource(
                if (isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
            )
        }


        override fun onClick(v: View?) {
            val navController = Navigation.findNavController(binding.root)
            val currentDestination = navController.currentDestination?.id
            val clickedItem = items[adapterPosition]
            callBack.onItemClicked(adapterPosition)

            val item = items[adapterPosition]
            val bundle = bundleOf("itemId" to item.id)

            when (currentDestination) {
                R.id.allItemsFragment -> {
                    navController.navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
                }
                R.id.myRecommendationsFragment -> {
                    navController.navigate(R.id.action_myRecommendationsFragment_to_itemDetailsFragment, bundle)
                }
                else -> {
                    println("⚠️ Navigation Error: Unknown source fragment!")
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(item: Item, currentUserId: String?) {
            binding.itemTitle.text = if (item.title.isBlank()) binding.root.context.getString(R.string.no_title) else item.title

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

            if (item.userId == currentUserId) {
                binding.itemCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.green))
                binding.itemCard.setContentPadding(5, 5, 5, 5)
                binding.editBtn.visibility = View.VISIBLE
                binding.deleteBtn.visibility = View.VISIBLE
                binding.likeBtn.visibility = View.GONE
            } else {
                binding.itemCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.light_blue))
                binding.editBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.GONE
                binding.likeBtn.visibility = View.VISIBLE
            }

            binding.likeBtn.setImageResource(
                if (item.isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
            )

            binding.likeBtn.setOnClickListener {
                val isNowLiked = !item.isLiked

                // ✅ Update UI immediately
                item.isLiked = isNowLiked
                updateLikeButton(isNowLiked)

                // ✅ Pass updated item to ViewModel for database update
                if (isNowLiked) {
                    callBack.onItemLiked(item.copy(isLiked = true))
                } else {
                    callBack.onItemUnliked(item.copy(isLiked = false))
                }
            }


            binding.deleteBtn.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this recommendation?")
                    .setPositiveButton("Yes") { _, _ ->
                        callBack.onItemDeleted(item)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(RecommendationLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        holder.bind(items[position], currentUserId)
    }

    override fun getItemCount(): Int = items.size
}

