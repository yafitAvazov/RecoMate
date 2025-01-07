package com.example.project2.ui.all_items

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.data.model.Item
import com.example.project2.R
import com.example.project2.databinding.RecommendationLayoutBinding

class ItemAdapter(var items: List<Item>, val callBack: ItemListener)
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
            binding.editBtn.setOnClickListener {
                val item = items[adapterPosition]
                val bundle = Bundle().apply {
                    putParcelable(getString(R.string.item.toString()), item)
                }
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_allItemsFragment_to_updateItemFragment, bundle)
            }
        }


        override fun onClick(p0: View?) {
            callBack.onItemClicked(adapterPosition)
            val context = binding.root.context
            val bundle = Bundle().apply {
                putString(context.getString(R.string.item), items[adapterPosition].title)
                putString(context.getString(R.string.comment), items[adapterPosition].comment)
                putDouble(context.getString(R.string.price), items[adapterPosition].price)
                putString(context.getString(R.string.photo), items[adapterPosition].photo)
                putString(context.getString(R.string.link), items[adapterPosition].link) // ודא שזה קיים
                putString(context.getString(R.string.category), items[adapterPosition].category) // העברת הקטגוריות

                putInt(context.getString(R.string.rating), items[adapterPosition].rating)
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

            binding.itemTitle.text = if (item.title.isBlank()) binding.root.context.getString(R.string.no_title) else item.title

//            binding.itemComment.text = if (item.comment.isBlank()) "No Comment" else item.comment
            // עדכון תמונה אם קיימת
            binding.itemImage.setImageURI(item.photo?.let { Uri.parse(it) })
            // עדכון מחיר
//            binding.priceTitle.text = item.price.toString() ?: "N/A"
            binding.priceTitle.text = if (item.price == 0.0) binding.root.context.getString(R.string.no_price) else item.price.toString()

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

    override fun getItemCount() = items.size
}
