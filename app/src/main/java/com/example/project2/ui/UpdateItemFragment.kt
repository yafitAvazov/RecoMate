package com.example.project2.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.project2.R
import com.example.project2.data.model.CategoryMapper
import com.example.project2.data.model.Item
import com.example.project2.databinding.UpdateRecommendationLayoutBinding
import com.example.project2.ui.recommendation_detail.RecommendationDetailViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@AndroidEntryPoint
class UpdateItemFragment : Fragment() {
    private var _binding: UpdateRecommendationLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationDetailViewModel by activityViewModels()
    private val itemRef = FirebaseFirestore.getInstance().collection("items")


    private var existingItemId: String? = null
    private var selectedRating: Int = 0
    private val selectedCategories = mutableSetOf<String>()
    private var imageUri: Uri? = null



    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.imageBtn.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UpdateRecommendationLayoutBinding.inflate(inflater, container, false)


        val item = requireArguments().getParcelable<Item>("item")

        item?.let { populateFields(it) }

        binding.removeImageButton.setOnClickListener {
            removeImage()
        }

        binding.chooseImageButton.setOnClickListener {
            chooseImage()
        }

        binding.finishBtn.setOnClickListener {
            updateItem()
        }

        setupStarRating()
        setupCategoryButtons()

        return binding.root
    }

    private fun populateFields(item: Item) {
        existingItemId = item.id
        binding.itemTitle.setText(item.title)
        binding.price.setText(item.price.toString())
        binding.itemLink.setText(item.link)
        binding.itemComment.setText(item.comment)

        if (item.photo.isNullOrEmpty()) {
            binding.imageBtn.setImageResource(R.drawable.baseline_hide_image_24)
        } else {
            imageUri = Uri.parse(item.photo)
            if (item.photo.startsWith("https")) {
                loadImageFromFirebase(item.photo)
            } else {
                binding.imageBtn.setImageURI(imageUri)
            }
        }

        binding.editAddressEditText.setText(item.address ?: context?.getString(R.string.no_address))

        selectedRating = item.rating
        selectedCategories.clear()


        val categoryIds = item.category.split(",").mapNotNull { it.toIntOrNull() }
        selectedCategories.addAll(categoryIds.mapNotNull { CategoryMapper.getLocalizedCategory(it, requireContext()) })

        updateStarDisplay(selectedRating - 1)
        updateCategoryButtons()
    }


    private fun updateItem() {
        try {
            val title = binding.itemTitle.text.toString().trim()
            val comment = binding.itemComment.text.toString().trim()
            val priceText = binding.price.text.toString()
            val address = binding.editAddressEditText.text.toString().trim()
            val link = binding.itemLink.text.toString().trim()
            val categoryIds = selectedCategories.mapNotNull { CategoryMapper.getCategoryId(it, requireContext()) }
            val categoryString = categoryIds.joinToString(",")
            val price = priceText.toDoubleOrNull() ?: 0.0
            val userId = viewModel.currentUserId ?: throw Exception("User not logged in.")
            val itemId = existingItemId ?: throw Exception("Item ID is missing.")


            binding.postProgressBar.visibility = View.VISIBLE
            binding.finishBtn.isEnabled = false
            binding.finishBtn.text = getString(R.string.updating)


            if (imageUri != null && !imageUri.toString().startsWith("https")) {
                uploadImageToFirebaseStorage(imageUri!!) { imageUrl ->
                    saveUpdatedItem(itemId, userId, title, comment, imageUrl, price, categoryString, link, selectedRating, address)
                }
            } else {
                saveUpdatedItem(itemId, userId, title, comment, imageUri?.toString(), price, categoryString, link, selectedRating, address)
            }


        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.postProgressBar.visibility = View.GONE
            binding.finishBtn.isEnabled = true
            binding.finishBtn.text = getString(R.string.finish)
        }
    }


    private fun uploadImageToFirebaseStorage(imageUri: Uri, onSuccess: (String) -> Unit) {
        val storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),
                    getString(R.string.image_upload_failed), Toast.LENGTH_SHORT).show()
                binding.postProgressBar.visibility = View.GONE
                binding.finishBtn.isEnabled = true
                binding.finishBtn.text = getString(R.string.finish)
            }
    }



    private fun removeImage() {
        binding.imageBtn.setImageResource(R.drawable.baseline_hide_image_24)
        imageUri = null
    }

    private fun chooseImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun setupStarRating() {
        val stars = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1
                updateStarDisplay(index)
            }
        }
    }

    private fun updateStarDisplay(selectedIndex: Int) {
        val stars = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index <= selectedIndex) R.drawable.star_full else R.drawable.star_empty
            )
        }
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
            binding.btn8 to getString(R.string.beauty),
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
                    if (selectedCategories.size < 3) {
                        selectedCategories.add(category)
                        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
                    } else {
                        Toast.makeText(requireContext(),
                            getString(R.string.only_3_categories_are_allowed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateCategoryButtons() {
        val buttons = listOf(
            binding.btn1 to getString(R.string.fashion),
            binding.btn2 to getString(R.string.food),
            binding.btn3 to getString(R.string.game),
            binding.btn4 to getString(R.string.home),
            binding.btn5 to getString(R.string.tech),
            binding.btn6 to getString(R.string.sport),
            binding.btn7 to getString(R.string.travel),
            binding.btn8 to getString(R.string.beauty),
            binding.btn9 to getString(R.string.book),
            binding.btn10 to getString(R.string.shops),
            binding.btn11 to getString(R.string.movie),
            binding.btn12 to getString(R.string.health)
        )

        buttons.forEach { (button, category) ->
            if (selectedCategories.contains(category)) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
            } else {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            }
        }
    }
    private fun loadImageFromFirebase(imageUrl: String) {
        try {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.baseline_hide_image_24)
                .into(binding.imageBtn)
        } catch (e: Exception) {
            Toast.makeText(requireContext(),
                getString(R.string.failed_to_load_image, e.message), Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveUpdatedItem(
        title: String,
        price: Double,
        link: String,
        comment: String,
        photoUrl: String?,
        category: String,
        address: String
    ) {
        val updatedItem = Item(
            id = existingItemId ?: "",
            title = title,
            price = price,
            link = link,
            comment = comment,
            photo = if (photoUrl.isNullOrEmpty()) null else photoUrl,
            rating = selectedRating,
            category = category,
            address = address,
            userId = viewModel.currentUserId ?: ""
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                viewModel.updateItem(updatedItem)
                viewModel.fetchItemsByCategory(category)

                Toast.makeText(requireContext(), getString(R.string.item_updated_successfully), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateItemFragment_to_allItemsFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_updating_item, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveUpdatedItem(
        itemId: String,
        userId: String,
        title: String,
        comment: String,
        photoUrl: String?,
        price: Double,
        category: String,
        link: String,
        rating: Int,
        address: String
    ) {
        val updatedItem = Item(
            id = itemId,
            userId = userId,
            title = title,
            comment = comment,
            photo = if (photoUrl.isNullOrEmpty()) null else photoUrl,
            price = price,
            category = category,
            link = link,
            rating = rating,
            address = address
        )

        itemRef.document(itemId).set(updatedItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.item_updated_successfully), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateItemFragment_to_allItemsFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), getString(R.string.error_updating_item), Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.postProgressBar.visibility = View.GONE
                binding.finishBtn.isEnabled = true
                binding.finishBtn.text = getString(R.string.finish)
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
