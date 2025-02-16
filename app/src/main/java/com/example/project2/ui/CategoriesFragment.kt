package com.example.project2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.ui.adapters.CategoryAdapter
import com.example.project2.ui.model.Category
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.categories_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 עמודות בגריד

        val categories = listOf(
            Category("Home", R.drawable.home_icon),
            Category("Sport", R.drawable.sport_icon),
            Category("Fashion", R.drawable.suit_icon),
            Category("Health", R.drawable.health_icon),
            Category("Game", R.drawable.game_icon),
            Category("Food", R.drawable.food_icon),
            Category("Beauty", R.drawable.beauty_icon),
            Category("Books", R.drawable.books_icon),
            Category("Travel", R.drawable.travel_icon),
            Category("Shops", R.drawable.shop_icon),
            Category("Movies", R.drawable.movie_icon),
            Category("Technology", R.drawable.technology_icon)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            Toast.makeText(requireContext(), "בחרת בקטגוריה: ${category.name}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = categoryAdapter
    }
}
