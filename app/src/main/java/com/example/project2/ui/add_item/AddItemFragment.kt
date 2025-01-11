package com.example.project2.ui.add_item

import android.content.Intent
import android.content.pm.PackageManager
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

    private val viewModel: ItemsViewModel by activityViewModels()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                imageUri = it
                binding.imageBtn.setImageURI(it)
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
            val title = if (binding.itemTitle.text.toString().isBlank()) "" else binding.itemTitle.text.toString()
            val comment = if (binding.itemComment.text.toString().isBlank()) "" else binding.itemComment.text.toString()
            val photo = imageUri?.toString()
            val priceText = binding.price.text.toString()

            val link = if (binding.itemLink.text.toString().isBlank()) "" else binding.itemLink.text.toString()
            val selectedCategoryText = if (selectedCategories.isEmpty()) "" else selectedCategories.joinToString(", ")
            val price = priceText.toDoubleOrNull() ?: 0.0
            val item = Item(
                title = title,
                comment = comment,
                photo = photo,
                price = price,
                category = selectedCategoryText,
                link = link,
                rating = selectedRating
            )

            viewModel.addItem(item)

            // הצגת Toast לאחר הוספת ההמלצה
            Toast.makeText(requireContext(), getString(R.string.recommendation_published), Toast.LENGTH_SHORT).show()

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
                    0 -> requestCameraPermission() // בקשת הרשאה לפני צילום תמונה
                    1 -> pickImageLauncher.launch(arrayOf("image/*")) // בחירת תמונה מהמכשיר
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(android.Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            takePhoto()
        }
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
            binding.btn7 to getString(R.string.travel),
            binding.btn8 to getString(R.string.music),
            binding.btn9 to getString(R.string.book),
            binding.btn10 to getString(R.string.shops),
            binding.btn11 to getString(R.string.movie),
            binding.btn12 to getString(R.string.health)
        )

        buttons.forEach { (button, category) ->
            button.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue1))
                } else {
                    selectedCategories.add(category)
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
                }
            }
        }
    }

    private fun setupStarRating() {
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1
                Log.d("StarRating", "Selected Rating: $selectedRating")
                updateStarDisplay(selectedRating - 1, stars)
            }
        }
    }

    private fun updateStarDisplay(selectedIndex: Int, stars: List<ImageView>) {
        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index <= selectedIndex) R.drawable.star_full else R.drawable.star_empty
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            } else {
                Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
