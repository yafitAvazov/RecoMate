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

        initializeRecyclerView()
        setupDrawerFilters(view)
        observeViewModel()
        viewModel.fetchItems() // ✅ מביא את כל ההמלצות
        viewModel.fetchUserItems() // ✅ מביא את ההמלצות של המשתמש המחובר
        viewModel.fetchUserFavorites() // ✅ מביא את רשימת המועדפים של המשתמש

        binding.actionDelete.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // ✅ מאפשר הצגת תפריט
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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

        Toast.makeText(requireContext(), getString(R.string.all_items_deleted), Toast.LENGTH_SHORT).show()
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
            viewModel.items.collectLatest { itemList ->
                binding.progressBar.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                adapter.updateList(itemList)
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userItems.collectLatest { userItemList ->
                if (showingUserItems) {
                    binding.progressBar.visibility = View.GONE
                    binding.recycler.visibility = View.VISIBLE
                    adapter.updateList(userItemList)
                }
            }
        }

        // ✅ מעקב אחר רשימת המועדפים של המשתמש ועדכון התצוגה
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userFavorites.collectLatest { favoriteItems ->
                binding.progressBar.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                adapter.updateList(favoriteItems) // 🔥 עכשיו המועדפים מתעדכנים נכון!
            }
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
            override fun onItemDeleted(item: Item) {
                viewModel.deleteItem(item) // 🔥 מוחק מה-DB המקומי ומה-Firebase

                viewLifecycleOwner.lifecycleScope.launch {
                    // 🔥 מחכים שהמחיקה תסתיים ואז מעדכנים את הרשימה
                    viewModel.fetchItems()
                    viewModel.fetchUserItems()
                }

                Toast.makeText(requireContext(), "Recommendation deleted!", Toast.LENGTH_SHORT).show()
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

//    private fun showDeleteItemConfirmationDialog(item: Item) {
//        AlertDialog.Builder(requireContext())
//            .setTitle(getString(R.string.delete_confirmation)) // 🔥 כותרת
//            .setMessage(getString(R.string.delete_confirmation_message)) // ✅ תוכן ההודעה
//            .setPositiveButton(getString(R.string.yes)) { _, _ ->
//                viewModel.deleteItem(item) // ✅ מוחק מה-DB
//                Toast.makeText(requireContext(), getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
//            }
//            .setNegativeButton(getString(R.string.no), null) // ❌ אם המשתמש לוחץ "לא", הדיאלוג פשוט נסגר
//            .show()
//    }
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

//        applyButton.setOnClickListener {
//            applyFilters()
//            drawerLayout.closeDrawer(GravityCompat.END)
//        }

        resetFilterButton.setOnClickListener {
            resetFilters()
        }
    }

//    private fun applyFilters() {
//        viewModel.fetchFilteredItems(
//            selectedCategories.joinToString(", "),
//            selectedRating,
//            selectedMinPrice.toDouble()
//        )
//    }

    private fun resetFilters() {
        selectedCategories.clear()
        selectedRating = 0
        selectedMinPrice = 0

        binding.priceSeekBar.progress = 0
        binding.minPrice.text = getString(R.string._0)

        viewModel.fetchItems()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
