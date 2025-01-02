package com.example.project2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.project2.databinding.FragmentItemDetailsBinding

class ItemDetailsFragment : Fragment() {
    private var _binding: FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
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
//            binding.itemCategory.text = "Category: ${bundle.getString("category", "None")}"

            val categories = bundle.getString("category", "No Category").split(", ").filter { it.isNotBlank() }
            setupCategoryButtons(categories)

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
        // איפוס התצוגה
        binding.categoryContainer.removeAllViews()

        if (categories.isEmpty()) {
            val noCategoryText = TextView(requireContext()).apply {
                text = "No Category"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
