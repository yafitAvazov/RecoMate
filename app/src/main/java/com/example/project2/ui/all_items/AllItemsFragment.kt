package com.example.project2.ui.all_items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.AllRecommendationsLayoutBinding
import com.example.project2.ui.ItemsViewModel


class AllItemsFragment : Fragment() {
    private var _binding: AllRecommendationsLayoutBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableSetOf<String>() // Selected categories
    private lateinit var originalItems: List<Item>
    private var selectedMinPrice: Int = 0
    private var selectedRating: Int = 0

    private val viewModel: ItemsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AllRecommendationsLayoutBinding.inflate(inflater, container, false)
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_allItemsFragment_to_addItemFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("title")?.let {
            Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
        }

        // Initialize RecyclerView and setup filters
        initializeRecyclerView()
        setupDrawerFilters(view)
        setupItemSwipeHandling()

        // Observe LiveData from ViewModel
        viewModel.items?.observe(viewLifecycleOwner) { items ->
            originalItems = items
            binding.recycler.adapter = ItemAdapter(items, object : ItemAdapter.ItemListener {
                override fun onItemClicked(index: Int) {
                    val clickedItem = items[index]
                    Toast.makeText(requireContext(), clickedItem.title, Toast.LENGTH_SHORT).show()
                }

                override fun onItemLongClicked(index: Int) {
                    val item = items[index]
                    viewModel.setItem(item)
                    findNavController().navigate(R.id.action_allItemsFragment_to_itemDetailsFragment)
                }
            })
        }

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initializeRecyclerView() {
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupDrawerFilters(view: View) {
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout)
        val filterButton = view.findViewById<ImageView>(R.id.filter_button)
        val resetFilterButton = view.findViewById<Button>(R.id.reset_filter_button)
        val applyButton = view.findViewById<Button>(R.id.apply_button)

        binding.priceSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedMinPrice = progress
                binding.minPrice.text = "$$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        filterButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        applyButton.setOnClickListener {
            applyFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        resetFilterButton.setOnClickListener {
            resetFilters()
        }

        setupCategoryFilters(view)
    }

    private fun applyFilters() {
        val filteredItems = originalItems.filter { item ->
            val matchesCategory = selectedCategories.isEmpty() || selectedCategories.any { category ->
                item.category.contains(category, ignoreCase = true)
            }
            val matchesRating = selectedRating == 0 || item.rating >= selectedRating
            val matchesPrice = item.price <= selectedMinPrice

            matchesCategory && matchesRating && matchesPrice
        }
        (binding.recycler.adapter as ItemAdapter).updateList(filteredItems)
    }

    private fun resetFilters() {
        selectedCategories.clear()
        selectedRating = 0
        selectedMinPrice = 0

        binding.priceSeekBar.progress = 0
        binding.minPrice.text = "$0"

        (binding.recycler.adapter as ItemAdapter).updateList(originalItems)
    }

    private fun setupCategoryFilters(view: View) {
        val categoryCheckBoxes = listOf(
            view.findViewById<CheckBox>(R.id.checkbox_fashion),
            view.findViewById<CheckBox>(R.id.checkbox_food),
            view.findViewById<CheckBox>(R.id.checkbox_game),
            view.findViewById<CheckBox>(R.id.checkbox_home),
            view.findViewById<CheckBox>(R.id.checkbox_tech),
            view.findViewById<CheckBox>(R.id.checkbox_sport),
            view.findViewById<CheckBox>(R.id.checkbox_travel),
            view.findViewById<CheckBox>(R.id.checkbox_music),
            view.findViewById<CheckBox>(R.id.checkbox_book),
            view.findViewById<CheckBox>(R.id.checkbox_shops),
            view.findViewById<CheckBox>(R.id.checkbox_movie),
            view.findViewById<CheckBox>(R.id.checkbox_health)
        )

        categoryCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategories.add(checkBox.text.toString())
                } else {
                    selectedCategories.remove(checkBox.text.toString())
                }
            }
        }

        setupStarFilters()
    }

    private fun setupStarFilters() {
        val stars = listOf(
            binding.star1Filter,
            binding.star2Filter,
            binding.star3Filter,
            binding.star4Filter,
            binding.star5Filter
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1
                updateStarDisplay(index, stars)
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

    private fun setupItemSwipeHandling() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = makeFlag(
                ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = (binding.recycler.adapter as ItemAdapter).itemAt(viewHolder.adapterPosition)
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteItem(item)
                    }
                    .setNegativeButton("No") { _, _ ->
                        (binding.recycler.adapter as ItemAdapter).notifyItemChanged(viewHolder.adapterPosition)
                    }
                    .setCancelable(false)
                    .show()
            }
        }).attachToRecyclerView(binding.recycler)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

