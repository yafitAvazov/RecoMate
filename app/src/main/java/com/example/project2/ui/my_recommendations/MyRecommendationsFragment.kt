package com.example.project2.ui.my_recommendations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.MyRecommendationLayoutBinding
import com.example.project2.ui.all_recommendation.ItemAdapter
import com.example.project2.ui.all_recommendation.RecommendationListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyRecommendationsFragment : Fragment() {
    private var _binding: MyRecommendationLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationListViewModel by viewModels()
    private lateinit var adapter: ItemAdapter // ✅ משתנה לשמירת המתאם

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyRecommendationLayoutBinding.inflate(inflater, container, false)
        return binding.root
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeRecyclerView()
        observeUserItems()
        viewModel.fetchUserItems() // 🔥 קריאה להמלצות של המשתמש בלבד


        binding.actionDelete.setOnClickListener {
            showDeleteAllConfirmationDialog()

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // טיפול בלחיצה אחורה לעדכון הנוויגיישן באר וחזרה לכל ההמלצות
            findNavController().navigate(R.id.allItemsFragment)

            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                ?.selectedItemId = R.id.nav_all_recommendation
        }
//        binding.actionDelete.setOnClickListener {
//            lastSelectedItem?.let { item ->
//                showDeleteItemConfirmationDialog(item)
//            } ?: Toast.makeText(requireContext(), "No item selected!", Toast.LENGTH_SHORT).show()
//        }


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

    private fun initializeRecyclerView() {
        adapter = ItemAdapter(emptyList(), object : ItemAdapter.ItemListener {
            override fun onItemClicked(index: Int) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.long_click_for_details),
                    Toast.LENGTH_SHORT).show()}
            override fun onItemLongClicked(index: Int) {
                val item = adapter.items[index]
                val bundle = bundleOf("itemId" to item.id)
                findNavController().navigate(R.id.action_allItemsFragment_to_itemDetailsFragment, bundle)
            }

            override fun onItemDeleted(item: Item) {
                try {
                    viewModel.deleteItem(item) // 🔥 Deletes from Firestore & Local DB

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
                    viewModel.updateLikeStatus(item.id, currentUserId) // ✅ Update Firestore
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to like item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onItemUnliked(item: Item) {
                try {
                    val currentUserId = viewModel.getCurrentUserId() ?: throw Exception("User not logged in")
                    viewModel.updateLikeStatus(item.id, currentUserId) // ✅ Update Firestore
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to unlike item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }



        })

        binding.recycler.layoutManager = LinearLayoutManager(requireContext()) // ✅ מגדיר רשימה אנכית
        binding.recycler.adapter = adapter
    }

    private fun observeUserItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userItems.collectLatest { itemList ->
                adapter.updateList(itemList) // ✅ עדכון הרשימה עם הנתונים החדשים
            }
        }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
