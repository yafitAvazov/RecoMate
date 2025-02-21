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
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.UpdateRecommendationLayoutBinding
import com.example.project2.ui.recommendation_detail.RecommendationDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@AndroidEntryPoint
class UpdateItemFragment : Fragment() {
    private var _binding: UpdateRecommendationLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationDetailViewModel by activityViewModels()

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

        // Retrieve the item using the fixed key
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
            binding.imageBtn.setImageURI(Uri.parse(item.photo))
            imageUri = Uri.parse(item.photo)
        }
        if (item.address.isNullOrEmpty()){
            binding.editAddressEditText.setText(context?.getString(R.string.no_address))
        }
        else
        {
            binding.editAddressEditText.setText(item.address)
        }
        selectedRating = item.rating
        selectedCategories.clear()
        selectedCategories.addAll(item.category.split(getString(R.string.separator)))
        updateStarDisplay(selectedRating - 1)
        updateCategoryButtons()
    }

    private fun updateItem() {
        val title = binding.itemTitle.text.toString()
        val price = binding.price.text.toString().toDoubleOrNull() ?: 0.0
        val link = binding.itemLink.text.toString()
        val comment = binding.itemComment.text.toString()
        val photoUriString = imageUri?.toString() ?: ""
        val address = binding.editAddressEditText.text.toString()

        val updatedItem = Item(
            id = existingItemId ?: "",
            title = title,
            price = price,
            link = link,
            comment = comment,
            photo = if (photoUriString.isEmpty()) null else photoUriString,
            rating = selectedRating,
            category = selectedCategories.joinToString(getString(R.string.separator)),
            address = address,
            userId = viewModel.currentUserId ?: "" // ✅ שימוש ב- userId מהמשתמש המחובר
        )

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.updateItem(updatedItem)
            Toast.makeText(requireContext(), getString(R.string.item_updated_successfully), Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateItemFragment_to_allItemsFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
