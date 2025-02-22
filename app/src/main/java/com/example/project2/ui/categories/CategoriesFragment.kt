package com.example.project2.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.databinding.CategoriesLayoutBinding
import com.example.project2.ui.model.Category
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private var _binding : CategoriesLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = CategoriesLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView // 🟢 ה-Binding מאותחל כראוי!
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val categories = listOf(
            Category(getString(R.string.home), R.drawable.home_icon),
            Category(getString(R.string.sport), R.drawable.sport_icon),
            Category(getString(R.string.fashion), R.drawable.suit_icon),
            Category(getString(R.string.health), R.drawable.health_icon),
            Category(getString(R.string.game), R.drawable.game_icon),
            Category(getString(R.string.food), R.drawable.food_icon),
            Category(getString(R.string.beauty), R.drawable.beauty_icon),
            Category(getString(R.string.book), R.drawable.books_icon),
            Category(getString(R.string.travel), R.drawable.travel_icon),
            Category(getString(R.string.shops), R.drawable.shop_icon),
            Category(getString(R.string.movie), R.drawable.movie_icon),
            Category(getString(R.string.tech), R.drawable.technology_icon)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            val bundle = Bundle().apply {
                putString("categoryName", category.name)
                putInt("categoryImage", category.imageResId)
            }
            Toast.makeText(requireContext(),
                getString(R.string.category_chosen, category.name), Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_categoriesFragment_to_specificCategoryItemsFragment, bundle)
        }
        binding.allCategoriesButton.setOnClickListener {

            findNavController().navigate(R.id.action_categoriesFragment_to_allItemsFragment)
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                ?.selectedItemId = R.id.nav_all_recommendation
        }

        recyclerView.adapter = categoryAdapter
    }

    // 🟢 שחרור ה-Binding כדי למנוע Memory Leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
