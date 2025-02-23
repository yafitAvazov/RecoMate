package com.example.project2.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.CategoryMapper
import com.example.project2.data.model.Item
import com.example.project2.databinding.SpecificCategoryItemsBinding
import com.example.project2.ui.CommentsAdapter
import com.example.project2.ui.all_recommendation.ItemAdapter
import com.example.project2.ui.all_recommendation.RecommendationListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpecificCategoryItemsFragment : Fragment() {

    private var _binding: SpecificCategoryItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationListViewModel by activityViewModels()
    private lateinit var adapter: ItemAdapter
    private lateinit var commentsAdapter: CommentsAdapter


    private var categoryName: String? = null
    private var categoryImageResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SpecificCategoryItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryName = arguments?.getString("categoryName")
        categoryImageResId = arguments?.getInt("categoryImage") ?: R.mipmap.ic_launcher

        binding.categoryImage.setImageResource(categoryImageResId)
        binding.categoryName.text = categoryName

        initializeRecyclerView()
        loadItems(categoryName ?: "")
    }

    private fun initializeRecyclerView() {
        adapter = ItemAdapter(emptyList(), object : ItemAdapter.ItemListener {
            override fun onItemClicked(index: Int) {
                val item = adapter.items[index]
                val bundle = bundleOf("itemId" to item.id)
                findNavController().navigate(R.id.action_specificCategoryItemsFragment_to_itemDetailsFragment, bundle)       }

            override fun onItemLongClicked(index: Int) {

            }

            override fun onItemDeleted(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return

                viewModel.deleteItem(item) // 🔥 מוחק מה-DB
                viewModel.updateLikeStatus(item.id, currentUserId)
                lifecycleScope.launch {
                    // 🔥 מחכים שהמחיקה תסתיים ואז מרעננים את הרשימה
                    categoryName?.let {
                        loadItems(categoryName?:"")
                    }
                }

                Toast.makeText(requireContext(), getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
                }
            override fun onItemLiked(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId) // ✅ Pass userId instead of "true"
            }

            override fun onItemUnliked(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId) // ✅ Pass userId instead of "false"
            }

        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())


    }

    private fun loadItems(category: String) {
        val categoryKey = CategoryMapper.getCategoryId(category,requireContext())
        lifecycleScope.launch {
            viewModel.fetchItemsByCategory(categoryKey.toString()) // ✅ Uses universal key
            viewModel.items.collectLatest { items ->
                adapter.updateList(items)
            }
        }
    }
    private fun setupCommentsSection(item: Item) {
        binding?.let { binding -> // ✅ בדיקה שה-Binding עדיין קיים
            val commentsRecyclerView = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
            val commentInput = binding.root.findViewById<EditText>(R.id.comment_input)
            val addCommentButton = binding.root.findViewById<Button>(R.id.add_comment_button)

            commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            commentsAdapter = CommentsAdapter(mutableListOf())
            commentsRecyclerView.adapter = commentsAdapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.items.collectLatest { itemList ->
                    val currentItem = itemList.find { it.id == item.id } // ✅ מחפש את הפריט ברשימה לפי ID
                    val commentsList = currentItem?.comments ?: emptyList()
                    commentsAdapter.updateComments(commentsList.toMutableList())
                }
            }


            addCommentButton.setOnClickListener {
                val newComment = commentInput.text.toString().trim()
                if (newComment.isNotEmpty()) {
                    val itemId = arguments?.getString("itemId") ?: return@setOnClickListener

                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val currentItem = viewModel.items.value.find { it.id == itemId } ?: return@launch
                        val commentsList = currentItem.comments.toMutableList().apply { add(newComment) }

                        viewModel.updateItemComments(currentItem, commentsList) // ✅ מעדכן במסד הנתונים

                        categoryName?.let {
                            viewModel.fetchItemsByCategory(CategoryMapper.getCategoryId(it, requireContext()).toString()) // ✅ רענון הרשימה
                        }
                        commentsAdapter.updateComments(commentsList) // ✅ עדכון התצוגה
                        commentInput.text.clear()
                    }
                }
            }

        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
