package com.example.project2.ui.recommendation_detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project2.R
import com.example.project2.data.model.CategoryMapper
import com.example.project2.data.model.Item
import com.example.project2.databinding.FragmentItemDetailsBinding
import com.example.project2.ui.CommentsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecommendationDetailsFragment : Fragment() {
    private var _binding: FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RecommendationDetailViewModel>()
    private lateinit var commentsAdapter: CommentsAdapter
    private var itemId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemId = arguments?.getString("itemId")
        if (itemId != null) {
            Log.d("RecommendationDetailsFragment", "Item ID received: $itemId")
            viewModel.fetchItemById(itemId)
            setupCommentsAdapter()
            observeViewModel(itemId)
        } else {
            Log.e("RecommendationDetailsFragment", "No item ID received")
        }
    }

    private fun setupCommentsAdapter() {
        commentsAdapter = CommentsAdapter(mutableListOf())
    }
    private fun observeViewModel(itemId: String) {
        viewModel.getItemById(itemId).observe(viewLifecycleOwner) { updatedItem ->
            updatedItem?.let {
                updateUI(it)
                val updatedComments = it.comments ?: emptyList()
                commentsAdapter.updateComments(updatedComments)
            }
        }
    }

    private fun updateUI(item: Item) {
        binding.itemTitle.text = item.title.ifBlank { getString(R.string.no_title) }
        binding.itemComment.text = if (item.comment.isBlank()) getString(R.string.no_comment) else "\"${item.comment}\""
        binding.itemPrice.text = if (item.price == 0.0) getString(R.string.no_price) else "$${item.price}"
        binding.addressTextView.text = item.address?.ifBlank { getString(R.string.no_address) }
//          setupCommentsSection(item)


        if (item.link.isNotEmpty()) {
            binding.itemTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700))
            binding.itemTitle.paint.isUnderlineText = true
            binding.itemTitle.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                startActivity(intent)
            }
        } else {
            binding.itemTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.itemTitle.paint.isUnderlineText = false
            binding.itemTitle.setOnClickListener(null)
        }

        if (!item.photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(item.photo)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.baseline_hide_image_24)
                .into(binding.itemImage)
        } else {
            binding.itemImage.setImageResource(R.drawable.baseline_hide_image_24)
        }


        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index < item.rating) R.drawable.star_full else R.drawable.star_empty
            )
        }

        setupCategoryText(item.category)


        if (item.address.isNullOrEmpty()) {
            binding.addressTextView.visibility = View.GONE
            binding.showAddressButton.visibility = View.GONE
            binding.locationIcon.visibility = View.GONE
        } else {
            binding.addressTextView.visibility = View.VISIBLE
            binding.showAddressButton.visibility = View.VISIBLE
            binding.locationIcon.visibility = View.VISIBLE
        }

        binding.showAddressButton.setOnClickListener {
            if (!item.address.isNullOrEmpty()) {
                val bundle = bundleOf("address" to item.address)
                findNavController().navigate(R.id.action_itemDetailsFragment_to_mapFragment, bundle)
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.no_address_to_check), Toast.LENGTH_SHORT).show()
            }
        }
        setupCommentsSection(item)
        }

    private fun setupCategoryText(categoryString: String) {
        val categoryIds = categoryString.split(",").mapNotNull { it.toIntOrNull() }
        val localizedCategories = categoryIds.map { CategoryMapper.getLocalizedCategory(it, requireContext()) }

        val formattedCategories = if (localizedCategories.isEmpty()) {
            getString(R.string.no_category)
        } else {
            localizedCategories.joinToString(" | ")
        }
        binding.itemCategory.text = formattedCategories
    }
    private fun setupCommentsSection(item: Item) {
        binding?.let { binding ->
            val commentsRecyclerView = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
            val commentInput = binding.root.findViewById<EditText>(R.id.comment_input)
            val addCommentButton = binding.root.findViewById<Button>(R.id.add_comment_button)

            commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            commentsAdapter = CommentsAdapter(item.comments.toMutableList())
            commentsRecyclerView.adapter = commentsAdapter


            viewModel.getItemById(item.id).observe(viewLifecycleOwner) { updatedItem ->
                val updatedComments = updatedItem?.comments ?: emptyList()
                commentsAdapter.updateComments(updatedComments.toMutableList())
            }

            addCommentButton.setOnClickListener {
                val newComment = commentInput.text.toString().trim()
                if (newComment.isNotEmpty()) {
                    val currentItem = viewModel.chosenItem.value ?: return@setOnClickListener


                    viewModel.getUsername().observe(viewLifecycleOwner) { userName ->
                        val commentsList = currentItem.comments.toMutableList()


                        println("🔥 DEBUG: Username for comment: $userName")


                        val commentWithUserName = "${userName ?: "אנונימי"}: $newComment"
                        commentsList.add(commentWithUserName)

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.updateItemComments(currentItem.id, commentsList)
                        }

                        viewModel.refreshItemComments(currentItem.id)
                        commentInput.text.clear()
                    }
                }
            }

            commentInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    commentsRecyclerView.postDelayed({
                        commentsRecyclerView.scrollToPosition(commentsAdapter.itemCount - 1)
                    }, 200)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
