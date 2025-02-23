package com.example.project2.ui.all_recommendation

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.addCallback
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllItemsFragment : Fragment() {
    private var _binding: AllRecommendationsLayoutBinding? = null
    private val binding get() = _binding!!
    private var selectedMaxPrice: Int = 1000
    private var selectedRating: Int = 0
    private var selectedSort: String? = null
    private var selectedSortButton: Button? = null


    private var showingUserItems = false

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
        viewModel.fetchItems()
        viewModel.fetchUserItems()
        viewModel.fetchUserFavorites()

        binding.topItemsButton.setOnClickListener {
            viewModel.fetchTopLikedItems()
        }






        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            findNavController().navigate(R.id.categoriesFragment)

            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                ?.selectedItemId = R.id.nav_categories
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchItems()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
                deleteAllItems()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
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
                try {
                    viewModel.deleteItem(item)

                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            viewModel.fetchItems()
                            viewModel.fetchUserItems()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error updating list: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Toast.makeText(requireContext(), getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to delete item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onItemLiked(item: Item) {
                try {
                    val currentUserId = viewModel.getCurrentUserId() ?: throw Exception("User not logged in")
                    viewModel.updateLikeStatus(item.id, currentUserId)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to like item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onItemUnliked(item: Item) {
                try {
                    val currentUserId = viewModel.getCurrentUserId() ?: throw Exception("User not logged in")
                    viewModel.updateLikeStatus(item.id, currentUserId)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to unlike item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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


        val sortPriceAscButton = view.findViewById<Button>(R.id.sort_price_asc_button)
        val sortPriceDescButton = view.findViewById<Button>(R.id.sort_price_desc_button)
        val sortStarsDescButton = view.findViewById<Button>(R.id.sort_stars_desc_button)


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


            }
        }



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
                selectedMaxPrice = progress
                binding.minPrice.text = "$$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



        sortPriceAscButton.setOnClickListener {
            handleSortSelection(sortPriceAscButton, "price_asc")
        }

        sortPriceDescButton.setOnClickListener {
            handleSortSelection(sortPriceDescButton, "price_desc")
        }

        sortStarsDescButton.setOnClickListener {
            handleSortSelection(sortStarsDescButton, "stars_desc")
        }



        applyButton.setOnClickListener {
            applyFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        resetFilterButton.setOnClickListener {
            resetFilters()
        }
    }

    private fun handleSortSelection(button: Button, sortType: String) {

        selectedSortButton?.setBackgroundColor(resources.getColor(R.color.gray))


        selectedSort = sortType
        selectedSortButton = button
        button.setBackgroundColor(resources.getColor(R.color.purple_500))

    }


    private fun applyFilters() {

        viewModel.fetchFilteredItems(selectedRating, selectedMaxPrice.toDouble())


        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(300)
            selectedSort?.let { sortType ->
                viewModel.fetchSortedItems(sortType)
            }
        }

        selectedSortButton?.setBackgroundColor(resources.getColor(R.color.gray))
        selectedSortButton = null
    }


    private fun resetFilters() {
        println("🔥 DEBUG: Remove Filter button clicked!")

        selectedRating = 0
        selectedMaxPrice = 1000
        binding.priceSeekBar.progress = 1000
        binding.minPrice.text = getString(R.string._1000)

        val sortButtons = listOf(
            binding.sortPriceAscButton,
            binding.sortPriceDescButton,
            binding.sortStarsDescButton
        )
        sortButtons.forEach { it.setBackgroundColor(resources.getColor(R.color.gray)) }

        selectedSort = null
        selectedSortButton = null

        viewModel.fetchItems()


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collectLatest { itemList ->
                adapter.updateList(itemList)
                println("🔥 DEBUG: Adapter updated with ${itemList.size} items after reset")
            }
        }

        viewModel.clearTopLikedItems()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


