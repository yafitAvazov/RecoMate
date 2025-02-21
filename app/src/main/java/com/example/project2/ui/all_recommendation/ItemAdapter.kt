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
        fun onItemDeleted(item: Item) // ✅ פונקציה חדשה למחיקת פריט
        fun onItemLiked(item: Item) // ✅ הוספת לפריטים אהובים

        fun onItemUnliked(item: Item) // ✅ הסרת פריטים אהובים

    }

    inner class ItemViewHolder(val binding: RecommendationLayoutBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(null)

            binding.editBtn.setOnClickListener {
                val item = items[adapterPosition]
                val bundle = bundleOf("item" to item) // ✅ Pass full Item object

                val navController = binding.root.findNavController()
                val currentDestination = navController.currentDestination?.id

                when (currentDestination) {
                    R.id.allItemsFragment -> {
                        navController.navigate(R.id.action_allItemsFragment_to_updateItemFragment, bundle)
                    }
                    R.id.myRecommendationsFragment -> {
                        navController.navigate(R.id.action_myRecommendationsFragment_to_updateItemFragment, bundle)
                    }
                    R.id.specificCategoryItemsFragment -> {
                        navController.navigate(R.id.action_specificCategoryItemsFragment_to_updateItemFragment, bundle)
                    }
                    else -> {
                        Toast.makeText(binding.root.context,
                            binding.root.context.getString(R.string.navigation_error_unknown_source_fragment), Toast.LENGTH_SHORT).show()
                    }
                }
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
            val item = items[adapterPosition] // 🔥 מקבל את הפריט שנלחץ
            val bundle = bundleOf("itemId" to item.id)

            // 🔥 מנווטים לפרטי הפריט רק בלחיצה רגילה
            when (currentDestination) {
                R.id.allItemsFragment -> {
                    navController.navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
                }
                R.id.myRecommendationsFragment -> {
                    navController.navigate(R.id.action_myRecommendationsFragment_to_itemDetailsFragment, bundle)
                }
                R.id.specificCategoryItemsFragment -> {
                    navController.navigate(R.id.action_specificCategoryItemsFragment_to_itemDetailsFragment, bundle)
                }
                else -> {
                    // במקרה שאין יעד מתאים (אופציונלי - רק לדיוג)
                    Toast.makeText(binding.root.context,
                        binding.root.context.getString(R.string.navigation_error_unknown_source_fragment),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }




        override fun onLongClick(v: View?): Boolean {
            return false

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

            // ✅ Display correct buttons based on item ownership
            if (item.userId == currentUserId) {
                // 🔥 If the logged-in user is the owner of the item
                binding.itemCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.green))
                binding.itemCard.setContentPadding(6, 6, 6, 6)

                binding.editBtn.visibility = View.VISIBLE
                binding.deleteBtn.visibility = View.VISIBLE
                binding.likeBtn.visibility = View.GONE
            } else {
                // 🔥 If the item belongs to another user
                binding.itemCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.light_blue))

                binding.editBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.GONE
                binding.likeBtn.visibility = View.VISIBLE
            }

            // ✅ Update Like Button State
            val isLiked = currentUserId?.let { item.likedBy.contains(it) } ?: false
            binding.likeBtn.setImageResource(
                if (isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
            )

            // ✅ Handle Like Button Click
            binding.likeBtn.setOnClickListener {
                if (currentUserId == null) return@setOnClickListener // Ensure user is logged in

                val updatedLikedBy = if (isLiked) item.likedBy - currentUserId else item.likedBy + currentUserId
                callBack.onItemLiked(item.copy(likedBy = updatedLikedBy))
            }

            // ✅ Handle Delete Button Click
            binding.deleteBtn.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle(binding.root.context.getString(R.string.delete_confirmation))
                    .setMessage(binding.root.context.getString(R.string.are_you_sure_you_want_to_delete_this_recommendation))
                    .setPositiveButton(binding.root.context.getString(R.string.yes)) { _, _ ->
                        callBack.onItemDeleted(item)
                    }
                    .setNegativeButton(binding.root.context.getString(R.string.no), null)
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
