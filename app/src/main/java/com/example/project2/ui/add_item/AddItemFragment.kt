package com.example.project2.ui.add_item

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project2.data.model.Item
import com.example.project2.R
import com.example.project2.databinding.AddRecommendationLayoutBinding
import com.example.project2.ui.ItemsViewModel

class AddItemFragment : Fragment() {
    private var _binding: AddRecommendationLayoutBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private var selectedRating: Int = 0 // שומר את הדירוג הנבחר
    private val selectedCategories = mutableSetOf<String>() // רשימת קטגוריות שנבחרו

    private val viewModel : ItemsViewModel by activityViewModels()


    // Launch Activity for image selection
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                binding.resultImage.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = it
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddRecommendationLayoutBinding.inflate(inflater, container, false)
        setupCategoryButtons() // הגדרת לחצני הקטגוריות

        // הגדרת כפתור Finish להוספת פריט
        binding.finishBtn.setOnClickListener {
            val price = binding.price.text.toString().toDoubleOrNull() ?: 0.0 // אם השדה ריק או שגוי, המחיר יהיה 0.0
            val selectedCategoryText = selectedCategories.joinToString(", ")
            val item = Item(
                title = binding.itemTitle.text.toString(),
                comment = binding.itemComment.text.toString(),
                photo = imageUri?.toString(),
                price = price,
                category = selectedCategoryText,
                link =binding.itemLink.text.toString(),
                rating = selectedRating // משתמש בדירוג שנבחר
            )

            //ItemManager.add(item)

            viewModel.addItem(item)

            findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment)
        }

        // הגדרת לחצן בחירת תמונה
        binding.imageBtn.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        // הגדרת דירוג כוכבים
        setupStarRating()

        return binding.root
    }
    private fun setupCategoryButtons() {
        val buttons = listOf(
            binding.alertDialogBtn to "Fashion",
            binding.dateDialogBtn to "Food",
            binding.customDialogBtn1 to "Game",
            binding.customDialogBtn2 to "Home",
            binding.customDialogBtn3 to "Tech",
            binding.customDialogBtn4 to "Sport"
        )

        buttons.forEach { (button, category) ->
            button.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(),
                        R.color.default_button
                    )) // צבע ברירת מחדל
                } else {
                    selectedCategories.add(category)
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(),
                        R.color.selected_button
                    )) // צבע לחצן נבחר
                }

            }
        }
    }



    // הגדרת פונקציית דירוג
    private fun setupStarRating() {
        // יצירת רשימה של הכוכבים
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

        // Listener לחיצה על כל כוכב
        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1 // שמירת הדירוג הנבחר
                Log.d("StarRating", "Selected Rating: $selectedRating") // בדיקה
                updateStarDisplay(selectedRating - 1, stars) // עדכון תצוגת הכוכבים
            }
        }
    }

    // עדכון תצוגת הכוכבים
    private fun updateStarDisplay(selectedIndex: Int, stars: List<ImageView>) {
        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index <= selectedIndex) R.drawable.star_full else R.drawable.star_empty
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
