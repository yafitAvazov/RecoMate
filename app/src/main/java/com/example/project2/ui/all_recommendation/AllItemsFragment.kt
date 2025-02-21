package com.example.project2.ui.all_recommendation

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.AllRecommendationsLayoutBinding
import com.example.project2.ui.recommendation_detail.RecommendationDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllItemsFragment : Fragment() {
    private var _binding: AllRecommendationsLayoutBinding? = null
    private val binding get() = _binding!!
    private var selectedMaxPrice: Int = 1000
    private var selectedRating: Int = 0
    private var selectedSort: String? = null // Track selected sort type
    private var selectedSortButton: Button? = null // Track selected button for color change


    private var showingUserItems = false // ✅ משתנה שמנהל האם להציג את הפריטים של המשתמש בלבד
    private val viewModel: RecommendationListViewModel by viewModels()
    private lateinit var adapter: ItemAdapter




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AllRecommendationsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStarRating()

        initializeRecyclerView()
        setupDrawerFilters(view)
        observeViewModel()
        viewModel.fetchItems() // ✅ מביא את כל ההמלצות
        viewModel.fetchUserItems() // ✅ מביא את ההמלצות של המשתמש המחובר
        viewModel.fetchUserFavorites() // ✅ מביא את רשימת המועדפים של המשתמש



//        binding.actionDelete.setOnClickListener {
//            showDeleteAllConfirmationDialog()
//        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchItems()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // ✅ מאפשר הצגת תפריט
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupStarRating() {
        val stars = listOf(
            binding.star1Filter to 1,
            binding.star2Filter to 2,
            binding.star3Filter to 3,
            binding.star4Filter to 4,
            binding.star5Filter to 5
        )

        stars.forEach { (starView, rating) ->
            starView.setOnClickListener {
                selectedRating = rating
                updateStars(rating)
                Toast.makeText(requireContext(),
                    getString(R.string.minimum_rating_set,selectedRating.toString()), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStars(selected: Int) {
        val starImages = listOf(
            binding.star1Filter,
            binding.star2Filter,
            binding.star3Filter,
            binding.star4Filter,
            binding.star5Filter
        )

        starImages.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index < selected) R.drawable.star_full else R.drawable.star_empty
            )
        }
    }


    //    private fun deleteAllItems() {
//        viewModel.deleteAll()
//    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sign_out) {
            viewModel.signOut()
            findNavController().navigate(R.id.action_allItemsFragment_to_loginFragment)
        }
        return super.onOptionsItemSelected(item)
    }


    private fun updateItemList() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (showingUserItems) {
                viewModel.userItems.collectLatest { userItemList ->
                    adapter.updateList(userItemList)
                }
            } else {
                viewModel.items.collectLatest { itemList ->
                    adapter.updateList(itemList)
                }
            }
        }
    }

    private fun deleteAllItems() {
        viewModel.deleteAllUserItems()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userItems.collectLatest { userItemList ->
                adapter.updateList(userItemList)
                binding.recycler.scrollToPosition(0)
            }
        }

        Toast.makeText(
            requireContext(),
            getString(R.string.all_items_deleted),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDeleteAllConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirmation))
            .setMessage(getString(R.string.all_delete_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteAllItems() // ✅ קריאה לפונקציה שמוחקת את כל הפריטים
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collectLatest { itemList ->
                    binding.progressBar.visibility = View.GONE
                    binding.recycler.visibility = View.VISIBLE
                    adapter.updateList(itemList) // ✅ Ensure all items are updated, not just liked ones
                }
            }
        }
    }




    private fun initializeRecyclerView() {
        adapter = ItemAdapter(emptyList(), object : ItemAdapter.ItemListener {

            override fun onItemClicked(index: Int) {
                val item = adapter.items[index]
                Toast.makeText(requireContext(),
                    getString(R.string.long_click_for_details), Toast.LENGTH_SHORT).show()            }
            override fun onItemLongClicked(index: Int) {
                val item = adapter.items[index]
                val bundle = bundleOf("itemId" to item.id)
                findNavController().navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
            }


            override fun onItemDeleted(item: Item) {
                viewModel.deleteItem(item) // 🔥 מוחק מה-DB המקומי ומה-Firebase

                viewLifecycleOwner.lifecycleScope.launch {
                    // 🔥 מחכים שהמחיקה תסתיים ואז מעדכנים את הרשימה
                    viewModel.fetchItems()
                    viewModel.fetchUserItems()
                }

                Toast.makeText(requireContext(), getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onItemLiked(item: Item) {
                viewModel.updateLikeStatus(item.id, true) // 🔥 שומר את הלייק
            }

            override fun onItemUnliked(item: Item) {
                viewModel.updateLikeStatus(item.id, false) // 🔥 מסיר מהמועדפים
            }
        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
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

        // Setup sort button listeners
        val sortButtons = mapOf(
            sortPriceAscButton to "price_asc",
            sortPriceDescButton to "price_desc",
            sortStarsDescButton to "stars_desc"
        )

        sortButtons.forEach { (button, sortKey) ->
            button.setOnClickListener {
                // Track selected sort
                selectedSort = sortKey

                // Reset all buttons to default color
                sortButtons.keys.forEach { it.setBackgroundColor(resources.getColor(R.color.gray)) }

                // Highlight the selected button
                button.setBackgroundColor(resources.getColor(R.color.purple_500))


            }
        }


        // Open drawer on filter icon click
        filterButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        binding.priceSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                selectedMaxPrice = progress  // Corrected variable name
                binding.minPrice.text = "$$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        // Sorting actions with color change
        sortPriceAscButton.setOnClickListener {
            handleSortSelection(sortPriceAscButton, "price_asc")
        }

        sortPriceDescButton.setOnClickListener {
            handleSortSelection(sortPriceDescButton, "price_desc")
        }

        sortStarsDescButton.setOnClickListener {
            handleSortSelection(sortStarsDescButton, "stars_desc")
        }


        // Apply filters with current selections
        applyButton.setOnClickListener {
            applyFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        resetFilterButton.setOnClickListener {
            resetFilters()
        }
    }

    private fun handleSortSelection(button: Button, sortType: String) {
        // Reset previous button color if any
        selectedSortButton?.setBackgroundColor(resources.getColor(R.color.gray))

        // Set new selected button
        selectedSort = sortType
        selectedSortButton = button
        button.setBackgroundColor(resources.getColor(R.color.purple_500))

    }


    private fun applyFilters() {
        // Fetch the filtered items first
        viewModel.fetchFilteredItems(selectedRating, selectedMaxPrice.toDouble())

        // Delay the sorting to give time for the filter operation to complete
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(300) // Adjust time if needed
            selectedSort?.let { sortType ->
                viewModel.fetchSortedItems(sortType)
            }
        }

        // Reset button color after apply
        selectedSortButton?.setBackgroundColor(resources.getColor(R.color.gray))
        selectedSortButton = null
    }


    private fun resetFilters() {
        selectedRating = 0
        selectedMaxPrice = 1000
        binding.priceSeekBar.progress = 1000
        binding.minPrice.text = getString(R.string._1000)
        // Reset sort button colors
        val sortButtons = listOf(
            binding.sortPriceAscButton,
            binding.sortPriceDescButton,
            binding.sortStarsDescButton
        )

        // Reset color for all sort buttons
        sortButtons.forEach { it.setBackgroundColor(resources.getColor(R.color.gray)) }

        // Reset selected sort
        selectedSort = null
        selectedSortButton = null
        viewModel.fetchItems()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


