package com.example.project2.ui.single_item

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project2.R
import com.example.project2.databinding.FragmentItemDetailsBinding
import com.example.project2.ui.ItemsViewModel

class ItemDetailsFragment : Fragment() {
    private var _binding: FragmentItemDetailsBinding? = null

    val viewModel: ItemsViewModel by activityViewModels()

    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)


        return binding.root
    }

    private fun setupCategoryButtons(categories: List<String>) {
        // איפוס התצוגה
        binding.categoryContainer.removeAllViews()

        if (categories.isEmpty()) {
            val noCategoryText = TextView(requireContext()).apply {
                text = getString(R.string.no_category)
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(8, 8, 8, 8)
            }
            binding.categoryContainer.addView(noCategoryText)
        } else {
            // יצירת כפתורים עבור קטגוריות שנבחרו
            categories.forEach { category ->
                val button = Button(requireContext()).apply {
                    text = category
                    textSize = 16f
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setPadding(8, 8, 8, 8)
                }
                binding.categoryContainer.addView(button)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // צופה בנתונים שנבחרו ב-ViewModel
        viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
            binding.itemTitle.text = item.title.ifBlank { getString(R.string.no_title) }
            binding.itemComment.text = item.comment.ifBlank { getString(R.string.no_comment) }
            binding.itemPrice.text =
                if (item.price == 0.0) getString(R.string.no_price) else "Price: ${item.price}"
            binding.itemLink.text = item.link.ifBlank { getString(R.string.no_link) }
            binding.addressTextView.text = item.address?.ifBlank { "No address" }

            // עדכון תמונה
            if (item.photo.isNullOrEmpty()) {
                binding.itemImage.setImageResource(R.drawable.baseline_hide_image_24)
            } else {
                binding.itemImage.setImageURI(Uri.parse(item.photo))
            }

            // עדכון דירוג כוכבים
            val stars =
                listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < item.rating) R.drawable.star_full else R.drawable.star_empty
                )
            }

            // עדכון קטגוריות
            setupCategoryButtons(item.category.split(", ").filter { it.isNotBlank() })
        }
        binding.showAddressButton.setOnClickListener {

            if (binding.addressTextView.text == "No address") {
                Toast.makeText(requireContext(), "No address to check", Toast.LENGTH_SHORT).show()
            }
            else {
                viewModel.chosenItem.value?.let { item ->
                    val bundle = Bundle().apply {
                        putString("address", item.address)
                    }
                    findNavController().navigate(
                        R.id.action_itemDetailsFragment_to_mapFragment,
                        bundle
                    )
                }

            }

        }



    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
