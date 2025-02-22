package com.example.project2.ui.categories

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.databinding.CategoriesLayoutBinding
import com.example.project2.ui.model.Category
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.project2.ui.recommendation_detail.RecommendationDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private var _binding : CategoriesLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryItemsViewModel by viewModels()
    private val detailViewModel: RecommendationDetailViewModel by viewModels()
    private val PREFS_NAME = "prefs"
    private val KEY_POPUP_COUNTER = "popup_counter"



    private fun showWelcomePopup(username: String) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0)
        val popupCounter = sharedPreferences.getInt(KEY_POPUP_COUNTER, 0)

        android.util.Log.d("PopupDebug", "Popup Counter: $popupCounter")
        if (popupCounter > 0) return
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_welcome, null)
        val popupText = popupView.findViewById<TextView>(R.id.welcomeText)

        popupText.text = "Hello, $username!"

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        Handler().postDelayed({
            popupWindow.showAtLocation(binding.root, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 150)
            Handler().postDelayed({ popupWindow.dismiss() }, 3000)
        }, 500)
        sharedPreferences.edit().putInt(KEY_POPUP_COUNTER, popupCounter + 1).apply()
        val updatedCounter = sharedPreferences.getInt(KEY_POPUP_COUNTER, 0)
        android.util.Log.d("PopupDebug", "Updated Popup Counter: $updatedCounter")
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = CategoriesLayoutBinding.inflate(inflater,container,false)

        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0)
        val popupCounter = sharedPreferences.getInt(KEY_POPUP_COUNTER, 0)

        android.util.Log.d("PopupDebug", "Popup Counter onCreateView: $popupCounter")
        if (popupCounter > 0) return binding.root

        detailViewModel.getUsername().observe(viewLifecycleOwner) { username ->
            username?.let {
                showWelcomePopup(it)
                sharedPreferences.edit().putInt(KEY_POPUP_COUNTER, popupCounter + 1).apply()
                val updatedCounter = sharedPreferences.getInt(KEY_POPUP_COUNTER, 0)
                android.util.Log.d("PopupDebug", "Updated Popup Counter onCreateView: $updatedCounter")
            }
        }
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
            val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0)
            sharedPreferences.edit().putInt(KEY_POPUP_COUNTER, 0).apply()

            val updatedCounter = sharedPreferences.getInt(KEY_POPUP_COUNTER, 0)
            android.util.Log.d("PopupDebug", "Counter Reset on Sign Out: $updatedCounter")

        }
        return super.onOptionsItemSelected(item)
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
