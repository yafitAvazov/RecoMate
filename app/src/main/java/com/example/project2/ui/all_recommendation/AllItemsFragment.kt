package com.example.project2.ui.all_recommendation

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.databinding.AllRecommendationsLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllItemsFragment : Fragment() {
    private var _binding: AllRecommendationsLayoutBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableSetOf<String>()
    private var selectedMinPrice: Int = 0
    private var selectedRating: Int = 0

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

        initializeRecyclerView()
        setupDrawerFilters(view)
        setupItemSwipeHandling()

        observeViewModel()
        viewModel.fetchItems() // טוען את הנתונים בתחילת הצגת המסך
        viewModel.fetchItems() // טוען את הנתונים בתחילת הצגת המסך

        binding.actionDelete.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // ✅ מאפשר הצגת תפריט
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_sign_out) {
            viewModel.signOut()
            findNavController().navigate(R.id.action_allItemsFragment_to_loginFragment)
        }
        return super.onOptionsItemSelected(item)
    }


    private fun deleteAllItems() {
        viewModel.deleteAll()

        // 🟢 עדכון LiveData כדי להפעיל את ה-Observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collectLatest { itemList ->
                adapter.updateList(itemList)
                binding.recycler.scrollToPosition(0) // ✅ גלילה לראש הרשימה לאחר מחיקה
            }
        }

        Toast.makeText(requireContext(), getString(R.string.all_items_deleted), Toast.LENGTH_SHORT).show()
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
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { // ✅ רק כאשר ה-Fragment פעיל
                viewModel.items.collectLatest { itemList ->
                    binding?.let { binding ->
                        binding.progressBar.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                        adapter.updateList(itemList)
                    }
                }}



//            launch {
//                viewModel.filteredItems.collectLatest { filteredList ->
//                    binding.progressBar.visibility = View.GONE
//                    binding.recycler.visibility = View.VISIBLE
//                    adapter.updateList(filteredList)
//                }
//            }
        }
    }


    private fun initializeRecyclerView() {
        adapter = ItemAdapter(emptyList(), object : ItemAdapter.ItemListener {
            override fun onItemClicked(index: Int) {
                val clickedItem = adapter.items[index]
                Toast.makeText(requireContext(), clickedItem.title, Toast.LENGTH_SHORT).show()
            }

            override fun onItemLongClicked(index: Int) {
                val item = adapter.items[index]
                val bundle = bundleOf("itemId" to item.id)
                findNavController().navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
            }
        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
    }


    private fun setupDrawerFilters(view: View) {
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout) // ✅ כעת משתמש ב-ID הנכון
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
            drawerLayout.openDrawer(GravityCompat.END) // ✅ תפריט מצד ימין (לפי `layout_gravity="end"`)
        }

        applyButton.setOnClickListener {
            applyFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        resetFilterButton.setOnClickListener {
            resetFilters()
        }
    }

    private fun applyFilters() {
        viewModel.fetchFilteredItems(
            selectedCategories.joinToString(", "),
            selectedRating,
            selectedMinPrice.toDouble()
        )
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

        viewModel.fetchItems()
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
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    }
                    .setNegativeButton(getString(R.string.no)) { _, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
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

