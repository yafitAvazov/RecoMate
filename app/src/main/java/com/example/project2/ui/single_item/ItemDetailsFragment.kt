package com.example.project2.ui.single_item

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.project2.R
import com.example.project2.databinding.FragmentItemDetailsBinding
import com.example.project2.ui.ItemsViewModel

class ItemDetailsFragment : Fragment() {
    private var _binding: FragmentItemDetailsBinding? = null

    val viewModel : ItemsViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)

        // קבלת הנתונים מה-Bundle
        arguments?.let { bundle ->
            binding.itemTitle.text = bundle.getString("title")
            binding.itemComment.text = bundle.getString("comment")
            binding.itemPrice.text = "Price: ${bundle.getDouble("price")}"
            binding.itemCategory.text = "Category: ${bundle.getString("category", "None")}"

            val categories = bundle.getString("category", "").split(", ")
            setupCategoryButtons(categories) // יצירת כפתורי קטגוריות

            val link = bundle.getString("link")
            binding.itemLink.text = link

            // עדכון תמונה
            val photoUri = bundle.getString("photo")
            binding.itemImage.setImageURI(photoUri?.let { Uri.parse(it) })

            // עדכון כוכבים
            val rating = bundle.getInt("rating")
            val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < rating) R.drawable.star_full else R.drawable.star_empty
                )
            }
            // הוספת פונקציונליות ללינק
            binding.itemLink.setOnClickListener {
                link?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(intent)
                }
        }}

        return binding.root
    }
    private fun setupCategoryButtons(categories: List<String>) {
        // יצירת כפתורים עבור הקטגוריות שנבחרו
        binding.categoryContainer.removeAllViews() // איפוס התצוגה
        categories.forEach { category ->
            val button = Button(requireContext()).apply {
                text = category
                textSize = 16f
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_button))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setPadding(8, 8, 8, 8)
            }
            binding.categoryContainer.addView(button)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.chosenItem.observe(viewLifecycleOwner) {
            binding.itemTitle.text = it.title
            binding.itemComment.text = it.comment
            binding.itemPrice.text = "Price: ${it.price}"
            binding.itemCategory.text = "Category: ${it.category}"
            binding.itemLink.text = it.link
            binding.itemImage.setImageURI(it.photo?.let { Uri.parse(it) }) // not sure
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
