package com.example.project2.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle


@AndroidEntryPoint
class SpecificCategoryItemsFragment : Fragment() {

    private var _binding: SpecificCategoryItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationListViewModel by activityViewModels()
    private lateinit var adapter: ItemAdapter
    private lateinit var commentsAdapter: CommentsAdapter


    private var categoryName: String? = null
    private var categoryImageResId = 0

    private var selectedMaxPrice: Int = 1000
    private var selectedRating: Int = 0
    private var selectedSort: String? = null
    private var selectedSortButton: Button? = null


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


        setupDrawerFilters(view)



        loadItems(categoryName ?: "")

        observeViewModel()



        binding.topItems.setOnClickListener {
            val categoryKey = CategoryMapper.getCategoryId(categoryName ?: "", requireContext()).toString()
            viewModel.fetchTopLikedItemsByCategory(categoryKey) // ✅ Fetch top 5 liked items for the category
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {


                launch {
                    viewModel.items.collectLatest { itemList ->
                        binding.progressBar.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                        adapter.updateList(itemList)
                    }
                }


                launch {
                    viewModel.topLikedItems.collectLatest { topItemList ->
                        if (topItemList.isNotEmpty()) {
                            adapter.updateList(topItemList)
                        }
                    }
                }
            }
        }
    }

    private fun setupDrawerFilters(view: View) {
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout)
        val filterButton = view.findViewById<ImageView>(R.id.filter_button)
        val resetFilterButton = view.findViewById<Button>(R.id.reset_filter_button)
        val applyButton = view.findViewById<Button>(R.id.apply_button)

        // Sorting buttons
        val sortPriceAscButton = view.findViewById<Button>(R.id.sort_price_asc_button)
        val sortPriceDescButton = view.findViewById<Button>(R.id.sort_price_desc_button)
        val sortStarsDescButton = view.findViewById<Button>(R.id.sort_stars_desc_button)

        // Track selected values
        var selectedMaxPrice = 1000
        var selectedRating = 0
        var selectedSort: String? = null
        var selectedSortButton: Button? = null

        // Open drawer when filter button is clicked
        filterButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // SeekBar for price range
        binding.priceSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedMaxPrice = progress
                binding.minPrice.text = "$$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Sorting actions
        val sortButtons = mapOf(
            sortPriceAscButton to "price_asc",
            sortPriceDescButton to "price_desc",
            sortStarsDescButton to "stars_desc"
        )

        sortButtons.forEach { (button, sortKey) ->
            button.setOnClickListener {
                selectedSort = sortKey
                sortButtons.keys.forEach { it.setBackgroundColor(resources.getColor(R.color.gray)) }
                button.setBackgroundColor(resources.getColor(R.color.purple_500))
                selectedSortButton = button
            }
        }

        // Apply filters
        applyButton.setOnClickListener {
            applyCategoryFilters(selectedRating, selectedMaxPrice.toDouble(), selectedSort)
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Reset filters
        resetFilterButton.setOnClickListener {
            resetCategoryFilters()
        }
    }

    private fun applyCategoryFilters(minRating: Int, maxPrice: Double, sortBy: String?) {
        val categoryKey = CategoryMapper.getCategoryId(categoryName ?: "", requireContext()).toString()

        // Use RecommendationListViewModel instead of CategoryItemsViewModel
        viewModel.fetchFilteredCategoryItems(categoryKey, minRating, maxPrice)

        // Delay sorting to ensure filtering completes first
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(300)
            sortBy?.let { viewModel.fetchSortedCategoryItems(it, categoryKey) }
        }
    }


    private fun resetCategoryFilters() {
        val categoryKey = CategoryMapper.getCategoryId(categoryName ?: "", requireContext()).toString()
        viewModel.fetchItemsByCategory(categoryKey)

        // Reset UI selections
        selectedRating = 0
        selectedMaxPrice = 1000
        binding.priceSeekBar.progress = 1000
        binding.minPrice.text = getString(R.string._1000)

        selectedSort = null
        selectedSortButton = null

        val sortButtons = listOf(
            binding.sortPriceAscButton,
            binding.sortPriceDescButton,
            binding.sortStarsDescButton
        )
        sortButtons.forEach { it.setBackgroundColor(resources.getColor(R.color.gray)) }

        viewModel.clearTopLikedItems()
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
                try {
                    viewModel.deleteItem(item) // Deletes from Firestore & Local DB

                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            viewModel.fetchItems()
                            viewModel.fetchUserItems()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(),
                                getString(R.string.error_updating_list, e.message), Toast.LENGTH_SHORT).show()
                        }
                    }

                    Toast.makeText(requireContext(), getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        getString(R.string.failed_to_delete_item, e.message), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onItemLiked(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId)
            }

            override fun onItemUnliked(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId)
            }

        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())


    }

    private fun loadItems(category: String) {
        val categoryKey = CategoryMapper.getCategoryId(category, requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.fetchItemsByCategory(categoryKey.toString())

                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED)
                    //coroutine paused when not visible
                {
                    viewModel.items.collectLatest { items ->
                        adapter.updateList(items)
                    }
                }
            } catch (e: Exception) {
                if (isAdded) { // Ensures the Fragment is still attached before showing Toast
                    Toast.makeText(requireContext(),
                        getString(R.string.error_loading_items, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun setupCommentsSection(item: Item) {
        binding?.let { binding -> //  בדיקה שה-Binding עדיין קיים
            val commentsRecyclerView = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
            val commentInput = binding.root.findViewById<EditText>(R.id.comment_input)
            val addCommentButton = binding.root.findViewById<Button>(R.id.add_comment_button)

            commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            commentsAdapter = CommentsAdapter(mutableListOf())
            commentsRecyclerView.adapter = commentsAdapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.items.collectLatest { itemList ->
                    val currentItem = itemList.find { it.id == item.id } //  מחפש את הפריט ברשימה לפי ID
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
