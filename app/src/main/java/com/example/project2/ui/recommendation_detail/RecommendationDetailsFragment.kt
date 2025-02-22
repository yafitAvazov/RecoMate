package com.example.project2.ui.recommendation_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    @SuppressLint("SetTextI18n")
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

        itemId = arguments?.getString("itemId") // ✅ ה-ID מתקבל כמחרוזת ישירות
        itemId?.let { viewModel.fetchItemById(it) } // ✅ שולחת ל-ViewModel את ה-ID
        observeViewModel()
    }



    private fun observeViewModel() {
        viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
            item?.let { updateUI(it) }
        }
    }

    private fun updateUI(item: Item) {
        binding.itemTitle.text = item.title.ifBlank { getString(R.string.no_title) }
        binding.itemComment.text = if (item.comment.isBlank()) getString(R.string.no_comment) else "\"${item.comment}\""
        binding.itemPrice.text = if (item.price == 0.0) getString(R.string.no_price) else "$${item.price}"
        binding.addressTextView.text = item.address?.ifBlank { getString(R.string.no_address) }
//        setupCommentsSection(item)

        // קישור ולחיצה עליו
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

        if (item.photo.isNullOrEmpty()) {
            binding.itemImage.setImageResource(R.drawable.baseline_hide_image_24)
        } else {
            binding.itemImage.setImageURI(Uri.parse(item.photo))
        }

        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index < item.rating) R.drawable.star_full else R.drawable.star_empty
            )
        }

        setupCategoryText(item.category) // שולח את המזהים לפונקציה שתתרגם


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
        val categoryIds = categoryString.split(",").mapNotNull { it.toIntOrNull() } // המרת מזהים לרשימה של מספרים
        val localizedCategories = categoryIds.map { CategoryMapper.getLocalizedCategory(it, requireContext()) }

        val formattedCategories = if (localizedCategories.isEmpty()) {
            getString(R.string.no_category)
        } else {
            localizedCategories.joinToString(" | ") // מציג שמות קטגוריות מופרדות עם "|"
        }
        binding.itemCategory.text = formattedCategories
    }

    private fun setupCommentsSection(item: Item) {
        binding?.let { binding -> // ✅ בדיקה שה-Binding עדיין קיים
            val commentsRecyclerView = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
            val commentInput = binding.root.findViewById<EditText>(R.id.comment_input)
            val addCommentButton = binding.root.findViewById<Button>(R.id.add_comment_button)

            commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            commentsAdapter = CommentsAdapter(mutableListOf())
            commentsRecyclerView.adapter = commentsAdapter

            viewModel.chosenItem.observe(viewLifecycleOwner) { item: Item? ->
                val commentsList = item?.comments ?: emptyList()
                commentsAdapter.updateComments(commentsList.toMutableList())
            }

            addCommentButton.setOnClickListener {
                val newComment = commentInput.text.toString().trim()
                if (newComment.isNotEmpty()) {
                    val currentItem = viewModel.chosenItem.value ?: return@setOnClickListener
                    val commentsList = currentItem.comments.toMutableList().apply { add(newComment) }

                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.updateItemComments(currentItem, commentsList)
                    }


                    commentsAdapter.updateComments(commentsList)
                    commentInput.text.clear()
                }
            }
        }
    }

//    private fun setupCommentsSection(item: Item) {
//        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        commentsAdapter = CommentsAdapter(mutableListOf())
//        binding.commentsRecyclerView.adapter = commentsAdapter
//
//        viewModel.chosenItem.observe(viewLifecycleOwner) { itemData ->
//            commentsAdapter.updateComments(itemData?.comments?.toMutableList() ?: mutableListOf())
//        }
//
//        binding.addCommentButton.setOnClickListener {
//            val newComment = binding.commentInput.text.toString().trim()
//            if (newComment.isNotEmpty()) {
//                val commentsList = item.comments.toMutableList().apply { add(newComment) }
//                viewModel.updateItemComments(item, commentsList)
//                commentsAdapter.updateComments(commentsList)
//                binding.commentInput.text.clear()
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
