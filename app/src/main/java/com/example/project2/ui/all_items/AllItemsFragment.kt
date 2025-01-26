package com.example.project2.ui.all_items

import android.os.Bundle
import android.view.*
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.AllRecommendationsLayoutBinding
import com.example.project2.ui.ItemsViewModel
import kotlinx.coroutines.launch
import androidx.navigation.fragment.navArgs

class AllItemsFragment : Fragment() {
    private var _binding: AllRecommendationsLayoutBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableSetOf<String>() // Selected categories
    private lateinit var originalItems: List<Item>
    private var selectedMinPrice: Int = 0
    private var selectedRating: Int = 0

    private val viewModel: ItemsViewModel by activityViewModels()
    private lateinit var adapter: ItemAdapter // משתנה אדפטר כדי להשתמש בו פעם אחת


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = AllRecommendationsLayoutBinding.inflate(inflater, container, false)

        // מעבר למסך הוספת פריט
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_allItemsFragment_to_addItemFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(getString(R.string.title))?.let {
            Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
        }

        // אתחול ה-RecyclerView וה-Adapter
        initializeRecyclerView()
        setupDrawerFilters(view)
        setupItemSwipeHandling()


        // אם כבר בוצע סינון, נציג את הפריטים המסוננים
        viewModel.filteredItems.observe(viewLifecycleOwner) { filteredItems ->
            if (filteredItems.isNotEmpty()) {
                adapter.updateList(filteredItems)
            } else {
                // אם אין פריטים מסוננים, נציג את כל הפריטים
                viewModel.items?.observe(viewLifecycleOwner) { items ->
                    originalItems = items
                    adapter.updateList(items)
                }
            }
        }

        // שמירה של רשימת הפריטים המקורית, למקרה שצריך איפוס סינון
        viewModel.items?.observe(viewLifecycleOwner) { items ->
            if (viewModel.filteredItems.value.isNullOrEmpty()) {
                originalItems = items
                adapter.updateList(items)
            }
        }
    }


    private fun initializeRecyclerView() {
        adapter = ItemAdapter(
            emptyList(),
            viewModel, // העברת ה-ViewModel לאדפטר
            object : ItemAdapter.ItemListener {
                override fun onItemClicked(index: Int) {
                    val clickedItem = adapter.items[index]
                    Toast.makeText(requireContext(), clickedItem.title, Toast.LENGTH_SHORT).show()
                }

                override fun onItemLongClicked(index: Int) {
                    val item = adapter.items[index]
                    viewModel.setItem(item)
                    findNavController().navigate(R.id.action_allItemsFragment_to_itemDetailsFragment)
                }
            }
        )

        binding.recycler.adapter = adapter
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
            adapter.updateList(originalItems)
        }

        setupCategoryFilters(view)
    }

    private fun applyFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            val filteredItems = (if (selectedRating == 0) null else selectedRating)?.let {
                viewModel.getFilteredItems(
                    selectedCategories.joinToString(", "),
                    it,
                    selectedMinPrice.toDouble()
                )
            }
            if (filteredItems != null) {
                adapter.updateList(filteredItems)
            }
            Toast.makeText(requireContext(), "${filteredItems?.size} ${getString(R.string.items_found)}", Toast.LENGTH_SHORT).show()
        }
    }






    private fun resetFilters() {
        selectedCategories.clear()
        selectedRating = 0
        selectedMinPrice = 0

        binding.priceSeekBar.progress = 0
        binding.minPrice.text = getString(R.string._0)

        val checkBoxes = listOf(
            binding.checkboxFashion,
            binding.checkboxFood,
            binding.checkboxGame,
            binding.checkboxHome,
            binding.checkboxTech,
            binding.checkboxSport,
            binding.checkboxTravel,
            binding.checkboxMusic,
            binding.checkboxBook,
            binding.checkboxShops,
            binding.checkboxMovie,
            binding.checkboxHealth
        )
        checkBoxes.forEach { it.isChecked = false }

        val stars = listOf(
            binding.star1Filter,
            binding.star2Filter,
            binding.star3Filter,
            binding.star4Filter,
            binding.star5Filter
        )
        stars.forEach { it.setImageResource(R.drawable.star_empty) }

        if (::originalItems.isInitialized) {
            adapter.updateList(originalItems)
        }

        Toast.makeText(requireContext(), getString(R.string.Filters_removed), Toast.LENGTH_SHORT).show()
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
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.itemAt(viewHolder.adapterPosition)
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_confirmation))
                    .setMessage(getString(R.string.delete_confirmation_message))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.deleteItem(item)
                    }
                    .setNegativeButton(getString(R.string.no)) { _, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    .setCancelable(false)
                    .show()
            }
        }).attachToRecyclerView(binding.recycler)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_confirmation))
                .setMessage(getString(R.string.all_delete_confirmation_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewModel.deleteAll()
                    Toast.makeText(requireContext(), getString(R.string.all_items_deleted), Toast.LENGTH_SHORT).show()
                }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
