package com.example.project2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        // ✅ Fetch favorites when fragment is created
        viewModel.fetchUserFavorites()

        observeFavoriteItems()
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
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId) // ✅ Pass `userId` instead
            }

            override fun onItemLiked(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId) // ✅
            }

            override fun onItemUnliked(item: Item) {
                val currentUserId = viewModel.getCurrentUserId() ?: return
                viewModel.updateLikeStatus(item.id, currentUserId) // ✅
            }

        })

        binding.recyclerMyFav.layoutManager = LinearLayoutManager(requireContext()) // ✅ Add LayoutManager
        binding.recyclerMyFav.adapter = adapter
    }


    private fun observeFavoriteItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userFavorites.collectLatest { favoriteItems ->
                adapter.updateList(favoriteItems) // ✅ Updates RecyclerView with only the current user's liked items

                binding.recyclerMyFav.postDelayed({
                    val currentCount = adapter.itemCount

                    if (currentCount == 0) { // ✅ Show toast only if no liked items exist
                        println("🔥 DEBUG: No favorites found for current user in RecyclerView!")
                        Toast.makeText(requireContext(), "No favorites found!", Toast.LENGTH_SHORT).show()
                    } else {
                        println("🔥 DEBUG: ${currentCount} favorite items found in RecyclerView!")
                        Toast.makeText(requireContext(), "$currentCount favorite items found!", Toast.LENGTH_SHORT).show()
                    }
                }, 800) // ✅ Ensures Firestore finishes loading before checking RecyclerView state
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



