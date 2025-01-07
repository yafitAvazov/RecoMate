package com.example.project2.ui.add_item

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project2.data.model.Item
import com.example.project2.R
import com.example.project2.databinding.AddRecommendationLayoutBinding
import java.io.File
import com.example.project2.ui.ItemsViewModel

class AddItemFragment : Fragment() {
    private var _binding: AddRecommendationLayoutBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private var selectedRating: Int = 0 // שומר את הדירוג הנבחר
    private val selectedCategories = mutableSetOf<String>() // רשימת קטגוריות שנבחרו

    private val viewModel : ItemsViewModel by activityViewModels()


    // Launch Activity for image selection
    // Launchers
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                imageUri = it
                binding.imageBtn.setImageURI(it) // הצגת התמונה שנבחרה
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                binding.imageBtn.setImageURI(imageUri)
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

            val title = if (binding.itemTitle.text.toString().isBlank()) "No Title" else binding.itemTitle.text.toString()
            val comment = if (binding.itemComment.text.toString().isBlank()) "No Comment" else binding.itemComment.text.toString()
            val photo = imageUri?.toString()
            val priceText = binding.price.text.toString()

            val link = if (binding.itemLink.text.toString().isBlank()) "No Link" else binding.itemLink.text.toString()
            val selectedCategoryText = if (selectedCategories.isEmpty()) "No Category" else selectedCategories.joinToString(", ")
            val price = priceText.toDoubleOrNull() ?: 0.0
            val item = Item(
                title = title,
                comment = comment,
                photo = photo,
                price = price,
                category = selectedCategoryText,
                link =link,
                rating = selectedRating // משתמש בדירוג שנבחר
            )

            //ItemManager.add(item)

            viewModel.addItem(item)

            // הצגת Toast לאחר הוספת ההמלצה
            Toast.makeText(requireContext(), "Recommendation published successfully!", Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment)
        }

        // הגדרת לחצן בחירת תמונה/צילום
        binding.imageBtn.setOnClickListener {
            showImagePickerDialog()
        }

        // הגדרת דירוג כוכבים
        setupStarRating()

        return binding.root
    }
    private fun showImagePickerDialog() {
        val options = arrayOf(getString(R.string.take_photo), getString(R.string.choose_from_device))
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_option))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto() // צילום תמונה
                    1 -> pickImageLauncher.launch(arrayOf("image/*")) // בחירת תמונה מהמכשיר
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun takePhoto() {
        val photoFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePhotoLauncher.launch(imageUri)
    }
    private fun setupCategoryButtons() {
        val buttons = listOf(
            binding.btn1 to getString(R.string.fashion),
            binding.btn2 to getString(R.string.food),
            binding.btn3 to getString(R.string.game),
            binding.btn4 to getString(R.string.home),
            binding.btn5 to getString(R.string.tech),
            binding.btn6 to getString(R.string.sport),
            binding.btn7 to "travel",
            binding.btn8 to "music",
            binding.btn9 to "book",
            binding.btn10 to "shops",
            binding.btn11 to "movie",
            binding.btn12 to "health"
        )

        buttons.forEach { (button, category) ->
            button.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue1)) // צבע ברירת מחדל
                } else {
                    selectedCategories.add(category)
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray)) // צבע לחצן נבחר
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
