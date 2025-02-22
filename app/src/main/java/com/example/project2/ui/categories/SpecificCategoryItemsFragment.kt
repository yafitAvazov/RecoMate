package com.example.project2.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2.R
import com.example.project2.data.model.CategoryMapper
import com.example.project2.data.model.Item
import com.example.project2.databinding.SpecificCategoryItemsBinding
import com.example.project2.ui.all_recommendation.ItemAdapter
import com.example.project2.ui.all_recommendation.RecommendationListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpecificCategoryItemsFragment : Fragment() {

    private var _binding: SpecificCategoryItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationListViewModel by activityViewModels()
    private lateinit var adapter: ItemAdapter

    private var categoryName: String? = null
    private var categoryImageResId = 0

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
        loadItems(categoryName ?: "")
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
                findNavController().navigate(R.id.action_specificCategoryItemsFragment_to_itemDetailsFragment, bundle)
            }

            override fun onItemDeleted(item: Item) {
                viewModel.updateLikeStatus(item.id, false)
            }

            override fun onItemLiked(item: Item) {
                viewModel.updateLikeStatus(item.id, true)
            }

            override fun onItemUnliked(item: Item) {
                viewModel.updateLikeStatus(item.id, false)
            }
        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())


    }

    private fun loadItems(category: String) {
        val categoryKey = CategoryMapper.getCategoryId(category,requireContext())
        lifecycleScope.launch {
            viewModel.fetchItemsByCategory(categoryKey.toString()) // ✅ Uses universal key
            viewModel.items.collectLatest { items ->
                adapter.updateList(items)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
