package com.example.project2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2.R
import com.example.project2.data.model.Item
import com.example.project2.databinding.FavoriteRecommendationLayoutBinding
import com.example.project2.ui.all_recommendation.ItemAdapter
import com.example.project2.ui.all_recommendation.RecommendationListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private var _binding: FavoriteRecommendationLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationListViewModel by viewModels()
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoriteRecommendationLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeRecyclerView()
        observeFavoriteItems()

        viewModel.fetchUserFavorites() // ✅ הבאת רשימת המועדפים
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // טיפול בלחיצה אחורה לעדכון הנוויגיישן באר וחזרה לכל ההמלצות
            findNavController().navigate(R.id.allItemsFragment)

            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                ?.selectedItemId = R.id.nav_all_recommendation
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
                val bundle = Bundle().apply {
                    putParcelable("item", item)
                }
                findNavController().navigate(R.id.action_favoritesFragment_to_itemDetailsFragment, bundle)
            }

            override fun onItemDeleted(item: Item) {
                viewModel.updateLikeStatus(item.id, false) // ❌ מסיר מהמועדפים
            }


            override fun onItemLiked(item: Item) {
                viewModel.updateLikeStatus(item.id, true)
            }

            override fun onItemUnliked(item: Item) {
                viewModel.updateLikeStatus(item.id, false)
            }

        })

        binding.recyclerMyFav.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyFav.adapter = adapter
    }

    private fun observeFavoriteItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userFavorites.collectLatest { favoriteItems ->
                adapter.updateList(favoriteItems) // 🔥 רק הפריטים עם `isLiked = true`
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
