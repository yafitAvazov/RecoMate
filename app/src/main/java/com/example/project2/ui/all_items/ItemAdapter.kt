package com.example.project2.ui.all_items

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.RecommendationLayoutBinding
import com.example.project2.ui.ItemsViewModel

class ItemAdapter(
    var items: List<Item>,
    private val viewModel: ItemsViewModel,
    private val callBack: ItemListener
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    interface ItemListener {
        fun onItemClicked(index: Int)
        fun onItemLongClicked(index: Int)
    }

    inner class ItemViewHolder(private val binding: RecommendationLayoutBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            // מאזין ללחיצה על כרטיסייה כדי להיכנס לעמוד הפרטים
            binding.root.setOnClickListener(this)

            // מאזין ללחיצה ארוכה על כרטיסייה
            binding.root.setOnLongClickListener(this)

            // מאזין ללחיצה על כפתור עריכה
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
            viewModel.setItem(item) // שמירת הפריט ב-ViewModel
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_allItemsFragment_to_itemDetailsFragment)
        }


        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(item: Item) {
            // שם הפריט
            binding.itemTitle.text = if (item.title=="")binding.root.context.getString((R.string.no_title)) else item.title



            // תמונה
            if (item.photo.isNullOrEmpty()) {
                binding.itemImage.setImageResource(R.drawable.baseline_hide_image_24) // תמונת ברירת מחדל
            } else {
                binding.itemImage.setImageURI(Uri.parse(item.photo))
            }

            // מחיר
            binding.priceTitle.text = if (item.price == 0.0) binding.root.context.getString(R.string.no_price) else "$${item.price}"

            // דירוג כוכבים
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

    fun updateList(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
    fun itemAt(position: Int) = items[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RecommendationLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
