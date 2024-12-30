package com.example.project2

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.databinding.RecommendationLayoutBinding

class ItemAdapter(val items: List<Item>, val callBack: ItemListener)
    : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    interface ItemListener {
        fun onItemClicked(index: Int)
        fun onItemLongClicked(index: Int)
    }

    inner class ItemViewHolder(private val binding: RecommendationLayoutBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            callBack.onItemClicked(adapterPosition)
            val context = binding.root.context
            val bundle = Bundle().apply {
                putString("title", items[adapterPosition].title)
                putString("comment", items[adapterPosition].comment)
                putDouble("price", items[adapterPosition].price)
                putString("photo", items[adapterPosition].photo)
                putString("link", items[adapterPosition].link) // ודא שזה קיים
                putString("category", items[adapterPosition].category) // העברת הקטגוריות

                putInt("rating", items[adapterPosition].rating)
            }

            // ניווט ל-ItemDetailsFragment
            val navController = Navigation.findNavController(binding.root)
            navController.navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)

        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(item: Item) {
            // עדכון כותרת והערות
            binding.itemTitle.text = item.title


            // עדכון תמונה אם קיימת
            binding.itemImage.setImageURI(item.photo?.let { Uri.parse(it) })

            // עדכון מחיר
            binding.priceTitle.text = item.price.toString() ?: "N/A"

            // עדכון הכוכבים
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RecommendationLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
