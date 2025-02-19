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
            binding.root.setOnLongClickListener(this)

            binding.editBtn.setOnClickListener {
                val item = items[adapterPosition]
                val bundle = Bundle().apply {
                    putParcelable("item", item)
                }
                binding.root.findNavController().navigate(R.id.action_myRecommendationsFragment_to_updateItemFragment, bundle)
            }
        }
        override fun onClick(v: View?) {
            val navController = Navigation.findNavController(binding.root)
            val currentDestination = navController.currentDestination?.id

            if (currentDestination == R.id.specificCategoryItemsFragment) {
                Toast.makeText(binding.root.context, "Long click for details", Toast.LENGTH_SHORT).show()
            return
            }

            val item = items[adapterPosition]
            val bundle = bundleOf("itemId" to item.id)

            val navController = Navigation.findNavController(binding.root)
            val currentDestination = navController.currentDestination?.id

            when (currentDestination) {
                R.id.allItemsFragment -> {
                    navController.navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
                }
                R.id.myRecommendationsFragment -> {
                    navController.navigate(R.id.action_myRecommendationsFragment_to_itemDetailsFragment, bundle)
                }
                else -> {
                    // במקרה שאין יעד מתאים (אופציונלי - רק לדיוג)
                    println("⚠️ Navigation Error: Unknown source fragment!")
                }
            }
        }



        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(item: Item, currentUserId: String?) {
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
            // ✅ הצגת הכפתורים הנכונים
            if (item.userId == currentUserId) {
                // 🔥 אם המשתמש המחובר הוא זה שפרסם את ההמלצה
                binding.itemCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.green))
                binding.itemCard.setContentPadding(5, 5, 5, 5)
                binding.editBtn.visibility = View.VISIBLE
                binding.deleteBtn.visibility = View.VISIBLE
                binding.likeBtn.visibility = View.GONE
            } else {
                // 🔥 אם זו המלצה של משתמש אחר
                binding.itemCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.light_blue))

                binding.editBtn.visibility = View.GONE
                binding.deleteBtn.visibility = View.GONE
                binding.likeBtn.visibility = View.VISIBLE
            }

            // ✅ עדכון מצב הלב (ריק או מלא) בעת לחיצה
//            var isLiked = item.isLiked // נניח שיש שדה כזה בפריט
            binding.likeBtn.setOnClickListener {
                val isNowLiked = !item.isLiked
                item.isLiked = isNowLiked
                callBack.onItemLiked(item)
                updateLikeButton(isNowLiked)
            }










            binding.deleteBtn.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this recommendation?")
                    .setPositiveButton("Yes") { _, _ ->
                        callBack.onItemDeleted(item) // ✅ מעביר למחיקה גם מההמלצות שלי וגם מכל ההמלצות
                    }
                    .setNegativeButton("No", null)
                    .show()
            }


        }
        // פונקציה שמשנה את צבע הלב
        private fun updateLikeButton(isLiked: Boolean) {
            binding.likeBtn.setImageResource(
                if (isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
            )
        }
        // ✅ פונקציה לעדכון הלייק במסד הנתונים (אם רוצים)
        private fun updateItemLikeStatus(itemId: Int, isLiked: Boolean) {
            val likeStatus = hashMapOf("isLiked" to isLiked)
            FirebaseFirestore.getInstance().collection("items")
                .document(itemId.toString())
                .update(likeStatus as Map<String, Any>)
        }
    }


    fun itemAt(position: Int): Item {
        return items[position]
    }

    fun deleteItem(item: Item) {
        val newList = items.toMutableList().apply { remove(item) }
        updateList(newList) // 🔥 מעדכן את הרשימה אחרי מחיקה
    }

//    fun updateList(newItems: List<Item>) {
//        val diffCallback = object : DiffUtil.Callback() {
//            override fun getOldListSize() = items.size
//            override fun getNewListSize() = newItems.size
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return items[oldItemPosition].id == newItems[newItemPosition].id
//            }
//
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return items[oldItemPosition] == newItems[newItemPosition]
//            }
//        }
//
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        items = newItems
//        diffResult.dispatchUpdatesTo(this)
//    }
fun updateList(newItems: List<Item>) {
    items = newItems
    notifyDataSetChanged()
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(RecommendationLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // ✅ מזהה המשתמש המחובר
        val item = items[position]
        holder.bind(item, currentUserId) // ✅ שולח את הפריט ל-ViewHolder


        }
    override fun getItemCount(): Int = items.size

}
